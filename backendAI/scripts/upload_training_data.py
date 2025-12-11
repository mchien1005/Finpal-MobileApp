#!/usr/bin/env python3
"""
Script Upload Training Data lên Server Production

Script này giúp upload file transactions.csv lên server remote qua admin API
để có thể retrain models.

Usage:
    python scripts/upload_training_data.py --server http://175.41.150.228:8000 --file data/raw/transactions.csv
"""

import argparse
import requests
from pathlib import Path


def upload_training_data(server_url: str, file_path: str, model_name: str = "categorization"):
    """
    Upload training data file lên server
    
    Args:
        server_url: URL của AI backend server (ví dụ: http://175.41.150.228:8000)
        file_path: Đường dẫn đến file CSV cần upload
        model_name: Tên model (categorization, anomaly, prediction)
    """
    import pandas as pd
    
    file_path = Path(file_path)
    
    if not file_path.exists():
        print(f"❌ File không tồn tại: {file_path}")
        return False
    
    # Validate CSV trước khi upload
    print(f"🔍 Validating CSV...")
    try:
        df = pd.read_csv(file_path, nrows=5)
        required_cols = ['user_id', 'transaction_type', 'category', 'merchant', 'amount', 'timestamp']
        missing = [col for col in required_cols if col not in df.columns]
        
        if missing:
            print(f"❌ CSV thiếu columns: {missing}")
            print(f"   Required columns: {required_cols}")
            return False
        
        print(f"✅ CSV valid - columns: {df.columns.tolist()}")
    except Exception as e:
        print(f"❌ Lỗi khi đọc CSV: {e}")
        return False
    
    print(f"📤 Uploading {file_path.name} ({file_path.stat().st_size / 1024:.2f} KB)...")
    
    # Endpoint upload
    url = f"{server_url}/api/admin/ai/upload-training-data"
    
    try:
        with open(file_path, 'rb') as f:
            files = {
                'file': (file_path.name, f, 'text/csv')
            }
            data = {
                'model_name': model_name,
                'append': 'false'  # Overwrite existing file
            }
            
            response = requests.post(url, files=files, data=data, timeout=60)
            
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Upload thành công!")
            print(f"   - Records: {result.get('records_count', 0)}")
            print(f"   - File: {result.get('target_path', 'N/A')}")
            return True
        else:
            print(f"❌ Upload thất bại: {response.status_code}")
            print(f"   {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        print(f"❌ Không thể kết nối đến server: {server_url}")
        print("   Kiểm tra xem server có đang chạy không?")
        return False
    except Exception as e:
        print(f"❌ Lỗi: {e}")
        return False


def trigger_retrain(server_url: str, model_names: list = None):
    """
    Trigger retrain models sau khi upload data
    
    Args:
        server_url: URL của AI backend server
        model_names: List tên models cần retrain (None = retrain all)
    """
    
    print(f"\n🔄 Triggering retrain...")
    
    url = f"{server_url}/api/admin/ai/retrain"
    
    payload = {}
    if model_names:
        payload['model_names'] = model_names
    
    try:
        response = requests.post(url, json=payload, timeout=300)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Retrain hoàn tất!")
            
            for status in result.get('retrain_results', []):
                print(f"\n   📊 {status['model_name']}:")
                print(f"      Status: {status['status']}")
                print(f"      Old accuracy: {status.get('old_accuracy', 'N/A')}%")
                print(f"      New accuracy: {status.get('new_accuracy', 'N/A')}%")
                print(f"      Duration: {status.get('duration_seconds', 'N/A')}s")
            
            return True
        else:
            print(f"❌ Retrain thất bại: {response.status_code}")
            print(f"   {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Lỗi khi retrain: {e}")
        return False


def main():
    parser = argparse.ArgumentParser(description='Upload training data và retrain models')
    parser.add_argument('--server', required=True, help='Server URL (ví dụ: http://175.41.150.228:8000)')
    parser.add_argument('--file', default='data/raw/transactions.csv', help='Đường dẫn file CSV')
    parser.add_argument('--model', default='categorization', choices=['categorization', 'anomaly', 'prediction'], 
                       help='Model name')
    parser.add_argument('--retrain', action='store_true', help='Auto retrain sau khi upload')
    parser.add_argument('--models', nargs='+', help='List models cần retrain (nếu --retrain được bật)')
    
    args = parser.parse_args()
    
    print(f"🚀 Upload Training Data Script")
    print(f"   Server: {args.server}")
    print(f"   File: {args.file}")
    print(f"   Model: {args.model}")
    print("="*60)
    
    # Upload file
    success = upload_training_data(args.server, args.file, args.model)
    
    if success and args.retrain:
        # Auto retrain
        trigger_retrain(args.server, args.models)
    
    print("\n✨ Done!")


if __name__ == "__main__":
    main()
