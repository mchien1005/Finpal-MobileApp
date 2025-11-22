"""
Generate Synthetic Transaction Data - Tạo Dữ Liệu Giao Dịch Mẫu

Script này tạo dữ liệu giao dịch mẫu (synthetic data) để train ML models.
Data bao gồm income/expense transactions với các patterns thực tế.

Usage:
    python scripts/generate_sample_data.py

Output:
    - data/raw/transactions.csv: File CSV chứa giao dịch mẫu
    - Bao gồm ~10 users, 12 tháng data, 50+ transactions/tháng/user
"""

import pandas as pd
import numpy as np
from faker import Faker
from datetime import datetime, timedelta
import random
import os

fake = Faker(['vi_VN'])  # Sử dụng Vietnamese locale

# Vietnamese categories and merchants - Danh mục và merchants Việt Nam
CATEGORIES = {
    'Ăn uống': {
        'merchants': ['GRAB FOOD', 'SHOPEE FOOD', 'HIGHLANDS COFFEE', 'STARBUCKS', 
                     'PHO 24', 'LOTTERIA', 'KFC', 'JOLLIBEE', 'THE COFFEE HOUSE',
                     'BÚN CHẢ HÀNG THAN', 'CƠM TẤM SƯƠ̛N', 'PHỞ BÒ'],
        'amount_range': (15000, 200000),  # 15k-200k VND
        'frequency': 0.30  # 30% của tất cả giao dịch
    },
    'Di chuyển': {
        'merchants': ['GRAB', 'BE', 'GOJEK', 'XĂNG PETROLIMEX', 'CỬA HÀNG XE MÁY',
                     'BẾN XE MIỀN ĐÔNG', 'VÉ TÀU HỎA'],
        'amount_range': (10000, 500000),
        'frequency': 0.15
    },
    'Mua sắm': {
        'merchants': ['SHOPEE', 'LAZADA', 'TIKI', 'SENDO', 'VINMART', 'CO.OP MART',
                     'BIG C', 'LOTTE MART', 'CIRCLE K', 'FAMILY MART'],
        'amount_range': (50000, 2000000),
        'frequency': 0.20
    },
    'Giải trí': {
        'merchants': ['CGV CINEMA', 'LOTTE CINEMA', 'GALAXY CINEMA', 'SPOTIFY',
                     'NETFLIX', 'YOUTUBE PREMIUM', 'GAME LIEN QUAN'],
        'amount_range': (50000, 500000),
        'frequency': 0.10
    },
    'Hóa đơn': {
        'merchants': ['ĐIỆN EVN', 'NƯỚC SAWACO', 'INTERNET VIETTEL', 'INTERNET FPT',
                     'GAS PETROLIMEX', 'PHÍ QUẢN LÝ CHUNG CƯ'],
        'amount_range': (100000, 1500000),
        'frequency': 0.08
    },
    'Sức khỏe': {
        'merchants': ['PHARMACITY', 'GUARDIAN', 'BỆNH VIỆN ĐA KHOA', 'NHA KHOA KIM',
                     'PHÒNG KHÁM FAMILY MEDICAL'],
        'amount_range': (50000, 3000000),
        'frequency': 0.05
    },
    'Giáo dục': {
        'merchants': ['HỌC PHÍ ĐẠI HỌC', 'COURSERA', 'UDEMY', 'NHÀ SÁCH FAHASA',
                     'TRUNG TÂM ANH NGỮ'],
        'amount_range': (100000, 5000000),
        'frequency': 0.05
    },
    'Làm đẹp': {
        'merchants': ['SALON TÓC', 'SPA', 'GUARDIAN', 'THE FACE SHOP', 'INNISFREE'],
        'amount_range': (100000, 1000000),
        'frequency': 0.04
    },
    'Chi tiêu khác': {
        'merchants': ['ATM RÚT TIỀN', 'CHUYỂN KHOẢN', 'GỬI TIỀN'],
        'amount_range': (50000, 5000000),
        'frequency': 0.03
    }
}

INCOME_CATEGORIES = {
    'Lương': {
        'merchants': ['LƯƠNG THÁNG', 'CÔNG TY ABC', 'CÔNG TY XYZ'],
        'amount_range': (8000000, 30000000),  # 8M-30M VND/tháng
        'frequency': 1.0  # 100% users có lương
    },
    'Thưởng': {
        'merchants': ['THƯỞNG THÁNG', 'THƯỞNG DỰ ÁN', 'THƯỞNG HIỆU QUẢ'],
        'amount_range': (1000000, 10000000),
        'frequency': 0.3  # 30% chance mỗi tháng
    }
}


def generate_transactions(num_users=10, months=12, transactions_per_month=50):
    """
    Generate synthetic transaction data - Tạo dữ liệu giao dịch mẫu
    
    Tạo realistic transaction data với patterns:
    - User spending habits (mỗi user có thói quen riêng)
    - Seasonal patterns (chi tiêu cao hơn vào tết, lễ)
    - Anomalies (5% transactions có amount bất thường)
    - Income patterns (lương vào ngày 10-15 hàng tháng)
    
    Args:
        num_users: Số lượng users (default: 10)
        months: Số tháng historical data (default: 12)
        transactions_per_month: Số giao dịch/tháng/user (default: 50)
    
    Returns:
        pd.DataFrame: DataFrame chứa tất cả transactions
    """
    
    transactions = []
    start_date = datetime.now() - timedelta(days=months * 30)
    
    for user_id in range(1, num_users + 1):
        # User spending pattern (một số user chi tiêu nhiều hơn)
        user_multiplier = random.uniform(0.7, 1.5)
        
        for month in range(months):
            month_start = start_date + timedelta(days=month * 30)
            
            # Generate expenses
            for _ in range(transactions_per_month):
                # Select category based on frequency (weighted random)
                category = random.choices(
                    list(CATEGORIES.keys()),
                    weights=[c['frequency'] for c in CATEGORIES.values()]
                )[0]
                
                merchant = random.choice(CATEGORIES[category]['merchants'])
                min_amt, max_amt = CATEGORIES[category]['amount_range']
                amount = random.randint(int(min_amt), int(max_amt)) * user_multiplier
                
                # Random timestamp within month
                timestamp = month_start + timedelta(
                    days=random.randint(0, 29),
                    hours=random.randint(6, 23),
                    minutes=random.randint(0, 59)
                )
                
                # Add some noise to descriptions
                descriptions = [
                    f"{merchant}",
                    f"Mua hàng tại {merchant}",
                    f"Thanh toán {merchant}",
                    f"{merchant} - {timestamp.strftime('%d/%m')}"
                ]
                
                transactions.append({
                    'user_id': user_id,
                    'transaction_type': 'EXPENSE',
                    'category': category,
                    'merchant': merchant,
                    'amount': round(amount, -3),  # Round to thousands
                    'description': random.choice(descriptions),
                    'timestamp': timestamp,
                    'is_anomaly': False
                })
            
            # Generate income (1-2 times per month)
            for _ in range(random.randint(1, 2)):
                category = random.choices(
                    list(INCOME_CATEGORIES.keys()),
                    weights=[c['frequency'] for c in INCOME_CATEGORIES.values()]
                )[0]
                
                merchant = random.choice(INCOME_CATEGORIES[category]['merchants'])
                min_amt, max_amt = INCOME_CATEGORIES[category]['amount_range']
                amount = random.randint(int(min_amt), int(max_amt))
                
                # Salary typically comes on 10th-15th
                if category == 'Lương':
                    day = random.randint(10, 15)
                else:
                    day = random.randint(1, 28)
                
                timestamp = month_start + timedelta(
                    days=day,
                    hours=random.randint(9, 17)
                )
                
                transactions.append({
                    'user_id': user_id,
                    'transaction_type': 'INCOME',
                    'category': category,
                    'merchant': merchant,
                    'amount': round(amount, -3),
                    'description': merchant,
                    'timestamp': timestamp,
                    'is_anomaly': False
                })
            
            # Add some anomalies (5% of transactions)
            num_anomalies = int(transactions_per_month * 0.05)
            for _ in range(num_anomalies):
                category = random.choice(list(CATEGORIES.keys()))
                merchant = random.choice(CATEGORIES[category]['merchants'])
                
                # Anomaly: very high amount
                min_amt, max_amt = CATEGORIES[category]['amount_range']
                amount = random.randint(int(max_amt * 3), int(max_amt * 5))
                
                timestamp = month_start + timedelta(
                    days=random.randint(0, 29),
                    hours=random.randint(0, 23)
                )
                
                transactions.append({
                    'user_id': user_id,
                    'transaction_type': 'EXPENSE',
                    'category': category,
                    'merchant': merchant,
                    'amount': round(amount, -3),
                    'description': f"GIAO DỊCH BẤT THƯỜNG - {merchant}",
                    'timestamp': timestamp,
                    'is_anomaly': True
                })
    
    return pd.DataFrame(transactions)


def main():
    """Main function"""
    print("🚀 Generating synthetic transaction data...")
    
    # Generate data
    df = generate_transactions(
        num_users=50,
        months=12,
        transactions_per_month=100
    )
    
    # Create directories
    os.makedirs('data/raw', exist_ok=True)
    os.makedirs('data/processed', exist_ok=True)
    os.makedirs('data/models', exist_ok=True)
    
    # Save raw data
    output_file = 'data/raw/transactions.csv'
    df.to_csv(output_file, index=False, encoding='utf-8-sig')
    
    # Statistics
    print(f"\n✅ Generated {len(df)} transactions")
    print(f"📊 Statistics:")
    print(f"   - Users: {df['user_id'].nunique()}")
    print(f"   - Date range: {df['timestamp'].min()} to {df['timestamp'].max()}")
    print(f"   - Categories: {df['category'].nunique()}")
    print(f"   - Anomalies: {df['is_anomaly'].sum()} ({df['is_anomaly'].sum()/len(df)*100:.2f}%)")
    print(f"\n📁 Saved to: {output_file}")
    
    # Show sample
    print(f"\n📋 Sample data:")
    print(df.head(10).to_string())
    
    # Category distribution
    print(f"\n📈 Category distribution:")
    print(df['category'].value_counts())


if __name__ == "__main__":
    main()
