FROM python:3.11-slim

WORKDIR /app

COPY prediction-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Create a directory for proto files and generated code
RUN mkdir -p /app/proto

COPY prediction-service/ .

# Copy proto file from the root proto dir (this assumes build context is server dir)
# We will do this via a script or command to generate pb2 files.

CMD ["python", "main.py"]
