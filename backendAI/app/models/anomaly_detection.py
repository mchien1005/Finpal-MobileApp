"""
Anomaly Detection Model using Isolation Forest - Model Phát Hiện Bất Thường

Model Machine Learning sử dụng Isolation Forest algorithm để phát hiện các giao dịch
bất thường có thể là gian lận, lỗi, hoặc chi tiêu không hợp lý.

Đặc điểm:
- Thuật toán: Isolation Forest (unsupervised learning)
- Contamination rate: 5% (giả định 5% giao dịch là anomaly)
- Features: Amount, time, user-specific statistics
- Use case: Phát hiện giao dịch cao bất thường, thời gian lạ, category không quen

Ví dụ anomaly:
- Chi tiêu 5 triệu cho trà sữa (bình thường 50k)
- Mua sắm lúc 3AM
- Chi tiêu cao hơn 3x trung bình của user
"""

import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import joblib
import os
from typing import Tuple, Dict


class AnomalyDetector:
    """
    ML Model cho phát hiện giao dịch bất thường - ML Model for detecting anomalous transactions
    
    Sử dụng Isolation Forest để phát hiện outliers trong chi tiêu của user.
    Model học từ lịch sử để hiểu "normal behavior" và cảnh báo khi có bất thường.
    """
    
    def __init__(self, model_path: str = "data/models"):
        """
        Khởi tạo AnomalyDetector
        
        Args:
            model_path: Đường dẫn thư mục lưu model
        """
        self.model_path = model_path
        self.model = None            # Isolation Forest model
        self.scaler = None           # StandardScaler cho features
        self.user_stats = {}         # Thống kê chi tiêu của từng user
        
    def _extract_features(self, df: pd.DataFrame) -> pd.DataFrame:
        """
        Trích xuất features cho anomaly detection - Extract features for anomaly detection
        
        Features bao gồm:
        1. Amount features: amount, log(amount)
        2. Time features: hour, day_of_week, is_weekend, is_night
        3. User-specific: amount vs user's mean, z-score
        
        Args:
            df: DataFrame chứa transaction data
        
        Returns:
            pd.DataFrame: DataFrame chứa engineered features
        """
        
        features = pd.DataFrame()
        
        # Amount features
        features['amount'] = df['amount']
        features['amount_log'] = np.log1p(df['amount'])
        
        # Time features - phát hiện thời gian bất thường
        if 'timestamp' in df.columns:
            df['timestamp'] = pd.to_datetime(df['timestamp'])
            features['hour'] = df['timestamp'].dt.hour
            features['day_of_week'] = df['timestamp'].dt.dayofweek
            features['is_weekend'] = (df['timestamp'].dt.dayofweek >= 5).astype(int)
            features['is_night'] = ((df['timestamp'].dt.hour < 6) | (df['timestamp'].dt.hour >= 22)).astype(int)  # 10PM-6AM
        
        # User-specific features - so sánh với thói quen cá nhân
        if 'user_id' in df.columns:
            for user_id in df['user_id'].unique():
                user_mask = df['user_id'] == user_id
                user_data = df[user_mask]
                
                if len(user_data) > 0:
                    user_mean = user_data['amount'].mean()
                    user_std = user_data['amount'].std()
                    
                    # Amount so với trung bình của user
                    features.loc[user_mask, 'amount_vs_mean'] = (
                        user_data['amount'] / user_mean if user_mean > 0 else 1
                    )
                    # Z-score: số lệch chuẩn so với mean
                    features.loc[user_mask, 'amount_zscore'] = (
                        (user_data['amount'] - user_mean) / user_std if user_std > 0 else 0
                    )
        
        return features.fillna(0)
    
    def _calculate_user_stats(self, df: pd.DataFrame):
        """
        Tính thống kê cho từng user - Calculate statistics per user for anomaly detection
        
        Lưu baseline statistics của mỗi user để so sánh với giao dịch mới.
        Bao gồm: mean, median, std, quantiles, top categories.
        
        Args:
            df: DataFrame chứa transaction data
        """
        
        for user_id in df['user_id'].unique():
            user_data = df[df['user_id'] == user_id]
            
            self.user_stats[user_id] = {
                'mean_amount': user_data['amount'].mean(),
                'std_amount': user_data['amount'].std(),
                'median_amount': user_data['amount'].median(),
                'q75_amount': user_data['amount'].quantile(0.75),      # 75% giao dịch thấp hơn
                'q95_amount': user_data['amount'].quantile(0.95),      # 95% giao dịch thấp hơn
                'transaction_count': len(user_data),
                'categories': user_data['category'].value_counts().to_dict()  # Top categories
            }
    
    def train(self, data_path: str = "data/raw/transactions.csv"):
        """
        Train model phát hiện anomaly - Train the anomaly detection model
        
        Quy trình:
        1. Load expense transactions
        2. Calculate user statistics (baseline)
        3. Extract features
        4. Scale features (StandardScaler)
        5. Train Isolation Forest (contamination=5%)
        6. Evaluate trên training data
        7. Save model, scaler, user_stats
        
        Args:
            data_path: Đường dẫn file CSV training data
        """
        
        print("📊 Loading training data...")
        df = pd.read_csv(data_path)
        
        # Chỉ train trên expense transactions
        df = df[df['transaction_type'] == 'EXPENSE'].copy()
        
        print(f"✅ Loaded {len(df)} expense transactions")
        
        # Calculate user statistics (baseline cho mỗi user)
        print("\n📈 Calculating user statistics...")
        self._calculate_user_stats(df)
        
        # Extract features
        print("\n🔧 Extracting features...")
        X = self._extract_features(df)
        
        print(f"📦 Features shape: {X.shape}")
        print(f"📋 Features: {X.columns.tolist()}")
        
        # Scale features (Isolation Forest yêu cầu scaled features)
        self.scaler = StandardScaler()
        X_scaled = self.scaler.fit_transform(X)
        
        # Train Isolation Forest
        print("\n🤖 Training Isolation Forest model...")
        """
        Isolation Forest Parameters:
        - contamination=0.05: Giả định 5% transactions là anomaly
        - max_samples=256: Mỗi tree train trên 256 samples
        - random_state=42: Reproducible results
        - n_jobs=-1: Sử dụng tất cả CPU cores
        """
        self.model = IsolationForest(
            contamination=0.05,  # 5% anomalies
            max_samples=256,
            random_state=42,
            n_jobs=-1
        )
        
        self.model.fit(X_scaled)
        
        # Evaluate on training data để kiểm tra model
        predictions = self.model.predict(X_scaled)
        anomaly_count = (predictions == -1).sum()
        
        print(f"\n✅ Model trained successfully!")
        print(f"🎯 Detected anomalies: {anomaly_count} ({anomaly_count/len(df)*100:.2f}%)")
        
        # Compare with actual anomalies (if labeled)
        # Nếu có label thật, đánh giá precision/recall
        if 'is_anomaly' in df.columns:
            actual_anomalies = df['is_anomaly'].sum()
            true_positives = ((predictions == -1) & (df['is_anomaly'])).sum()
            precision = true_positives / anomaly_count if anomaly_count > 0 else 0
            recall = true_positives / actual_anomalies if actual_anomalies > 0 else 0
            
            print(f"📊 Actual labeled anomalies: {actual_anomalies}")
            print(f"🎯 Precision: {precision:.2%}")
            print(f"🎯 Recall: {recall:.2%}")
        
        # Save model
        self.save()
    
    def detect(
        self,
        user_id: int,
        amount: float,
        merchant: str,
        category: str,
        timestamp: pd.Timestamp = None
    ) -> Tuple[bool, float, str, str]:
        """
        Phát hiện anomaly cho transaction mới - Detect if transaction is anomalous
        
        Sử dụng trained model để dự đoán xem giao dịch có bất thường không.
        Trả về anomaly score, reason và recommendation.
        
        Args:
            user_id: ID người dùng
            amount: Số tiền giao dịch
            merchant: Tên merchant (ví dụ: "Shopee", "Circle K")
            category: Category ("Ăn uống", "Mua sắm", ...)
            timestamp: Thời gian giao dịch (default = now)
        
        Returns:
            is_anomaly: True nếu là anomaly
            anomaly_score: Score 0-1 (càng cao càng bất thường)
            reason: Lý do cụ thể (ví dụ: "Số tiền cao hơn 95% giao dịch")
            recommendation: Khuyến nghị cho user
        """
        
        if self.model is None:
            self.load()
        
        # Create dataframe từ input
        data = pd.DataFrame([{
            'user_id': user_id,
            'amount': amount,
            'merchant': merchant,
            'category': category,
            'timestamp': timestamp or pd.Timestamp.now()
        }])
        
        # Extract features (giống như training)
        X = self._extract_features(data)
        X_scaled = self.scaler.transform(X)
        
        # Predict using Isolation Forest
        prediction = self.model.predict(X_scaled)[0]       # 1 = normal, -1 = anomaly
        decision_score = self.model.decision_function(X_scaled)[0]  # Negative = anomaly
        
        # Normalize score to 0-1 (lower decision_score = more anomalous)
        anomaly_score = 1 / (1 + np.exp(decision_score))  # Sigmoid transformation
        
        is_anomaly = prediction == -1
        
        # Phân tích lý do và recommendation
        reason, recommendation = self._analyze_anomaly(
            user_id, amount, category, anomaly_score, is_anomaly
        )
        
        return is_anomaly, float(anomaly_score), reason, recommendation
    
    def _analyze_anomaly(
        self,
        user_id: int,
        amount: float,
        category: str,
        anomaly_score: float,
        is_anomaly: bool
    ) -> Tuple[str, str]:
        """
        Phân tích lý do anomaly - Analyze why transaction is anomalous
        
        So sánh với user statistics để xác định lý do cụ thể:
        - Amount quá cao
        - Category lạ
        - Time không bình thường
        
        Args:
            user_id: User ID
            amount: Số tiền giao dịch
            category: Category
            anomaly_score: Score từ model
            is_anomaly: True nếu là anomaly
        
        Returns:
            reason: Lý do cụ thể (Vietnamese)
            recommendation: Khuyến nghị cho user (Vietnamese)
        """
        
        if not is_anomaly:
            return (
                "Giao dịch bình thường",
                "Giao dịch này nằm trong phạm vi thông thường của bạn"
            )
        
        # Get user statistics
        stats = self.user_stats.get(user_id, {})
        
        if not stats:
            return (
                "Không đủ dữ liệu lịch sử",
                "Chưa có đủ dữ liệu để đánh giá. Tiếp tục theo dõi giao dịch này."
            )
        
        mean_amount = stats.get('mean_amount', 0)
        q95_amount = stats.get('q95_amount', 0)
        
        # Determine specific reason dựa vào amount
        if amount > mean_amount * 3:
            multiplier = amount / mean_amount
            return (
                f"Giao dịch cao hơn {multiplier:.1f}x mức trung bình",
                f"Giao dịch này cao bất thường. Trung bình bạn chi {mean_amount:,.0f} VND, "
                f"nhưng giao dịch này là {amount:,.0f} VND. Xác nhận lại giao dịch."
            )
        
        if amount > q95_amount:
            return (
                "Giao dịch nằm trong top 5% cao nhất",
                f"Giao dịch này cao hơn 95% các giao dịch trước đây của bạn. "
                f"Xem xét lại nếu cần."
            )
        
        return (
            f"Giao dịch bất thường (điểm: {anomaly_score:.2f})",
            "Giao dịch này có đặc điểm khác lạ so với thói quen chi tiêu của bạn. "
            "Kiểm tra lại để đảm bảo đúng."
        )
    
    def save(self):
        """
        Lưu model vào disk - Save model to disk
        
        Lưu 3 files:
        - anomaly_model.pkl: Isolation Forest model
        - anomaly_scaler.pkl: StandardScaler
        - anomaly_user_stats.pkl: User statistics dictionary
        """
        os.makedirs(self.model_path, exist_ok=True)
        
        joblib.dump(self.model, f"{self.model_path}/anomaly_model.pkl")
        joblib.dump(self.scaler, f"{self.model_path}/anomaly_scaler.pkl")
        joblib.dump(self.user_stats, f"{self.model_path}/anomaly_user_stats.pkl")
        
        print(f"\n💾 Model saved to {self.model_path}/")
    
    def load(self):
        """
        Load model từ disk - Load model from disk
        
        Load 3 files đã save để sử dụng cho prediction.
        Nếu không tìm thấy file, raise FileNotFoundError.
        """
        try:
            self.model = joblib.load(f"{self.model_path}/anomaly_model.pkl")
            self.scaler = joblib.load(f"{self.model_path}/anomaly_scaler.pkl")
            self.user_stats = joblib.load(f"{self.model_path}/anomaly_user_stats.pkl")
            print("✅ Anomaly detector loaded successfully")
        except FileNotFoundError:
            raise FileNotFoundError(
                f"Model not found in {self.model_path}/. Please train the model first."
            )


if __name__ == "__main__":
    # Train model
    detector = AnomalyDetector()
    detector.train()
    
    # Test detection
    print("\n🧪 Testing anomaly detection...")
    test_cases = [
        (1, 50000, "GRAB", "Di chuyển"),
        (1, 5000000, "SHOPEE", "Mua sắm"),  # Anomaly
        (1, 85000, "HIGHLANDS COFFEE", "Ăn uống"),
        (1, 10000000, "ĐIỆN EVN", "Hóa đơn"),  # Anomaly
    ]
    
    for user_id, amount, merchant, category in test_cases:
        is_anomaly, score, reason, rec = detector.detect(
            user_id, amount, merchant, category
        )
        
        print(f"\n{merchant} - {amount:,} VND ({category})")
        print(f"  Anomaly: {is_anomaly} | Score: {score:.2%}")
        print(f"  Reason: {reason}")
        print(f"  → {rec}")
