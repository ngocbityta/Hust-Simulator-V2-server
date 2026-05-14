import grpc
from concurrent import futures
import time
import logging
import random

# Import generated classes
import prediction_pb2
import prediction_pb2_grpc

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger('PredictionService')

class PredictionServiceServicer(prediction_pb2_grpc.PredictionServiceServicer):
    def PredictNextLocation(self, request, context):
        logger.info(f"Received prediction request for user: {request.user_id}")
        logger.info(f"Trajectory points received: {len(request.trajectory)}")
        logger.info(f"Current heading: {request.current_heading}")

        # --- Mock STTF Implementation ---
        # In Phase 3, this will be replaced with PyTorch model inference
        # e.g., model(trajectory_tensor)
        
        # Simulate processing time
        time.sleep(0.1)
        
        # If trajectory is very short, confidence is low
        confidence = 0.85 if len(request.trajectory) >= 5 else 0.4
        
        # Mock predicted POIs
        mock_pois = [
            {"id": "c1_building", "name": "Tòa C1", "intent": "GOING_TO_CLASS", "lat": 21.006, "lng": 105.843},
            {"id": "d3_building", "name": "Tòa D3", "intent": "GOING_TO_CLASS", "lat": 21.004, "lng": 105.845},
            {"id": "library_tqb", "name": "Thư viện Tạ Quang Bửu", "intent": "GOING_TO_STUDY", "lat": 21.005, "lng": 105.844},
            {"id": "canteen_b", "name": "Căng tin B", "intent": "GOING_TO_EAT", "lat": 21.003, "lng": 105.842}
        ]
        
        # Select a random mock POI
        selected_poi = random.choice(mock_pois)

        response = prediction_pb2.PredictNextLocationResponse(
            predicted_poi_id=selected_poi["id"],
            predicted_poi_name=selected_poi["name"],
            confidence=confidence,
            intent_type=selected_poi["intent"],
            target_lat=selected_poi["lat"],
            target_lng=selected_poi["lng"]
        )
        
        logger.info(f"Returning prediction: {selected_poi['name']} with confidence {confidence}")
        return response

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    prediction_pb2_grpc.add_PredictionServiceServicer_to_server(
        PredictionServiceServicer(), server)
    
    # Listen on port 50055
    server.add_insecure_port('[::]:50055')
    server.start()
    logger.info("Prediction Service started on port 50055")
    
    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        server.stop(0)

if __name__ == '__main__':
    serve()
