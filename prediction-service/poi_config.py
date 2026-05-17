import json
import os
import logging

logger = logging.getLogger(__name__)

_CONFIG_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'pois.json')


def load_pois(config_path: str = _CONFIG_PATH) -> dict:
    """Load POIs from JSON config file."""
    with open(config_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
        return {int(k): v for k, v in data.items()}


def save_pois(pois: dict, config_path: str = _CONFIG_PATH):
    """Save POIs dict back to JSON config file."""
    data = {str(k): v for k, v in sorted(pois.items())}
    with open(config_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)
    logger.info(f"Saved {len(pois)} POIs to {config_path}")


# Module-level constants — imported by main.py, train.py, extract_and_preprocess.py
POIS = load_pois()
NUM_POIS = len(POIS)
POI_IDS_SORTED = sorted(POIS.keys())
