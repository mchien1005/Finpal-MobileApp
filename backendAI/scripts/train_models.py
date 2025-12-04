"""
Train All ML Models - Script Huấn Luyện Tất Cả Models

Script này train 3 ML models cho FinPal AI:
1. TransactionCategorizer - Phân loại giao dịch
2. AnomalyDetector - Phát hiện giao dịch bất thường
3. SpendingPredictor - Dự đoán chi tiêu tương lai

Usage:
    python scripts/train_models.py

Prerequisites:
    - Cần có file data/raw/transactions.csv (chạy generate_sample_data.py trước)
    - Đã cài đặt dependencies: scikit-learn, pandas, numpy, joblib
"""

import sys
import os

# Add parent directory to path để import được app modules
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from app.models.categorization import TransactionCategorizer
from app.models.anomaly_detection import AnomalyDetector
from app.models.spending_prediction import SpendingPredictor


def main():
    """
    Train all models - Huấn luyện tất cả models
    
    Quy trình:
    1. Check data availability
    2. Train categorization model (Random Forest)
    3. Train anomaly detection model (Isolation Forest)
    4. Train spending prediction model (Linear Regression)
    5. Save all models to data/models/
    """
    
    print("=" * 60)
    print("🚀 FINPAL AI - MODEL TRAINING")
    print("=" * 60)
    
    data_path = "data/raw/transactions.csv"
    
    # Check if data exists
    if not os.path.exists(data_path):
        print(f"\n❌ Data file not found: {data_path}")
        print("Please run: python scripts/generate_sample_data.py")
        return
    
    # Train categorization model
    print("\n" + "=" * 60)
    print("1️⃣  TRAINING CATEGORIZATION MODEL")
    print("=" * 60)
    
    try:
        categorizer = TransactionCategorizer()
        categorizer.train(data_path)
    except Exception as e:
        print(f"\n❌ Error training categorization model: {e}")
        return
    
    # Train anomaly detection model
    print("\n" + "=" * 60)
    print("2️⃣  TRAINING ANOMALY DETECTION MODEL")
    print("=" * 60)
    
    try:
        detector = AnomalyDetector()
        detector.train(data_path)
    except Exception as e:
        print(f"\n❌ Error training anomaly detection model: {e}")
        return
    
    # Train spending prediction model
    print("\n" + "=" * 60)
    print("3️⃣  TRAINING SPENDING PREDICTION MODEL")
    print("=" * 60)
    
    try:
        predictor = SpendingPredictor()
        predictor.train(data_path)
    except Exception as e:
        print(f"\n❌ Error training spending prediction model: {e}")
        return
    
    print("\n" + "=" * 60)
    print("✅ ALL MODELS TRAINED SUCCESSFULLY")
    print("=" * 60)
    print("\n📋 Next steps:")
    print("1. Start the API server: uvicorn app.main:app --reload")
    print("2. Open docs: http://localhost:8000/docs")
    print("3. Test endpoints with sample data")


if __name__ == "__main__":
    main()
