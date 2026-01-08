"""
Transaction Categorization Model using Random Forest - Model Phân Loại Giao Dịch

Model Machine Learning sử dụng Random Forest Classifier để tự động phân loại
giao dịch vào các danh mục phù hợp (Ăn uống, Di chuyển, Mua sắm, ...).

Đặc điểm:
- Thuật toán: Random Forest (ensemble learning)
- Features: TF-IDF từ merchant name + numerical features (amount, time)
- Accuracy: ~85-95% tùy dataset
- Training data: Expense transactions từ lịch sử user

Quy trình:
1. Extract features (merchant TF-IDF, amount, time features)
2. Train Random Forest với 100 trees
3. Predict category với confidence score
4. Trả về top 3 categories có khả năng cao nhất
"""

import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import SGDClassifier
from sklearn.calibration import CalibratedClassifierCV
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report, precision_score, recall_score, f1_score
import joblib
import os
from typing import List, Tuple, Dict

# Import training history để ghi dữ liệu thực
try:
    from app.services.training_history import record_training, record_prediction
    HAS_TRAINING_HISTORY = True
except ImportError:
    HAS_TRAINING_HISTORY = False


class TransactionCategorizer:
    """
    ML Model cho phân loại giao dịch tự động - ML Model for automatic transaction categorization
    
    Sử dụng Random Forest để học patterns từ lịch sử giao dịch và tự động
    gán category cho giao dịch mới.
    """
    
    def __init__(self, model_path: str = "data/models"):
        """
        Khởi tạo TransactionCategorizer
        
        Args:
            model_path: Đường dẫn thư mục lưu model đã train
        """
        self.model_path = model_path
        self.model = None                # Random Forest model
        self.vectorizer = None           # TF-IDF vectorizer cho merchant names
        self.label_encoder = None        # Encoder cho category labels
        self.feature_names = []          # Tên các features
        
    def _extract_features(self, df: pd.DataFrame, is_training: bool = False) -> pd.DataFrame:
        """
        Trích xuất features từ transaction data - Extract features from transaction data
        
        Tạo features cho model từ raw transaction data:
        1. Text features: TF-IDF vectorization của merchant + description (150 dimensions)
        2. Numerical features: amount, log(amount), amount_category
        3. Time features: hour, day_of_week, is_weekend, is_month_start/end, quarter
        
        Args:
            df: DataFrame chứa transaction data (merchant, amount, timestamp)
            is_training: True nếu đang training (để tạo combined_text từ description)
        
        Returns:
            pd.DataFrame: DataFrame chứa engineered features sẵn sàng cho model
        """
        
        # QUAN TRỌNG: Kết hợp merchant + description để có context tốt hơn
        if 'description' in df.columns:
            # combined_text = df['merchant'].fillna('') + ' ' + df['description'].fillna('')
            combined_text = df['merchant'].fillna('')
        else:
            combined_text = df['merchant'].fillna('')
        
        # Text features từ combined text (TF-IDF)
        merchant_tfidf = self.vectorizer.transform(combined_text)
        
        # Numerical features
        features = pd.DataFrame()
        features['amount'] = df['amount']
        features['amount_log'] = np.log1p(df['amount'])  # log transform để giảm skewness
        
        # Amount category: low, medium, high, very_high
        amount_q = df['amount'].quantile([0.25, 0.5, 0.75]).values if len(df) > 4 else [50000, 200000, 1000000]
        features['amount_low'] = (df['amount'] <= amount_q[0] if len(amount_q) > 0 else 50000).astype(int)
        features['amount_medium'] = ((df['amount'] > amount_q[0] if len(amount_q) > 0 else 50000) & 
                                     (df['amount'] <= amount_q[1] if len(amount_q) > 1 else 200000)).astype(int)
        features['amount_high'] = ((df['amount'] > amount_q[1] if len(amount_q) > 1 else 200000) & 
                                   (df['amount'] <= amount_q[2] if len(amount_q) > 2 else 1000000)).astype(int)
        features['amount_very_high'] = (df['amount'] > amount_q[2] if len(amount_q) > 2 else 1000000).astype(int)
        
        # Time features - thời gian ảnh hưởng đến category
        if 'timestamp' in df.columns:
            ts = pd.to_datetime(df['timestamp'])
            features['hour'] = ts.dt.hour
            features['day_of_week'] = ts.dt.dayofweek  # 0=Monday, 6=Sunday
            features['day_of_month'] = ts.dt.day
            features['month'] = ts.dt.month
            features['quarter'] = ts.dt.quarter
            features['is_weekend'] = (ts.dt.dayofweek >= 5).astype(int)
            features['is_month_start'] = (ts.dt.day <= 5).astype(int)  # Đầu tháng
            features['is_month_end'] = (ts.dt.day >= 25).astype(int)   # Cuối tháng
            features['is_lunch_time'] = ((ts.dt.hour >= 11) & (ts.dt.hour <= 14)).astype(int)  # Giờ ăn trưa
            features['is_dinner_time'] = ((ts.dt.hour >= 18) & (ts.dt.hour <= 21)).astype(int)  # Giờ ăn tối
            features['is_morning'] = ((ts.dt.hour >= 6) & (ts.dt.hour <= 10)).astype(int)  # Buổi sáng
            features['is_night'] = ((ts.dt.hour >= 22) | (ts.dt.hour <= 5)).astype(int)  # Đêm khuya
        
        # Combine TF-IDF với numerical features
        tfidf_df = pd.DataFrame(
            merchant_tfidf.toarray(),
            columns=[f'text_{i}' for i in range(merchant_tfidf.shape[1])]
        )
        
        features = pd.concat([features.reset_index(drop=True), tfidf_df], axis=1)
        
        return features
    
    def train(self, data_path: str = "data/raw/transactions.csv"):
        """
        Train model phân loại - Train the categorization model
        
        Quy trình training:
        1. Load transaction data từ CSV
        2. Filter chỉ lấy expense transactions
        3. Initialize TF-IDF vectorizer cho merchant names
        4. Extract features (TF-IDF + numerical + time)
        5. Encode category labels
        6. Split train/test (80/20)
        7. Train Random Forest (100 trees)
        8. Evaluate và print metrics
        9. Save model, vectorizer, encoder
        
        Args:
            data_path: Đường dẫn file CSV chứa training data
        
        Returns:
            float: Accuracy score trên test set
        """
        
        print("📊 Loading training data...")
        df = pd.read_csv(data_path)
        
        # Chỉ train trên expense transactions (không train cho income)
        df = df[df['transaction_type'] == 'EXPENSE'].copy()
        
        print(f"✅ Loaded {len(df)} expense transactions")
        print(f"📋 Categories: {df['category'].unique()}")
        
        # Initialize TF-IDF vectorizer - CẢI TIẾN
        # max_features=150: tăng từ 50 lên 150 để capture nhiều patterns hơn
        # ngram_range=(1,3): unigrams + bigrams + trigrams
        # min_df=1: giữ lại tất cả words (vì đã có max_features)
        # sublinear_tf=True: giảm ảnh hưởng của terms xuất hiện nhiều lần
        print("\n🔧 Extracting features (OPTIMIZED)...")
        
        # Kết hợp merchant + description để có context tốt hơn
        if 'description' in df.columns:
            # combined_text = df['merchant'].fillna('') + ' ' + df['description'].fillna('')
            # CHỈ SỬ DỤNG MERCHANT để model tập trung vào tên thương hiệu
            combined_text = df['merchant'].fillna('')
        else:
            combined_text = df['merchant'].fillna('')
        
        # Tối ưu hóa Vectorizer - Giảm RAM nhưng vẫn giữ hiệu quả
        self.vectorizer = TfidfVectorizer(
            max_features=800,     # Giảm xuống 800 để tiết kiệm RAM
            ngram_range=(1, 2),   # Unigrams + bigrams
            analyzer='word',
            min_df=2,             # Giữ min_df=2 để không mất từ quan trọng
            max_df=0.9,           # Loại bỏ từ quá phổ biến
            sublinear_tf=True,
            token_pattern=r'(?u)\b\w+\b'
        )
        self.vectorizer.fit(combined_text)
        
        # Extract features với description
        X = self._extract_features(df, is_training=True)
        self.feature_names = X.columns.tolist()
        
        # Encode labels
        self.label_encoder = LabelEncoder()
        y = self.label_encoder.fit_transform(df['category'])
        
        # Split data
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y
        )
        
        print(f"📦 Training set: {len(X_train)}, Test set: {len(X_test)}")
        
        # Train model - Tối ưu RandomForest: Giảm RAM nhưng giữ confidence cao
        print("🌲 Training Random Forest Classifier (Optimized)...")
        self.model = RandomForestClassifier(
            n_estimators=150,           # Giảm xuống 150 trees để tiết kiệm RAM
            max_depth=None,             # Không giới hạn depth, để model học sâu
            min_samples_split=2,        # Minimum để capture patterns tốt nhất
            min_samples_leaf=1,         # Minimum để fit tốt nhất
            max_features='sqrt',        # sqrt(n_features) cho mỗi split
            class_weight='balanced',    # Cân bằng classes
            random_state=42,
            n_jobs=-1,                  # Dùng tất cả CPU cores
            bootstrap=True,
            oob_score=True,             # Out-of-bag score để đánh giá
            warm_start=False,
            criterion='gini',           # Gini impurity
            max_samples=0.8             # Sử dụng 80% samples cho mỗi tree (giảm RAM)
        )
        
        self.model.fit(X_train, y_train)
        
        # Evaluate
        y_pred = self.model.predict(X_test)
        accuracy = accuracy_score(y_test, y_pred)
        
        print(f"\n✅ Model trained successfully!")
        print(f"🎯 Accuracy: {accuracy:.2%}")
        if hasattr(self.model, 'oob_score_'):
            print(f"🎯 OOB Score: {self.model.oob_score_:.2%}")
        
        print("\n📊 Classification Report:")
        print(classification_report(
            y_test,
            y_pred,
            target_names=self.label_encoder.classes_,
            zero_division=0
        ))
        
        # Feature importance
        feature_importance = pd.DataFrame({
            'feature': self.feature_names,
            'importance': self.model.feature_importances_
        }).sort_values('importance', ascending=False)
        
        print("\n🔝 Top 10 Important Features:")
        print(feature_importance.head(10).to_string(index=False))
        
        # Save model
        self.save()
        
        # Record training result to history
        if HAS_TRAINING_HISTORY:
            try:
                # Calculate detailed metrics
                precision = precision_score(y_test, y_pred, average='weighted', zero_division=0)
                recall = recall_score(y_test, y_pred, average='weighted', zero_division=0)
                f1 = f1_score(y_test, y_pred, average='weighted', zero_division=0)
                
                record_training(
                    model_name="Category Classification",
                    accuracy=accuracy * 100,  # Convert to percentage
                    metrics={
                        "precision": round(precision * 100, 2),
                        "recall": round(recall * 100, 2),
                        "f1_score": round(f1 * 100, 2),
                        "test_samples": len(X_test),
                        "train_samples": len(X_train)
                    }
                )
                print("📝 Training result recorded to history")
            except Exception as e:
                print(f"⚠️ Could not record training: {e}")
        
        return accuracy
    
    def predict(
        self,
        merchant: str,
        amount: float,
        timestamp: pd.Timestamp = None,
        top_n: int = 3
    ) -> Tuple[str, float, List[Dict]]:
        """
        Predict category for a transaction
        
        Returns:
            category: Predicted category
            confidence: Confidence score
            alternatives: List of alternative predictions
        """
        
        if self.model is None:
            self.load()
        
        # Create dataframe
        data = pd.DataFrame([{
            'merchant': merchant,
            'amount': amount,
            'timestamp': timestamp or pd.Timestamp.now()
        }])
        
        # Extract features
        X = self._extract_features(data)
        
        # Predict probabilities
        proba = self.model.predict_proba(X)[0]
        
        # Get top N predictions
        top_indices = np.argsort(proba)[-top_n:][::-1]
        
        # Main prediction
        main_idx = top_indices[0]
        category = self.label_encoder.classes_[main_idx]
        confidence = proba[main_idx]
        
        # Alternatives - chỉ lấy những alternatives có confidence đáng kể
        alternatives = [
            {
                'category': self.label_encoder.classes_[idx],
                'confidence': float(proba[idx])
            }
            for idx in top_indices[1:]
            if proba[idx] > 0.10  # Only include if confidence > 10%
        ]
        
        # Record prediction to history (for logging purposes)
        if HAS_TRAINING_HISTORY and confidence < 0.7:  # Only log low-confidence predictions
            try:
                record_prediction(
                    model_name="Category Classification",
                    user_id="system",
                    input_text=merchant,
                    predicted_category=category,
                    confidence=confidence * 100
                )
            except Exception:
                pass  # Silently fail for prediction logging
        
        return category, float(confidence), alternatives
    
    def save(self):
        """Save model to disk"""
        os.makedirs(self.model_path, exist_ok=True)
        
        joblib.dump(self.model, f"{self.model_path}/categorizer_model.pkl")
        joblib.dump(self.vectorizer, f"{self.model_path}/categorizer_vectorizer.pkl")
        joblib.dump(self.label_encoder, f"{self.model_path}/categorizer_encoder.pkl")
        joblib.dump(self.feature_names, f"{self.model_path}/categorizer_features.pkl")
        
        print(f"\n💾 Model saved to {self.model_path}/")
    
    def load(self):
        """Load model from disk"""
        try:
            self.model = joblib.load(f"{self.model_path}/categorizer_model.pkl")
            self.vectorizer = joblib.load(f"{self.model_path}/categorizer_vectorizer.pkl")
            self.label_encoder = joblib.load(f"{self.model_path}/categorizer_encoder.pkl")
            self.feature_names = joblib.load(f"{self.model_path}/categorizer_features.pkl")
            print("✅ Model loaded successfully")
        except FileNotFoundError:
            raise FileNotFoundError(
                f"Model not found in {self.model_path}/. Please train the model first."
            )


if __name__ == "__main__":
    # Train model
    categorizer = TransactionCategorizer()
    categorizer.train()
    
    # Test prediction
    print("\n🧪 Testing predictions...")
    test_cases = [
        ("GRAB", 50000),
        ("SHOPEE", 250000),
        ("HIGHLANDS COFFEE", 85000),
        ("ĐIỆN EVN", 450000),
    ]
    
    for merchant, amount in test_cases:
        category, confidence, alternatives = categorizer.predict(merchant, amount)
        print(f"\n{merchant} - {amount:,} VND")
        print(f"  → {category} ({confidence:.2%})")
        if alternatives:
            alt_text = ', '.join([f"{a['category']} ({a['confidence']:.2%})" for a in alternatives])
            print(f"  Alternatives: {alt_text}")
