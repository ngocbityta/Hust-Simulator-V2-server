"""
Synthetic trajectory data generator for STTF-Recommender cold-start training.
Simulates realistic HUST student movement patterns.

Usage:
    python generate_synthetic_data.py --num-users 200 --journeys-per-user 15

Output: data/checkin_sequences.csv (same format as ETL pipeline)
"""

import os
import csv
import uuid
import math
import random
import argparse
from datetime import datetime, timedelta

from poi_config import POIS, NUM_POIS, POI_IDS_SORTED

# ===================== Dynamic POI Categorization =====================

def categorize_pois():
    categories = {
        'academic': [],
        'canteen': [],
        'library': [],
        'dorm': [],
        'other': []
    }
    
    for poi_id in POI_IDS_SORTED:
        name = POIS[poi_id]['name'].lower()
        if 'thư viện' in name or 'library' in name:
            categories['library'].append(poi_id)
        elif 'căng tin' in name or 'canteen' in name:
            categories['canteen'].append(poi_id)
        elif 'ký túc' in name or 'ktx' in name or 'dorm' in name:
            categories['dorm'].append(poi_id)
        elif 'tòa' in name or 'nhà' in name or 'viện' in name or 'c1' in name or 'd3' in name:
            categories['academic'].append(poi_id)
        else:
            categories['other'].append(poi_id)
            
    # Fallback if a category is empty (just use all POIs)
    for k, v in categories.items():
        if not v:
            categories[k] = POI_IDS_SORTED
            
    return categories

CATEGORIES = categorize_pois()

def get_random_poi(category):
    return random.choice(CATEGORIES[category])

# ===================== Behavior Profiles =====================

def generate_journey(user_id_str, day_of_week):
    """
    Generate a single day's trajectory using dynamic POI categories.
    """
    checkins = []
    
    # Decide user type for this journey (Study heavy, Library heavy, Relaxed)
    journey_type = random.choices(['study', 'library', 'relaxed'], weights=[0.5, 0.3, 0.2])[0]

    if day_of_week >= 5:
        active_slots = random.choices(["morning", "afternoon", "evening"], weights=[0.2, 0.4, 0.4], k=random.randint(1, 2))
    else:
        active_slots = random.choices(["morning", "afternoon", "evening"], weights=[0.4, 0.4, 0.2], k=random.randint(2, 3))

    seen = set()
    active_slots = [s for s in active_slots if s not in seen and not seen.add(s)]
    
    # Base building for the day
    main_building = get_random_poi('academic')
    
    for slot in active_slots:
        if slot == "morning":
            h_start, h_end = 7, 11
        elif slot == "afternoon":
            h_start, h_end = 13, 17
        else:
            h_start, h_end = 18, 21
            
        pattern = []
        if journey_type == 'study':
            pattern = [main_building, get_random_poi('canteen') if random.random() > 0.5 else get_random_poi('academic')]
        elif journey_type == 'library':
            pattern = [get_random_poi('library'), get_random_poi('canteen')]
        else:
            pattern = [get_random_poi('dorm'), get_random_poi('other')]
            
        # Sometimes go home or to another random place
        if random.random() > 0.7:
            pattern.append(get_random_poi('other'))

        for i, poi_id in enumerate(pattern):
            hour = random.randint(h_start, h_end - 1) + i
            hour = min(hour, 23)
            hour_of_week = day_of_week * 24 + hour

            if random.random() < 0.10 and len(pattern) > 1:
                continue

            checkins.append((poi_id, hour_of_week))

    return checkins


def generate_dataset(num_users, journeys_per_user, output_path):
    """Generate synthetic check-in dataset."""
    records = []
    journey_count = 0
    skipped = 0

    for u in range(num_users):
        user_id = f"synthetic-user-{u:04d}"

        for j in range(journeys_per_user):
            # Pick a random day of the week for this journey
            day_of_week = random.randint(0, 6)  # 0=Mon, 6=Sun

            checkins = generate_journey(user_id, day_of_week)

            # Filter: paper says discard trajectories with < 3 check-ins
            if len(checkins) < 3:
                skipped += 1
                continue

            journey_id = str(uuid.uuid4())
            target_poi = checkins[-1][0]  # Final destination = training label

            for step, (poi_id, hour_of_week) in enumerate(checkins):
                records.append({
                    'journey_id':   journey_id,
                    'step':         step,
                    'user_id':      user_id,
                    'poi_id':       poi_id,
                    'hour_of_week': hour_of_week,
                    'target_poi':   target_poi,
                })

            journey_count += 1

    # Write CSV
    os.makedirs(os.path.dirname(output_path) if os.path.dirname(output_path) else '.', exist_ok=True)
    with open(output_path, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=[
            'journey_id', 'step', 'user_id', 'poi_id', 'hour_of_week', 'target_poi'
        ])
        writer.writeheader()
        writer.writerows(records)

    return journey_count, len(records), skipped


def main():
    parser = argparse.ArgumentParser(
        description='Generate synthetic trajectory data for STTF-Recommender'
    )
    parser.add_argument('--num-users', type=int, default=200,
                        help='Number of synthetic users')
    parser.add_argument('--journeys-per-user', type=int, default=15,
                        help='Number of journeys per user')
    parser.add_argument('--output', type=str, default='data/checkin_sequences.csv',
                        help='Output CSV file path')
    parser.add_argument('--seed', type=int, default=42,
                        help='Random seed for reproducibility')
    args = parser.parse_args()

    random.seed(args.seed)

    print(f"Generating synthetic data ...")
    print(f"  Users:            {args.num_users}")
    print(f"  Journeys/user:    {args.journeys_per_user}")
    print(f"  POIs:             {NUM_POIS} (from pois.json)")

    journeys, records, skipped = generate_dataset(
        args.num_users, args.journeys_per_user, args.output
    )

    print(f"\nResults:")
    print(f"  Valid journeys:   {journeys}")
    print(f"  Check-in records: {records}")
    print(f"  Skipped (short):  {skipped}")
    print(f"  Output:           {args.output}")

    # Print top 5 POI distribution
    import csv as csv_mod
    poi_counts = {}
    with open(args.output, 'r') as f:
        reader = csv_mod.DictReader(f)
        for row in reader:
            t = int(row['target_poi'])
            poi_counts[t] = poi_counts.get(t, 0) + 1

    print(f"\nTop 5 Target POIs:")
    sorted_pois = sorted(poi_counts.items(), key=lambda x: x[1], reverse=True)
    for poi_id, count in sorted_pois[:5]:
        name = POIS[poi_id]['name']
        print(f"  [{poi_id}] {name}: {count}")


if __name__ == '__main__':
    main()
