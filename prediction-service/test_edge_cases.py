import grpc
import prediction_pb2
import prediction_pb2_grpc
import time

def run_test(name, trajectory, target_time=None):
    print(f"--- Running Test: {name} ---")
    try:
        channel = grpc.insecure_channel('localhost:50055')
        stub = prediction_pb2_grpc.PredictionServiceStub(channel)
        
        request = prediction_pb2.PredictNextLocationRequest(
            user_id="test_user_1",
            trajectory=trajectory,
            current_heading=0.0
        )
        if target_time:
            request.target_timestamp_ms = target_time
            
        response = stub.PredictNextLocation(request)
        print(f"Predicted POI: {response.predicted_poi_name} (ID: {response.predicted_poi_id})")
        print(f"Confidence: {response.confidence:.3f}")
        print(f"Candidates: {len(response.candidate_destinations)}")
        for c in response.candidate_destinations[:3]:
            print(f"  - {c.poi_name}: {c.probability:.3f}")
    except Exception as e:
        print(f"Error during test: {e}")
    print()

def main():
    # 1. Empty trajectory
    run_test("Empty Trajectory", [])

    # 2. Out of bounds trajectory (lat: 0, lng: 0)
    out_of_bounds = [
        prediction_pb2.TrajectoryPoint(latitude=0.0, longitude=0.0, timestamp=int(time.time()*1000))
    ]
    run_test("Out of Bounds (No POI Snap)", out_of_bounds)

    # 3. Short trajectory near C5 (Fallback heuristic test)
    # C5: lat: 21.005764, lng: 105.843992
    short_traj = [
        prediction_pb2.TrajectoryPoint(latitude=21.00576, longitude=105.84399, timestamp=int(time.time()*1000))
    ]
    run_test("1-Point Trajectory near C5", short_traj)

    # 4. Very long trajectory (60 points) to test sequence truncation
    long_traj = []
    for i in range(60):
        # Slightly moving around C5
        lat = 21.00576 + (i * 0.000001)
        long_traj.append(
            prediction_pb2.TrajectoryPoint(latitude=lat, longitude=105.84399, timestamp=int((time.time() - (60-i)*300)*1000))
        )
    run_test("60-Point Trajectory (Exceeds MAX_SEQ_LEN)", long_traj)
    
if __name__ == '__main__':
    main()
