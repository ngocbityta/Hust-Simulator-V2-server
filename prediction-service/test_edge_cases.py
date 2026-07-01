import grpc
import prediction_pb2
import prediction_pb2_grpc
import time

# Use a real UUID — the service validates UUID format and queries DB by user_id.
# Replace with an actual user UUID from your database that has check-in history.
TEST_USER_ID = "00000000-0000-0000-0000-000000000001"

def run_test(name, user_id, target_time_ms=None):
    print(f"--- Running Test: {name} ---")
    try:
        channel = grpc.insecure_channel('localhost:50051')
        stub = prediction_pb2_grpc.PredictionServiceStub(channel)

        request = prediction_pb2.PredictNextLocationRequest(
            user_id=user_id,
            current_heading=0.0
        )
        if target_time_ms:
            request.target_timestamp_ms = target_time_ms

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
    # 1. User with no history (invalid UUID → empty history → empty predictions)
    run_test("Non-existent User (no history)", "00000000-0000-0000-0000-000000000002")

    # 2. Invalid UUID format (should return empty predictions)
    run_test("Invalid UUID Format", "not-a-uuid")

    # 3. Valid user with target time = 1 hour from now
    run_test("Valid User + Target Time (+1h)", TEST_USER_ID, int((time.time() + 3600) * 1000))

    # 4. Valid user with no target time (uses current time)
    run_test("Valid User + No Target Time", TEST_USER_ID)

if __name__ == '__main__':
    main()
