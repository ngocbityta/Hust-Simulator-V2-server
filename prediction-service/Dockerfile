FROM python:3.11-slim

WORKDIR /app

COPY prediction-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Create a directory for proto files and generated code
RUN mkdir -p /app/proto

COPY prediction-service/ .

# Copy proto file from the root proto dir (this assumes build context is server dir)
# We will do this via a script or command to generate pb2 files.

# Entrypoint: bootstrap model if missing, then start services
CMD ["sh", "-c", "\
  if [ ! -f model/sttf_model.pth ]; then \
    echo '[Bootstrap] No trained model found. Generating synthetic data and training...'; \
    python generate_synthetic_data.py --num-users 200 --journeys-per-user 15 && \
    python train.py --data data/checkin_sequences.csv --epochs 30; \
  fi && \
  python pipeline_orchestrator.py & python main.py"]
