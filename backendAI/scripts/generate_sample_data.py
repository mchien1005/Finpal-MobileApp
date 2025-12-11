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
        'merchants': ['GRAB FOOD', 'SHOPEE FOOD', 'BAEMIN', 'NOW', 'GOJEK FOOD',
                     'HIGHLANDS COFFEE', 'STARBUCKS', 'THE COFFEE HOUSE', 'PHUC LONG',
                     'KATINAT', 'GONG CHA', 'TOCO TOCO', 'DING TEA', 'BOBAPOP',
                     'PHO 24', 'PHO THIN', 'PHO BO', 'BUN CHA', 'BUN BO HUE',
                     'COM TAM', 'COM RANG', 'BANH MI', 'BANH MI PATE',
                     'LOTTERIA', 'KFC', 'JOLLIBEE', 'MCDONALDS', 'BURGER KING',
                     'PIZZA HUT', 'DOMINOS', 'PIZZA 4PS', 'AL FRESCO',
                     'KICHI KICHI', 'MANWAH', 'SUMO BBQ', 'GOGI', 'HOTPOT STORY',
                     'HUTONG', 'QUÁN LẨU', 'QUÁN NƯỚNG', 'NHÀ HÀNG',
                     'CIRCLE K', 'MINISTOP', '7-ELEVEN', 'FAMILY MART', 'GS25',
                     'BACHHOAXANH', 'VINMART+', 'CO.OP FOOD', 'WINMART+',
                     'QUAN AN', 'QUAN COM', 'CHE', 'SUA CHUA', 'YOGURT'],
        'amount_range': (15000, 300000),  # 15k-300k VND
        'frequency': 0.25  # 25% của tất cả giao dịch
    },
    'Di chuyển': {
        'merchants': ['GRAB', 'GRAB BIKE', 'GRAB CAR', 'BE', 'BE BIKE', 'BE CAR',
                     'GOJEK', 'XANH SM', 'MAI LINH', 'VINASUN', 'TAXI',
                     'PETROLIMEX', 'PVOIL', 'SHELL', 'TOTAL', 'XANG DAU', 'DO XANG',
                     'GUI XE', 'BAI DO XE', 'PHI GIU XE', 'VETC', 'EPASS',
                     'VEXERE', 'FUTA BUS', 'PHUONG TRANG', 'BEN XE',
                     'VIETNAM AIRLINES', 'VIETJET', 'BAMBOO AIRWAYS', 'PACIFIC AIRLINES',
                     'VE MAY BAY', 'VE TAU', 'DUONG SAT', 'XE BUYT', 'XE OM CONG NGHE'],
        'amount_range': (10000, 5000000),
        'frequency': 0.12
    },
    'Mua sắm': {
        'merchants': ['SHOPEE', 'LAZADA', 'TIKI', 'SENDO', 'AMAZON',
                     'THEGIOIDIDONG', 'CELLPHONES', 'FPT SHOP', 'DIENMAYXANH', 'PICO',
                     'VINMART', 'VINMART+', 'CO.OP MART', 'BIG C', 'LOTTE MART',
                     'AEON', 'EMART', 'MEGA MARKET', 'SATRA', 'GO!',
                     'UNIQLO', 'ZARA', 'H&M', 'MANGO', 'PULL&BEAR',
                     'CANIFA', 'ROUTINE', 'IVY MODA', 'ELISE', 'YAME', 'OWEN',
                     'NINOMAXX', 'BITI\'S', 'ANANAS', 'JUNO', 'PEDRO',
                     'ADIDAS', 'NIKE', 'PUMA', 'CONVERSE', 'VANS',
                     'CHO', 'SAN', 'CUA HANG', 'BOOK STORE'],
        'amount_range': (50000, 5000000),
        'frequency': 0.15
    },
    'Giải trí': {
        'merchants': ['CGV', 'LOTTE CINEMA', 'GALAXY', 'BHD STAR', 'BETA CINEMAS',
                     'PLATINUM', 'CINESTAR', 'MEGA GS', 'VE PHIM', 'RAP PHIM',
                     'NETFLIX', 'SPOTIFY', 'YOUTUBE PREMIUM', 'FPT PLAY', 'VTV GO',
                     'APPLE MUSIC', 'ZING MP3', 'NHACCUATUI', 'SOUNDCLOUD',
                     'STEAM', 'GARENA', 'RIOT GAMES', 'BLIZZARD', 'EA GAMES',
                     'GOOGLE PLAY', 'APP STORE', 'ITUNES', 'PLAYSTATION', 'XBOX',
                     'KARAOKE', 'KARAOKE ARIRANG', 'KARAOKE MUSIC BOX',
                     'BILLIARD', 'BOWLING', 'GAME CENTER', 'KHU VUI CHOI',
                     'DAM SEN', 'SUI TIEN', 'VINPEARL', 'BAO TANG', 'TOURIST',
                     'DU LICH', 'KHACH SAN', 'RESORT'],
        'amount_range': (50000, 2000000),
        'frequency': 0.08
    },
    'Sức khỏe': {
        'merchants': ['BENH VIEN', 'PHONG KHAM', 'VINMEC', 'MEDLATEC', 'HONG NGOC',
                     'COLUMBIA ASIA', 'FAMILY MEDICAL', 'TRIEN VONG', 'Y HOC CO TRUYEN',
                     'NHA KHOA', 'NHAN TAM', 'DENTAL CLINIC', 'IMPLANT',
                     'PHARMACITY', 'LONG CHAU', 'AN KHANG', 'GUARDIAN', 'MEDICARE',
                     'PHANO', 'NHA THUOC', 'THUOC', 'KHAM BENH',
                     'CALIFORNIA FITNESS', 'CITIGYM', 'ELITE FITNESS', 'YOGA+',
                     'YOGA', 'FITNESS', 'GYM', 'PILATES', 'BOXING',
                     'SPA', 'MASSAGE', 'THAM MY VIEN', 'SKINCARE', 'THU GIAN'],
        'amount_range': (50000, 5000000),
        'frequency': 0.05
    },
    'Giáo dục': {
        'merchants': ['HOC PHI', 'TRUONG', 'TRUONG DAI HOC', 'TRUONG MN', 'TRUONG CAP 1',
                     'IELTS', 'TOEIC', 'TOEFL', 'IIG', 'BRITISH COUNCIL',
                     'TIENG ANH', 'ENGLISH CENTER', 'APOLLO', 'ILA', 'WALL STREET',
                     'UDEMY', 'COURSERA', 'SKILLSHARE', 'EDUMALL', 'UNICA',
                     'FAHASA', 'NHA SACH', 'TIKI BOOKS', 'PHUONG NAM', 'MINH KHAI',
                     'KHOA HOC', 'LUYEN THI', 'HUAN LUYEN', 'HOC THEM'],
        'amount_range': (100000, 10000000),
        'frequency': 0.04
    },
    'Hóa đơn & Tiện ích': {
        'merchants': ['EVN', 'DIEN LUC', 'TIEN DIEN', 'CONG TO DIEN',
                     'SAWACO', 'NUOC', 'TIEN NUOC', 'CAP NUOC',
                     'VNPT', 'VIETTEL', 'FPT TELECOM', 'MOBIFONE', 'VINAPHONE',
                     'INTERNET', 'CAP QUANG', 'WIFI', 'CUOC INTERNET',
                     'NAP TIEN', 'NAP THE', 'TRA TRUOC', 'TRA SAU',
                     'GAS', 'PHI QUAN LY', 'CHUNG CU', 'NHA O', 'VE SINH',
                     'BAO VE', 'DU', 'PHI DICH VU'],
        'amount_range': (100000, 3000000),
        'frequency': 0.06
    },
    'Nhà ở': {
        'merchants': ['TIEN NHA', 'THUE NHA', 'THUE PHONG', 'TIEN CAT COC',
                     'IKEA', 'UMA', 'JYSK', 'INDEX', 'NHAT TIN', 'DOSI-IN',
                     'NOI THAT', 'DO GO', 'SOFA', 'GIUONG', 'BAN GHE',
                     'SUA CHUA NHA', 'THO', 'SON NHA', 'SUA ONG NUOC', 'DIEN NUOC',
                     'VAT LIEU XAY DUNG', 'XIU PHONG', 'SAT THEP'],
        'amount_range': (1000000, 20000000),
        'frequency': 0.03
    },
    'Gia đình': {
        'merchants': ['BIEU BO ME', 'BIEU ONG BA', 'GIUP DO GIA DINH',
                     'NUOI CON', 'SUA CHO BE', 'TA GIAY', 'TA VAI', 'BOBBY',
                     'DO CHOI TRE EM', 'TOYKINGDOM', 'KIDS PLAZA', 'CON CAU VANG',
                     'THU CUNG', 'PET SHOP', 'PETMART', 'CHO', 'MEO',
                     'THUC AN THU CUNG', 'KHAM THU Y', 'PET SPA',
                     'TAM BE', 'BAP BE', 'QUẦN ÁO TRẺ EM'],
        'amount_range': (100000, 3000000),
        'frequency': 0.04
    },
    'Bảo hiểm': {
        'merchants': ['BHXH', 'BHYT', 'BAO HIEM XA HOI', 'BAO HIEM Y TE',
                     'PRUDENTIAL', 'MANULIFE', 'AIA', 'SUNLIFE', 'DAI-ICHI',
                     'GENERALI', 'PJICO', 'BIC', 'BAO VIET', 'PTI',
                     'BAO HIEM NHAN THO', 'BAO HIEM XE', 'BAO HIEM NHA',
                     'BAO HIEM SAC KHOE', 'BAO HIEM DU LICH'],
        'amount_range': (500000, 10000000),
        'frequency': 0.02
    },
    'Đầu tư': {
        'merchants': ['TCBS', 'SSI', 'VNDIRECT', 'VPS', 'HSC', 'VCBS', 'MBS',
                     'CHUNG KHOAN', 'CO PHIEU', 'TRAI PHIEU', 'QUY DAU TU',
                     'CRYPTO', 'BINANCE', 'REMITANO', 'BITCOIN', 'ETHEREUM',
                     'VANG SJC', 'PNJ', 'DOJI', 'BAO TIN MINH CHAU',
                     'TIET KIEM', 'GUI TIET KIEM', 'SO TIET KIEM', 'LAI SUAT',
                     'DAU TU BAT DONG SAN', 'NHA DAT', 'FOREX'],
        'amount_range': (1000000, 100000000),
        'frequency': 0.03
    },
    'Quà tặng': {
        'merchants': ['QUA SINH NHAT', 'TIEC SINH NHAT', 'PARTY',
                     'DAM CUOI', 'TIEC CUOI', 'MUNG CUOI', 'PHU DAU',
                     'LE TET', 'TET NGUYEN DAN', 'LI XI', 'MUNG TUOI',
                     'QUA TANG', 'QUA LUU NIEM', 'HOA', 'BANH KEM',
                     'THAM BENH', 'MUNG', 'CHUC MUNG', 'GIUONG HOP',
                     'QUA VALENTINE', 'QUA 8/3', 'QUA 20/10'],
        'amount_range': (200000, 5000000),
        'frequency': 0.03
    },
    'Công việc': {
        'merchants': ['VAN PHONG PHAM', 'THIEN LONG', 'BUT', 'SO TAY', 'GIAY IN',
                     'MICROSOFT OFFICE', 'MICROSOFT 365', 'OFFICE 365', 'WINDOWS',
                     'ZOOM', 'GOOGLE WORKSPACE', 'SLACK', 'TEAMS', 'SKYPE',
                     'CANVA', 'CANVA PRO', 'ADOBE', 'PHOTOSHOP', 'ILLUSTRATOR',
                     'NOTION', 'EVERNOTE', 'DROPBOX', 'GOOGLE DRIVE',
                     'HOP', 'HOI NGHI', 'AN TRUA CONG TY', 'TEAM BUILDING'],
        'amount_range': (100000, 3000000),
        'frequency': 0.02
    },
    'Làm đẹp': {
        'merchants': ['30SHINE', 'SALON TOC', 'CAT TOC', 'UON TOC', 'NHUOM TOC',
                     'NAIL', 'NAIL SALON', 'SPA NAIL', 'TIỆM NAIL',
                     'SPA', 'THAM MY VIEN', 'CHĂM SÓC DA', 'CHAM SAC BODY',
                     'MY PHAM', 'HASAKI', 'WATSONS', 'SOCIOLLA', 'BEAUTYTALK',
                     'THE FACE SHOP', 'INNISFREE', 'NATURE REPUBLIC', 'LANEIGE',
                     'GUARDIAN', 'OLIVE YOUNG', 'MAC', 'LANCOME', 'L\'OREAL',
                     'NHO LONG MAY', 'PHAT CHAM', 'THAM MY'],
        'amount_range': (100000, 2000000),
        'frequency': 0.04
    },
    'Khác (Chi)': {
        'merchants': ['ATM RÚT TIỀN', 'RUT TIEN', 'WITHDRAWAL',
                     'CHUYỂN KHOẢN', 'TRANSFER', 'CHUYEN TIEN',
                     'GỬI TIỀN', 'NOP TIEN', 'PAYMENT',
                     'CHI KHAC', 'PHI GIAO DICH', 'PHI ATM', 'PHI CHUYEN KHOAN',
                     'VAY', 'TRA NO', 'NO', 'CREDIT'],
        'amount_range': (50000, 10000000),
        'frequency': 0.04
    }
}

INCOME_CATEGORIES = {
    'Lương': {
        'merchants': ['LƯƠNG THÁNG', 'CÔNG TY ABC', 'CÔNG TY XYZ', 'SALARY'],
        'amount_range': (8000000, 30000000),  # 8M-30M VND/tháng
        'frequency': 1.0  # 100% users có lương
    },
    'Thưởng': {
        'merchants': ['THƯỞNG THÁNG', 'THƯỞNG DỰ ÁN', 'THƯỞNG HIỆU QUẢ', 'BONUS', 
                     'KPI', 'HOA HONG'],
        'amount_range': (1000000, 10000000),
        'frequency': 0.3  # 30% chance mỗi tháng
    },
    'Làm thêm': {
        'merchants': ['FREELANCE', 'PART TIME', 'LAM THEM', 'TANG CA', 'OT'],
        'amount_range': (500000, 5000000),
        'frequency': 0.2  # 20% chance mỗi tháng
    },
    'Kinh doanh': {
        'merchants': ['BAN HANG', 'DOANH THU', 'KHACH HANG', 'KINH DOANH'],
        'amount_range': (2000000, 20000000),
        'frequency': 0.15  # 15% users có thu nhập kinh doanh
    },
    'Đầu tư': {
        'merchants': ['CO TUC', 'LAI SUAT', 'TIEN LAI', 'LAI', 'DAU TU'],
        'amount_range': (500000, 10000000),
        'frequency': 0.1  # 10% chance mỗi tháng
    },
    'Cho vay': {
        'merchants': ['THU NO', 'TRA NO', 'HOAN TIEN', 'REFUND'],
        'amount_range': (1000000, 10000000),
        'frequency': 0.05  # 5% chance
    },
    'Được tặng': {
        'merchants': ['DUOC TANG', 'NHAN QUA', 'LI XI', 'QUA TANG'],
        'amount_range': (200000, 5000000),
        'frequency': 0.08  # 8% chance
    },
    'Khác (Thu)': {
        'merchants': ['THU NHAP KHAC', 'KHAC'],
        'amount_range': (100000, 5000000),
        'frequency': 0.05
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
