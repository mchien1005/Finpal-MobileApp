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
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
import joblib
import os
from typing import List, Tuple, Dict


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
        
    def _extract_features(self, df: pd.DataFrame) -> pd.DataFrame:
        """
        Trích xuất features từ transaction data - Extract features from transaction data
        
        Tạo features cho model từ raw transaction data:
        1. Text features: TF-IDF vectorization của merchant name (50 dimensions)
        2. Numerical features: amount, log(amount)
        3. Time features: hour, day_of_week, is_weekend, is_month_start/end
        
        Args:
            df: DataFrame chứa transaction data (merchant, amount, timestamp)
        
        Returns:
            pd.DataFrame: DataFrame chứa engineered features sẵn sàng cho model
        """
        
        # Text features từ merchant name (TF-IDF)
        # Chuyển "GRAB VIETNAM" -> vector [0.3, 0.7, 0.1, ...] (50 dims)
        merchant_tfidf = self.vectorizer.transform(df['merchant'].fillna(''))
        
        # Numerical features
        features = pd.DataFrame()
        features['amount'] = df['amount']
        features['amount_log'] = np.log1p(df['amount'])  # log transform để giảm skewness
        
        # Time features - thời gian ảnh hưởng đến category
        # Ví dụ: 7AM -> café, 12PM -> ăn trưa, 10PM -> giải trí
        if 'timestamp' in df.columns:
            df['timestamp'] = pd.to_datetime(df['timestamp'])
            features['hour'] = df['timestamp'].dt.hour
            features['day_of_week'] = df['timestamp'].dt.dayofweek  # 0=Monday, 6=Sunday
            features['day_of_month'] = df['timestamp'].dt.day
            features['is_weekend'] = (df['timestamp'].dt.dayofweek >= 5).astype(int)
            features['is_month_start'] = (df['timestamp'].dt.day <= 5).astype(int)  # Đầu tháng
            features['is_month_end'] = (df['timestamp'].dt.day >= 25).astype(int)   # Cuối tháng
        
        # Combine TF-IDF với numerical features
        tfidf_df = pd.DataFrame(
            merchant_tfidf.toarray(),
            columns=[f'merchant_{i}' for i in range(merchant_tfidf.shape[1])]
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
        
        # Initialize TF-IDF vectorizer
        # max_features=50: chỉ lấy 50 words quan trọng nhất
        # ngram_range=(1,2): unigrams + bigrams ("grab" + "grab vietnam")
        # min_df=2: bỏ words xuất hiện < 2 lần
        print("\n🔧 Extracting features...")
        self.vectorizer = TfidfVectorizer(
            max_features=50,
            ngram_range=(1, 2),
            min_df=2
        )
        self.vectorizer.fit(df['merchant'].fillna(''))
        
        # Extract features
        X = self._extract_features(df)
        self.feature_names = X.columns.tolist()
        
        # Encode labels
        self.label_encoder = LabelEncoder()
        y = self.label_encoder.fit_transform(df['category'])
        
        # Split data
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y
        )
        
        print(f"📦 Training set: {len(X_train)}, Test set: {len(X_test)}")
        
        # Train model
        print("\n🤖 Training Random Forest model...")
        self.model = RandomForestClassifier(
            n_estimators=100,
            max_depth=20,
            min_samples_split=5,
            min_samples_leaf=2,
            random_state=42,
            n_jobs=-1
        )
        
        self.model.fit(X_train, y_train)
        
        # Evaluate
        y_pred = self.model.predict(X_test)
        accuracy = accuracy_score(y_test, y_pred)
        
        print(f"\n✅ Model trained successfully!")
        print(f"🎯 Accuracy: {accuracy:.2%}")
        
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
        
        # Alternatives
        alternatives = [
            {
                'category': self.label_encoder.classes_[idx],
                'confidence': float(proba[idx])
            }
            for idx in top_indices[1:]
            if proba[idx] > 0.05  # Only include if confidence > 5%
        ]
        
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
