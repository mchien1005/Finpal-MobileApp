# Dockerfile cho Backend AI Python FastAPI

FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
  gcc \
  g++ \
  && rm -rf /var/lib/apt/lists/*

# Copy requirements
COPY requirements.txt .

# Install Python dependencies với timeout cao hơn
# Split installation để tránh timeout
RUN pip install --default-timeout=1000 --no-cache-dir \
  fastapi==0.104.1 \
  uvicorn[standard]==0.24.0 \
  pydantic==2.5.0 \
  pydantic-settings==2.1.0 \
  pandas==2.1.3 \
  numpy==1.26.2

RUN pip install --default-timeout=1000 --no-cache-dir \
  scikit-learn==1.5.2 \
  joblib==1.3.2 \
  scipy==1.11.4

RUN pip install --default-timeout=1000 --no-cache-dir \
  sqlalchemy==2.0.23 \
  pymysql==1.1.0 \
  httpx==0.25.2 \
  requests==2.31.0 \
  python-dotenv==1.0.0 \
  python-multipart==0.0.6 \
  faker==20.1.0

# Copy application code
COPY app ./app
COPY scripts ./scripts
COPY data ./data

# Create directories for models
RUN mkdir -p data/models data/raw

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD python -c "import requests; requests.get('http://localhost:8000/health')" || exit 1

# Run application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
