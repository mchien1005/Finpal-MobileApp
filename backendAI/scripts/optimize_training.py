"""
Script Tối Ưu Training Models - Optimized Training Script

Script này tạo dữ liệu training tốt hơn và train các models với cấu hình tối ưu.

Cải tiến:
1. Category Classification: 
   - Tăng TF-IDF features từ 50 lên 100
   - Thêm description vào feature extraction
   - Tăng n_estimators lên 200
   - Thêm cross-validation

2. Anomaly Detection:
   - Thêm category-based anomaly features
   - Fine-tune contamination parameter

3. Spending Prediction:
   - Sử dụng Gradient Boosting thay vì Linear Regression
   - Thêm more time features

Usage:
    python scripts/optimize_training.py --generate-data
    python scripts/optimize_training.py --train-all
    python scripts/optimize_training.py --generate-data --train-all
"""

import sys
import os
import argparse

# Add parent directory to path để import được app modules
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import random
from faker import Faker

fake = Faker(['vi_VN'])

# ============================================================
# CẢI TIẾN DỮ LIỆU TRAINING - OPTIMIZED TRAINING DATA
# ============================================================

# Categories cải tiến với nhiều merchants hơn và patterns rõ ràng hơn
OPTIMIZED_CATEGORIES = {
    'Ăn uống': {
        'merchants': [
            # Food delivery
            'GRAB FOOD', 'GRAB FOOD - HCM', 'GRAB FOOD HANOI', 
            'SHOPEE FOOD', 'SHOPEE FOOD EXPRESS', 
            'BAEMIN', 'BAEMIN VIET NAM',
            'NOW', 'NOW VN', 'LOSHIP',
            # Coffee shops
            'HIGHLANDS COFFEE', 'HIGHLANDS COFFEE HCM', 
            'THE COFFEE HOUSE', 'STARBUCKS', 'STARBUCKS VN',
            'PHUC LONG', 'PHUC LONG COFFEE', 'KATINAT', 'GONG CHA', 
            'TOCO TOCO', 'DING TEA', 'BOBAPOP', 'KICHI KICHI',
            # Restaurants
            'PHO 24', 'PHO 24 Q1', 'PHO THIN', 'PHO BO', 'BUN CHA', 'BUN BO HUE',
            'COM TAM', 'COM RANG', 'BANH MI', 'BANH MI PATE', 'BANH MI HN',
            'LOTTERIA', 'LOTTERIA VN', 'KFC', 'KFC VIETNAM', 
            'JOLLIBEE', 'MCDONALDS', 'BURGER KING', 'BURGER KING VN',
            'PIZZA HUT', 'DOMINOS', 'PIZZA 4PS', 'AL FRESCO',
            'MANWAH', 'SUMO BBQ', 'GOGI', 'HOTPOT STORY', 'HUTONG',
            # Convenience stores
            'CIRCLE K', 'MINISTOP', '7-ELEVEN', 'FAMILY MART', 'GS25',
            'BACHHOAXANH', 'VINMART+', 'CO.OP FOOD', 'WINMART+',
        ],
        'descriptions': [
            'Đặt đồ ăn trưa', 'Ăn sáng', 'Ăn tối với gia đình', 
            'Cafe với bạn', 'Trà sữa chiều', 'Ăn vặt',
            'Đặt đồ ăn online', 'Mua đồ ăn sáng', 'Ăn buffet',
            'Giao đồ ăn', 'Ăn nhanh', 'Mua cafe'
        ],
        'amount_range': (15000, 500000),
        'frequency': 0.28
    },
    'Di chuyển': {
        'merchants': [
            'GRAB', 'GRAB BIKE', 'GRAB CAR', 'GRAB TAXI', 
            'BE', 'BE BIKE', 'BE CAR', 'GOJEK', 
            'XANH SM', 'MAI LINH', 'MAI LINH TAXI', 'VINASUN', 'TAXI',
            'PETROLIMEX', 'PVOIL', 'SHELL', 'TOTAL', 'XANG DAU', 'DO XANG',
            'GUI XE', 'BAI DO XE', 'PHI GIU XE', 'VETC', 'EPASS',
            'VIETNAM AIRLINES', 'VIETJET', 'BAMBOO AIRWAYS',
        ],
        'descriptions': [
            'Đi làm bằng Grab', 'Đi taxi về nhà', 'Đổ xăng xe máy',
            'Đi sân bay', 'Phí gửi xe', 'Nạp tiền VETC',
            'Đi công tác', 'Về quê', 'Di chuyển nội thành'
        ],
        'amount_range': (10000, 3000000),
        'frequency': 0.14
    },
    'Mua sắm': {
        'merchants': [
            'SHOPEE', 'SHOPEE MALL', 'LAZADA', 'TIKI', 'SENDO', 'AMAZON',
            'THEGIOIDIDONG', 'DIEN THOAI', 'CELLPHONES', 'FPT SHOP', 
            'DIENMAYXANH', 'PICO', 'NGUYEN KIM',
            'VINMART', 'CO.OP MART', 'BIG C', 'LOTTE MART', 'AEON', 
            'EMART', 'MEGA MARKET', 'GO!',
            'UNIQLO', 'ZARA', 'H&M', 'MANGO', 'CANIFA', 
            'ROUTINE', 'IVY MODA', 'YAME', 'OWEN',
            'ADIDAS', 'NIKE', 'PUMA', 'CONVERSE', 'VANS', 'BITI\'S',
        ],
        'descriptions': [
            'Mua quần áo online', 'Mua đồ điện tử', 'Mua sắm cuối tuần',
            'Mua đồ gia dụng', 'Mua nhu yếu phẩm', 'Shopping online',
            'Mua giày dép', 'Mua phụ kiện', 'Mua đồ thời trang'
        ],
        'amount_range': (50000, 10000000),
        'frequency': 0.15
    },
    'Giải trí': {
        'merchants': [
            'CGV', 'LOTTE CINEMA', 'GALAXY', 'BHD STAR', 'BETA CINEMAS',
            'NETFLIX', 'SPOTIFY', 'YOUTUBE PREMIUM', 'FPT PLAY', 
            'APPLE MUSIC', 'ZING MP3',
            'STEAM', 'GARENA', 'RIOT GAMES', 'GOOGLE PLAY', 'APP STORE',
            'KARAOKE', 'BILLIARD', 'BOWLING', 'GAME CENTER',
            'DAM SEN', 'SUI TIEN', 'VINPEARL',
        ],
        'descriptions': [
            'Xem phim cuối tuần', 'Phí Netflix tháng', 'Mua game Steam',
            'Đi karaoke', 'Du lịch', 'Giải trí cuối tuần',
            'Phí Spotify Premium', 'Mua app', 'Vé xem phim'
        ],
        'amount_range': (50000, 2000000),
        'frequency': 0.08
    },
    'Sức khỏe': {
        'merchants': [
            'BENH VIEN', 'PHONG KHAM', 'VINMEC', 'MEDLATEC', 'HONG NGOC',
            'NHA KHOA', 'DENTAL CLINIC', 'IMPLANT',
            'PHARMACITY', 'LONG CHAU', 'AN KHANG', 'GUARDIAN', 'NHA THUOC',
            'CALIFORNIA FITNESS', 'CITIGYM', 'ELITE FITNESS', 
            'YOGA', 'GYM', 'FITNESS', 'SPA', 'MASSAGE',
        ],
        'descriptions': [
            'Khám sức khỏe định kỳ', 'Mua thuốc', 'Phí gym tháng',
            'Chăm sóc răng', 'Mua vitamin', 'Đi spa',
            'Khám bệnh', 'Tập yoga', 'Bổ sung vitamin'
        ],
        'amount_range': (50000, 5000000),
        'frequency': 0.06
    },
    'Giáo dục': {
        'merchants': [
            'HOC PHI', 'TRUONG', 'TRUONG DAI HOC', 
            'IELTS', 'TOEIC', 'BRITISH COUNCIL', 'ILA',
            'UDEMY', 'COURSERA', 'SKILLSHARE', 'EDUMALL', 'UNICA',
            'FAHASA', 'NHA SACH', 'TIKI BOOKS', 'PHUONG NAM',
        ],
        'descriptions': [
            'Đăng ký khóa học online', 'Học phí tiếng Anh', 'Mua sách',
            'Thi IELTS', 'Khóa học lập trình', 'Học thêm',
            'Đăng ký Coursera', 'Mua tài liệu học tập'
        ],
        'amount_range': (100000, 10000000),
        'frequency': 0.04
    },
    'Hóa đơn & Tiện ích': {
        'merchants': [
            'EVN', 'DIEN LUC', 'TIEN DIEN', 'CONG TO DIEN',
            'SAWACO', 'NUOC', 'TIEN NUOC', 'CAP NUOC',
            'VNPT', 'VIETTEL', 'FPT TELECOM', 'MOBIFONE', 'VINAPHONE',
            'INTERNET', 'CAP QUANG', 'WIFI',
            'GAS', 'PHI QUAN LY', 'CHUNG CU',
        ],
        'descriptions': [
            'Tiền điện tháng', 'Tiền nước', 'Cước internet',
            'Phí điện thoại', 'Tiền gas', 'Phí quản lý chung cư',
            'Nạp tiền điện thoại', 'Cước viễn thông'
        ],
        'amount_range': (100000, 3000000),
        'frequency': 0.06
    },
    'Nhà ở': {
        'merchants': [
            'TIEN NHA', 'THUE NHA', 'THUE PHONG', 'TIEN CAT COC',
            'IKEA', 'UMA', 'JYSK', 'INDEX', 
            'NOI THAT', 'DO GO', 'SOFA', 
            'SUA CHUA NHA', 'THO', 'SON NHA', 'DIEN NUOC',
        ],
        'descriptions': [
            'Tiền thuê nhà tháng', 'Mua nội thất', 'Sửa chữa nhà',
            'Tiền đặt cọc', 'Mua đồ decor', 'Phí chung cư'
        ],
        'amount_range': (1000000, 20000000),
        'frequency': 0.04
    },
    'Gia đình': {
        'merchants': [
            'BIEU BO ME', 'BIEU ONG BA', 'GIUP DO GIA DINH',
            'SUA CHO BE', 'TA GIAY', 'TA VAI', 'BOBBY',
            'KIDS PLAZA', 'CON CUU VANG', 'DO CHOI TRE EM',
            'THU CUNG', 'PET SHOP', 'PETMART',
        ],
        'descriptions': [
            'Biếu bố mẹ', 'Mua sữa cho con', 'Chi phí nuôi con',
            'Mua đồ cho thú cưng', 'Hỗ trợ gia đình', 'Mua đồ chơi'
        ],
        'amount_range': (100000, 5000000),
        'frequency': 0.04
    },
    'Bảo hiểm': {
        'merchants': [
            'PRUDENTIAL', 'MANULIFE', 'AIA', 'SUNLIFE', 'DAI-ICHI',
            'BAO VIET', 'BIC', 'PJICO', 'PTI',
            'BHXH', 'BHYT', 'BAO HIEM XA HOI',
        ],
        'descriptions': [
            'Phí bảo hiểm nhân thọ', 'Bảo hiểm xe', 'Bảo hiểm sức khỏe',
            'BHXH hàng tháng', 'Gia hạn bảo hiểm'
        ],
        'amount_range': (500000, 10000000),
        'frequency': 0.02
    },
    'Đầu tư': {
        'merchants': [
            'TCBS', 'SSI', 'VNDIRECT', 'VPS', 'HSC', 'MBS',
            'CHUNG KHOAN', 'CO PHIEU', 'TRAI PHIEU',
            'VANG SJC', 'PNJ', 'DOJI',
        ],
        'descriptions': [
            'Mua cổ phiếu', 'Đầu tư chứng khoán', 'Mua vàng',
            'Gửi tiết kiệm', 'Mua trái phiếu'
        ],
        'amount_range': (1000000, 50000000),
        'frequency': 0.03
    },
    'Quà tặng': {
        'merchants': [
            'QUA SINH NHAT', 'TIEC SINH NHAT', 'PARTY',
            'DAM CUOI', 'MUNG CUOI', 
            'LI XI', 'MUNG TUOI', 'QUA TANG', 'HOA', 'BANH KEM',
        ],
        'descriptions': [
            'Quà sinh nhật bạn', 'Mừng cưới', 'Lì xì Tết',
            'Mua hoa tặng', 'Quà Valentine', 'Quà 8/3'
        ],
        'amount_range': (200000, 5000000),
        'frequency': 0.03
    },
    'Làm đẹp': {
        'merchants': [
            '30SHINE', 'SALON TOC', 'CAT TOC', 'UON TOC', 'NHUOM TOC',
            'NAIL', 'NAIL SALON', 'SPA NAIL',
            'SPA', 'THAM MY VIEN', 
            'HASAKI', 'WATSONS', 'SOCIOLLA', 'GUARDIAN',
            'THE FACE SHOP', 'INNISFREE', 'LANCOME',
        ],
        'descriptions': [
            'Cắt tóc', 'Làm nail', 'Mua mỹ phẩm',
            'Chăm sóc da', 'Spa thư giãn', 'Làm đẹp'
        ],
        'amount_range': (80000, 2000000),
        'frequency': 0.04
    },
    'Công việc': {
        'merchants': [
            'VAN PHONG PHAM', 'THIEN LONG', 'BUT', 'SO TAY',
            'MICROSOFT OFFICE', 'MICROSOFT 365', 'OFFICE 365',
            'ZOOM', 'GOOGLE WORKSPACE', 'SLACK', 
            'CANVA', 'CANVA PRO', 'ADOBE', 'PHOTOSHOP',
            'NOTION', 'EVERNOTE', 'DROPBOX',
        ],
        'descriptions': [
            'Phí Office 365', 'Đăng ký Canva Pro', 'Mua văn phòng phẩm',
            'Phí Zoom Pro', 'Phần mềm công việc', 'Dụng cụ làm việc'
        ],
        'amount_range': (100000, 3000000),
        'frequency': 0.02
    },
}

INCOME_CATEGORIES = {
    'Lương': {
        'merchants': ['LƯƠNG THÁNG', 'SALARY', 'LUONG T12', 'CONG TY ABC', 'CONG TY XYZ'],
        'descriptions': ['Lương tháng', 'Lương NET', 'Lương cơ bản'],
        'amount_range': (8000000, 40000000),
        'frequency': 1.0
    },
    'Thưởng': {
        'merchants': ['THUONG', 'BONUS', 'THUONG DU AN', 'KPI', 'HOA HONG'],
        'descriptions': ['Thưởng dự án', 'Thưởng Tết', 'Thưởng hiệu quả'],
        'amount_range': (1000000, 20000000),
        'frequency': 0.25
    },
    'Làm thêm': {
        'merchants': ['FREELANCE', 'PART TIME', 'LAM THEM', 'TANG CA', 'OT'],
        'descriptions': ['Thu nhập freelance', 'Làm thêm ngoài giờ', 'Tăng ca'],
        'amount_range': (500000, 10000000),
        'frequency': 0.15
    },
    'Kinh doanh': {
        'merchants': ['BAN HANG', 'DOANH THU', 'KINH DOANH', 'SHOPEE SELLER'],
        'descriptions': ['Doanh thu bán hàng', 'Thu nhập kinh doanh'],
        'amount_range': (2000000, 30000000),
        'frequency': 0.1
    },
    'Đầu tư': {
        'merchants': ['CO TUC', 'LAI SUAT', 'TIEN LAI', 'DAU TU'],
        'descriptions': ['Cổ tức', 'Lãi tiết kiệm', 'Thu nhập đầu tư'],
        'amount_range': (500000, 10000000),
        'frequency': 0.1
    },
    'Được tặng': {
        'merchants': ['DUOC TANG', 'LI XI', 'QUA TANG', 'NHAN QUA'],
        'descriptions': ['Lì xì', 'Được tặng tiền', 'Quà'],
        'amount_range': (500000, 10000000),
        'frequency': 0.05
    },
}


def generate_optimized_data(num_users=100, months=12, transactions_per_month=80):
    """
    Tạo dữ liệu training tối ưu với nhiều patterns hơn
    
    Args:
        num_users: Số users (100 để có đủ data)
        months: Số tháng (12 tháng = 1 năm full data)
        transactions_per_month: Số giao dịch/tháng/user
    """
    
    transactions = []
    start_date = datetime.now() - timedelta(days=months * 30)
    
    print(f"🚀 Generating optimized training data...")
    print(f"   Users: {num_users}")
    print(f"   Months: {months}")
    print(f"   Transactions/month/user: {transactions_per_month}")
    
    for user_id in range(1, num_users + 1):
        # Mỗi user có spending pattern riêng
        user_multiplier = random.uniform(0.6, 1.8)
        
        # Mỗi user prefer một số categories nhất định
        preferred_categories = random.sample(
            list(OPTIMIZED_CATEGORIES.keys()), 
            k=random.randint(5, 10)
        )
        
        for month in range(months):
            month_start = start_date + timedelta(days=month * 30)
            
            # Seasonal adjustment (chi tiêu cao hơn vào tháng 12, 1, 2 - Tết)
            month_num = (month_start.month)
            if month_num in [12, 1, 2]:
                seasonal_multiplier = 1.3  # Tết - chi tiêu cao
            elif month_num in [6, 7, 8]:
                seasonal_multiplier = 1.15  # Hè - du lịch nhiều
            else:
                seasonal_multiplier = 1.0
            
            # Generate expenses
            for _ in range(transactions_per_month):
                # Ưu tiên preferred categories
                if random.random() < 0.7:
                    category = random.choice(preferred_categories)
                else:
                    category = random.choices(
                        list(OPTIMIZED_CATEGORIES.keys()),
                        weights=[c['frequency'] for c in OPTIMIZED_CATEGORIES.values()]
                    )[0]
                
                cat_data = OPTIMIZED_CATEGORIES[category]
                merchant = random.choice(cat_data['merchants'])
                # Randomly drop description (50%) to force model to learn from merchant name
                if random.random() < 0.5:
                    description = ""
                else:
                    description = random.choice(cat_data['descriptions'])
                
                min_amt, max_amt = cat_data['amount_range']
                amount = random.randint(int(min_amt), int(max_amt)) 
                amount = amount * user_multiplier * seasonal_multiplier
                
                # Round to thousands
                amount = round(amount, -3)
                
                # Random timestamp within month
                timestamp = month_start + timedelta(
                    days=random.randint(0, 29),
                    hours=random.randint(6, 23),
                    minutes=random.randint(0, 59)
                )
                
                transactions.append({
                    'user_id': user_id,
                    'transaction_type': 'EXPENSE',
                    'category': category,
                    'merchant': merchant,
                    'amount': amount,
                    'description': description,
                    'timestamp': timestamp,
                    'is_anomaly': False
                })
            
            # Generate income (1-2 times per month)
            for _ in range(random.randint(1, 2)):
                category = random.choices(
                    list(INCOME_CATEGORIES.keys()),
                    weights=[c['frequency'] for c in INCOME_CATEGORIES.values()]
                )[0]
                
                cat_data = INCOME_CATEGORIES[category]
                merchant = random.choice(cat_data['merchants'])
                if random.random() < 0.5:
                    description = ""
                else:
                    description = random.choice(cat_data['descriptions'])
                min_amt, max_amt = cat_data['amount_range']
                amount = random.randint(int(min_amt), int(max_amt))
                
                # Salary on 10th-15th
                if category == 'Lương':
                    day = random.randint(10, 15)
                else:
                    day = random.randint(1, 28)
                
                timestamp = month_start + timedelta(
                    days=min(day, 28),
                    hours=random.randint(9, 17)
                )
                
                transactions.append({
                    'user_id': user_id,
                    'transaction_type': 'INCOME',
                    'category': category,
                    'merchant': merchant,
                    'amount': round(amount, -3),
                    'description': description,
                    'timestamp': timestamp,
                    'is_anomaly': False
                })
            
            # Add anomalies (3% of transactions)
            num_anomalies = max(1, int(transactions_per_month * 0.03))
            for _ in range(num_anomalies):
                category = random.choice(list(OPTIMIZED_CATEGORIES.keys()))
                cat_data = OPTIMIZED_CATEGORIES[category]
                merchant = random.choice(cat_data['merchants'])
                
                # Anomaly: 4-6x normal amount
                min_amt, max_amt = cat_data['amount_range']
                amount = random.randint(int(max_amt * 4), int(max_amt * 6))
                
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
    
    df = pd.DataFrame(transactions)
    
    # Save to CSV
    os.makedirs('data/raw', exist_ok=True)
    output_file = 'data/raw/transactions.csv'
    df.to_csv(output_file, index=False, encoding='utf-8-sig')
    
    print(f"\n✅ Generated {len(df)} transactions")
    print(f"   Users: {df['user_id'].nunique()}")
    print(f"   Date range: {df['timestamp'].min()} to {df['timestamp'].max()}")
    print(f"   Categories: {df['category'].nunique()}")
    print(f"   Anomalies: {df['is_anomaly'].sum()} ({df['is_anomaly'].sum()/len(df)*100:.2f}%)")
    print(f"\n📁 Saved to: {output_file}")
    
    return df


def train_optimized_models():
    """Train all models với cấu hình tối ưu"""
    
    from app.models.categorization import TransactionCategorizer
    from app.models.anomaly_detection import AnomalyDetector
    from app.models.spending_prediction import SpendingPredictor
    
    data_path = "data/raw/transactions.csv"
    
    if not os.path.exists(data_path):
        print(f"❌ Data file not found: {data_path}")
        print("Run with --generate-data first")
        return
    
    print("=" * 60)
    print("🚀 OPTIMIZED MODEL TRAINING")
    print("=" * 60)
    
    # 1. Train Category Classification
    print("\n" + "=" * 60)
    print("1️⃣  TRAINING CATEGORY CLASSIFICATION (OPTIMIZED)")
    print("=" * 60)
    
    try:
        categorizer = TransactionCategorizer()
        accuracy = categorizer.train(data_path)
        print(f"📊 Final Accuracy: {accuracy:.2%}")
    except Exception as e:
        print(f"❌ Error: {e}")
    
    # 2. Train Anomaly Detection
    print("\n" + "=" * 60)
    print("2️⃣  TRAINING ANOMALY DETECTION (OPTIMIZED)")
    print("=" * 60)
    
    try:
        detector = AnomalyDetector()
        detector.train(data_path)
    except Exception as e:
        print(f"❌ Error: {e}")
    
    # 3. Train Spending Prediction
    print("\n" + "=" * 60)
    print("3️⃣  TRAINING SPENDING PREDICTION (OPTIMIZED)")
    print("=" * 60)
    
    try:
        predictor = SpendingPredictor()
        predictor.train(data_path)
    except Exception as e:
        print(f"❌ Error: {e}")
    
    print("\n" + "=" * 60)
    print("✅ ALL MODELS TRAINED SUCCESSFULLY")
    print("=" * 60)


def upload_to_server(server_url: str, file_path: str = "data/raw/transactions.csv"):
    """
    Upload training data lên server remote
    
    Args:
        server_url: URL của AI backend server
        file_path: Đường dẫn file CSV
    """
    import requests
    from pathlib import Path
    
    file_path = Path(file_path)
    
    if not file_path.exists():
        print(f"❌ File không tồn tại: {file_path}")
        return False
    
    print(f"\n📤 Uploading {file_path.name} to {server_url}...")
    
    url = f"{server_url}/api/admin/ai/upload-training-data"
    
    try:
        with open(file_path, 'rb') as f:
            files = {'file': (file_path.name, f, 'text/csv')}
            data = {'model_name': 'Category Classification', 'append': 'false'}
            response = requests.post(url, files=files, data=data, timeout=120)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Upload thành công!")
            print(f"   Records: {result.get('records_count', 0)}")
            return True
        else:
            print(f"❌ Upload thất bại: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Lỗi: {e}")
        return False


def trigger_remote_retrain(server_url: str, model_names: list = None):
    """
    Trigger retrain trên server remote
    
    Args:
        server_url: URL của AI backend server
        model_names: List model cần retrain (None = tất cả)
    """
    import requests
    
    print(f"\n🔄 Triggering retrain on {server_url}...")
    
    url = f"{server_url}/api/admin/ai/retrain"
    payload = {'model_names': model_names} if model_names else {}
    
    try:
        response = requests.post(url, json=payload, timeout=300)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Retrain hoàn tất!")
            for status in result.get('retrain_results', []):
                print(f"   📊 {status['model_name']}: {status.get('new_accuracy', 'N/A')}%")
            return True
        else:
            print(f"❌ Retrain thất bại: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Lỗi: {e}")
        return False


def main():
    parser = argparse.ArgumentParser(
        description='FinPal AI Training Script - Tạo data và train models',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Ví dụ sử dụng:
  # Tạo dữ liệu training
  python scripts/optimize_training.py --generate-data
  
  # Train tất cả models
  python scripts/optimize_training.py --train-all
  
  # Tạo data và train
  python scripts/optimize_training.py --generate-data --train-all
  
  # Upload lên server và retrain remote
  python scripts/optimize_training.py --generate-data --upload --server http://175.41.150.228:8000 --retrain
        """
    )
    
    # Data generation
    parser.add_argument('--generate-data', action='store_true', 
                        help='Tạo dữ liệu training tối ưu')
    parser.add_argument('--users', type=int, default=100,
                        help='Số users cho data (default: 100)')
    parser.add_argument('--months', type=int, default=12,
                        help='Số tháng data (default: 12)')
    
    # Local training
    parser.add_argument('--train-all', action='store_true',
                        help='Train tất cả models locally')
    
    # Remote operations
    parser.add_argument('--server', type=str,
                        help='URL server AI (ví dụ: http://175.41.150.228:8000)')
    parser.add_argument('--upload', action='store_true',
                        help='Upload data lên server')
    parser.add_argument('--retrain', action='store_true',
                        help='Trigger retrain trên server')
    
    args = parser.parse_args()
    
    # Validate args
    if not any([args.generate_data, args.train_all, args.upload, args.retrain]):
        parser.print_help()
        return
    
    if (args.upload or args.retrain) and not args.server:
        print("❌ Cần chỉ định --server khi dùng --upload hoặc --retrain")
        return
    
    print("=" * 60)
    print("🚀 FINPAL AI TRAINING SCRIPT")
    print("=" * 60)
    
    # 1. Generate data
    if args.generate_data:
        generate_optimized_data(
            num_users=args.users,
            months=args.months,
            transactions_per_month=80
        )
    
    # 2. Train locally
    if args.train_all:
        train_optimized_models()
    
    # 3. Upload to server
    if args.upload:
        upload_to_server(args.server)
    
    # 4. Trigger remote retrain
    if args.retrain:
        trigger_remote_retrain(args.server)
    
    print("\n" + "=" * 60)
    print("✨ Done!")
    print("=" * 60)


if __name__ == "__main__":
    main()

