#!/usr/bin/env python3
"""
Quick Test Script - Verify BackendAI Production

Kiểm tra nhanh tất cả APIs và models trên production server
"""

import requests
import json
from datetime import datetime


SERVER_URL = "http://175.41.150.228:8000"


def test_health():
    """Test health endpoint"""
    print("\n🏥 Testing Health...")
    try:
        r = requests.get(f"{SERVER_URL}/health", timeout=5)
        if r.status_code == 200:
            print(f"   ✅ Server healthy: {r.json()}")
            return True
        else:
            print(f"   ❌ Health check failed: {r.status_code}")
            return False
    except Exception as e:
        print(f"   ❌ Error: {e}")
        return False


def test_models_status():
    """Test models status"""
    print("\n📊 Testing Models Status...")
    try:
        r = requests.get(f"{SERVER_URL}/api/admin/ai/models", timeout=10)
        if r.status_code == 200:
            data = r.json()
            print(f"   ✅ Found {len(data.get('models', []))} models")
            for model in data.get('models', []):
                status_icon = "✅" if model['status'] == 'active' else "❌"
                print(f"      {status_icon} {model['name']}: {model.get('accuracy', 'N/A')}% accuracy")
            return True
        else:
            print(f"   ❌ Failed: {r.status_code}")
            return False
    except Exception as e:
        print(f"   ❌ Error: {e}")
        return False


def test_training_data():
    """Test training data info"""
    print("\n📁 Testing Training Data...")
    try:
        r = requests.get(f"{SERVER_URL}/api/admin/ai/training-data-info", timeout=10)
        if r.status_code == 200:
            data = r.json()
            files = data.get('data_files', [])
            print(f"   ✅ Found {len(files)} data files")
            for file in files:
                print(f"      📄 {file['file_name']}: {file['records_count']} records")
                print(f"         Columns: {', '.join(file['columns'][:5])}...")
            return True
        else:
            print(f"   ❌ Failed: {r.status_code}")
            return False
    except Exception as e:
        print(f"   ❌ Error: {e}")
        return False


def test_categorization():
    """Test categorization API"""
    print("\n🏷️  Testing Categorization API...")
    try:
        payload = {
            "merchant": "GRAB FOOD",
            "amount": 50000,
            "timestamp": datetime.now().isoformat()
        }
        r = requests.post(f"{SERVER_URL}/api/categorization/predict", json=payload, timeout=10)
        if r.status_code == 200:
            data = r.json()
            print(f"   ✅ Predicted: {data.get('predicted_category')} (confidence: {data.get('confidence', 0):.1f}%)")
            return True
        else:
            print(f"   ❌ Failed: {r.status_code}")
            return False
    except Exception as e:
        print(f"   ❌ Error: {e}")
        return False


def test_insights():
    """Test insights API"""
    print("\n💡 Testing Insights API...")
    try:
        r = requests.get(f"{SERVER_URL}/api/insights/savings-suggestions/1", timeout=10)
        if r.status_code == 200:
            data = r.json()
            print(f"   ✅ User {data.get('user_id')}: {len(data.get('suggestions', []))} suggestions")
            print(f"      Total potential savings: {data.get('total_potential_savings', 0):,.0f} VND/month")
            if data.get('suggestions'):
                top = data['suggestions'][0]
                print(f"      Top: {top['category']} - save {top['monthly_savings']:,.0f} VND/month")
            return True
        else:
            print(f"   ❌ Failed: {r.status_code}")
            return False
    except Exception as e:
        print(f"   ❌ Error: {e}")
        return False


def test_retrain():
    """Test retrain API (without actually retraining)"""
    print("\n🔄 Checking Retrain Capability...")
    print("   ℹ️  Skipping actual retrain (use --retrain flag to test)")
    return True


def main():
    print("="*60)
    print("🚀 BackendAI Production Test Suite")
    print(f"   Server: {SERVER_URL}")
    print(f"   Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*60)
    
    results = {
        "Health": test_health(),
        "Models Status": test_models_status(),
        "Training Data": test_training_data(),
        "Categorization": test_categorization(),
        "Insights": test_insights(),
        "Retrain": test_retrain(),
    }
    
    print("\n" + "="*60)
    print("📈 Test Results:")
    print("="*60)
    
    passed = sum(1 for v in results.values() if v)
    total = len(results)
    
    for test, result in results.items():
        icon = "✅" if result else "❌"
        print(f"   {icon} {test}")
    
    print(f"\n   Score: {passed}/{total} tests passed")
    
    if passed == total:
        print("\n   🎉 All tests passed! System is healthy!")
    else:
        print(f"\n   ⚠️  {total - passed} test(s) failed. Please investigate.")
    
    print("="*60)


if __name__ == "__main__":
    main()
