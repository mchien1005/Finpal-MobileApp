#!/usr/bin/env python3
"""
Script Fix CSV trên Server Production - Add Missing transaction_type Column

Lỗi: Server có file transactions.csv cũ thiếu cột 'transaction_type'
→ Models không train được vì KeyError: 'transaction_type'

Script này sẽ:
1. Download CSV từ server
2. Add cột 'transaction_type' = 'EXPENSE' (hoặc 'INCOME' nếu category là income)
3. Upload lại CSV đã fix

Usage:
    python scripts/fix_server_csv.py --server http://175.41.150.228:8000
"""

import argparse
import requests
import pandas as pd
from pathlib import Path
import io


# Categories thuộc INCOME
INCOME_CATEGORIES = {
    'Lương', 'Thưởng', 'Làm thêm', 'Kinh doanh', 
    'Đầu tư', 'Được tặng', 'Cho vay', 'Khác (Thu)'
}


def download_current_csv(server_url: str) -> pd.DataFrame:
    """Download CSV hiện tại từ server"""
    
    # Thử download trực tiếp (nếu server expose file)
    # Hoặc qua admin API
    
    print(f"📥 Attempting to download current CSV from {server_url}...")
    
    # Option 1: Direct file access (nếu server có static file serving)
    try:
        response = requests.get(f"{server_url}/data/raw/transactions.csv", timeout=30)
        if response.status_code == 200:
            df = pd.read_csv(io.StringIO(response.text))
            print(f"✅ Downloaded via direct access: {len(df)} rows")
            return df
    except Exception:
        pass
    
    # Option 2: Qua admin API (nếu có endpoint export)
    try:
        response = requests.get(f"{server_url}/api/admin/export-training-data", timeout=30)
        if response.status_code == 200:
            df = pd.read_csv(io.StringIO(response.text))
            print(f"✅ Downloaded via API: {len(df)} rows")
            return df
    except Exception:
        pass
    
    print("❌ Cannot download CSV from server")
    print("   Vui lòng download manual hoặc regenerate toàn bộ file mới")
    return None


def fix_csv_add_transaction_type(df: pd.DataFrame) -> pd.DataFrame:
    """
    Add cột transaction_type vào CSV
    
    Logic:
    - Nếu category thuộc INCOME_CATEGORIES → 'INCOME'
    - Còn lại → 'EXPENSE'
    """
    
    print("\n🔧 Adding transaction_type column...")
    
    # Check nếu đã có column rồi
    if 'transaction_type' in df.columns:
        print("   ✅ Column already exists, validating values...")
        
        # Validate and fix nếu có null
        df['transaction_type'] = df.apply(
            lambda row: 'INCOME' if row['category'] in INCOME_CATEGORIES else 'EXPENSE',
            axis=1
        )
    else:
        # Add new column
        df['transaction_type'] = df['category'].apply(
            lambda cat: 'INCOME' if cat in INCOME_CATEGORIES else 'EXPENSE'
        )
        print(f"   ✅ Added transaction_type column")
    
    # Stats
    type_counts = df['transaction_type'].value_counts()
    print(f"\n   📊 Transaction types:")
    for ttype, count in type_counts.items():
        print(f"      - {ttype}: {count} ({count/len(df)*100:.1f}%)")
    
    return df


def validate_csv(df: pd.DataFrame) -> bool:
    """Validate CSV có đầy đủ columns required"""
    
    required_columns = [
        'user_id', 'transaction_type', 'category', 
        'merchant', 'amount', 'description', 'timestamp'
    ]
    
    print("\n✓ Validating CSV structure...")
    
    missing = [col for col in required_columns if col not in df.columns]
    
    if missing:
        print(f"   ❌ Missing columns: {missing}")
        return False
    
    print(f"   ✅ All required columns present: {required_columns}")
    
    # Check nulls
    null_counts = df[required_columns].isnull().sum()
    critical_nulls = null_counts[null_counts > 0]
    
    if len(critical_nulls) > 0:
        print(f"   ⚠️  Null values found:")
        for col, count in critical_nulls.items():
            print(f"      - {col}: {count} nulls")
    else:
        print(f"   ✅ No critical null values")
    
    return True


def upload_fixed_csv(server_url: str, df: pd.DataFrame) -> bool:
    """Upload CSV đã fix lên server"""
    
    print(f"\n📤 Uploading fixed CSV to {server_url}...")
    
    # Save to temporary file
    temp_file = Path("temp_fixed_transactions.csv")
    df.to_csv(temp_file, index=False, encoding='utf-8')
    
    try:
        url = f"{server_url}/api/admin/upload-training-data"
        
        with open(temp_file, 'rb') as f:
            files = {'file': ('transactions.csv', f, 'text/csv')}
            data = {
                'model_name': 'categorization',
                'append': 'false'  # Overwrite completely
            }
            
            response = requests.post(url, files=files, data=data, timeout=120)
        
        # Cleanup
        temp_file.unlink()
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Upload successful!")
            print(f"   - Records: {result.get('records_count', 0)}")
            print(f"   - Path: {result.get('target_path', 'N/A')}")
            return True
        else:
            print(f"❌ Upload failed: {response.status_code}")
            print(f"   {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Upload error: {e}")
        if temp_file.exists():
            temp_file.unlink()
        return False


def main():
    parser = argparse.ArgumentParser(description='Fix CSV trên server - add transaction_type column')
    parser.add_argument('--server', required=True, help='Server URL (e.g., http://175.41.150.228:8000)')
    parser.add_argument('--local-file', help='Use local CSV file instead of downloading from server')
    parser.add_argument('--upload', action='store_true', help='Auto upload after fixing')
    
    args = parser.parse_args()
    
    print("🔧 CSV Fix Script - Add transaction_type Column")
    print("="*60)
    
    # Load CSV
    if args.local_file:
        print(f"📁 Using local file: {args.local_file}")
        df = pd.read_csv(args.local_file)
        print(f"✅ Loaded {len(df)} rows")
    else:
        df = download_current_csv(args.server)
        if df is None:
            print("\n❌ Cannot proceed without CSV data")
            print("   Try using --local-file option:")
            print(f"   python scripts/fix_server_csv.py --server {args.server} --local-file data/raw/transactions.csv --upload")
            return
    
    # Show current columns
    print(f"\n📋 Current columns: {df.columns.tolist()}")
    
    # Fix CSV
    df_fixed = fix_csv_add_transaction_type(df)
    
    # Validate
    if not validate_csv(df_fixed):
        print("\n❌ Validation failed!")
        return
    
    # Save locally
    output_file = Path("data/raw/transactions_fixed.csv")
    output_file.parent.mkdir(parents=True, exist_ok=True)
    df_fixed.to_csv(output_file, index=False, encoding='utf-8')
    print(f"\n💾 Saved fixed CSV to: {output_file}")
    
    # Upload
    if args.upload:
        if upload_fixed_csv(args.server, df_fixed):
            print("\n🎉 CSV fixed and uploaded successfully!")
        else:
            print("\n❌ Upload failed!")
    else:
        print(f"\n💡 To upload, run:")
        print(f"   python scripts/upload_training_data.py --server {args.server} --file {output_file}")


if __name__ == "__main__":
    main()
