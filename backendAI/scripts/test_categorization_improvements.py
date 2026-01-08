"""
Script Test Cải Tiến Category Classification

Script này test các cải tiến confidence scores của model categorization
"""

import sys
import os
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

import pandas as pd
from app.models.categorization import TransactionCategorizer

def test_predictions():
    """Test predictions với các merchant khác nhau"""
    
    print("=" * 60)
    print("🧪 TESTING CATEGORY CLASSIFICATION IMPROVEMENTS")
    print("=" * 60)
    
    # Load model
    categorizer = TransactionCategorizer()
    try:
        categorizer.load()
    except FileNotFoundError:
        print("❌ Model chưa được train. Vui lòng chạy training trước:")
        print("   python scripts/optimize_training.py --generate-data --train-all")
        return
    
    # Test cases phân loại rõ ràng
    test_cases = [
        # Ăn uống
        ("GRAB FOOD", 85000, "Ăn uống"),
        ("HIGHLANDS COFFEE", 55000, "Ăn uống"),
        ("PHO 24", 45000, "Ăn uống"),
        ("KFC VIETNAM", 125000, "Ăn uống"),
        ("THE COFFEE HOUSE", 65000, "Ăn uống"),
        
        # Di chuyển
        ("GRAB BIKE", 35000, "Di chuyển"),
        ("PETROLIMEX", 250000, "Di chuyển"),
        ("VETC", 100000, "Di chuyển"),
        ("BE CAR", 85000, "Di chuyển"),
        
        # Mua sắm
        ("SHOPEE", 350000, "Mua sắm"),
        ("LAZADA", 450000, "Mua sắm"),
        ("THEGIOIDIDONG", 5500000, "Mua sắm"),
        ("UNIQLO", 850000, "Mua sắm"),
        
        # Giải trí
        ("NETFLIX", 180000, "Giải trí"),
        ("CGV", 150000, "Giải trí"),
        ("SPOTIFY", 59000, "Giải trí"),
        
        # Hóa đơn
        ("EVN DIEN LUC", 450000, "Hóa đơn & Tiện ích"),
        ("VIETTEL", 200000, "Hóa đơn & Tiện ích"),
        ("SAWACO NUOC", 120000, "Hóa đơn & Tiện ích"),
        
        # Sức khỏe
        ("PHARMACITY", 180000, "Sức khỏe"),
        ("CALIFORNIA FITNESS", 1200000, "Sức khỏe"),
        
        # Làm đẹp
        ("30SHINE", 120000, "Làm đẹp"),
        ("HASAKI", 450000, "Làm đẹp"),
    ]
    
    print("\n📊 Testing Predictions:\n")
    
    correct = 0
    total = len(test_cases)
    low_confidence_count = 0
    confidence_scores = []
    
    for merchant, amount, expected_category in test_cases:
        category, confidence, alternatives = categorizer.predict(merchant, amount)
        
        confidence_scores.append(confidence)
        is_correct = category == expected_category
        if is_correct:
            correct += 1
        
        if confidence < 0.70:
            low_confidence_count += 1
        
        # Display result
        status = "✅" if is_correct else "❌"
        conf_status = "🟢" if confidence >= 0.80 else "🟡" if confidence >= 0.60 else "🔴"
        
        print(f"{status} {conf_status} {merchant:25s} {amount:>10,} VND")
        print(f"   Predicted: {category:20s} (confidence: {confidence:.1%})")
        if not is_correct:
            print(f"   Expected:  {expected_category}")
        if alternatives:
            alt_text = ', '.join([f"{a['category']} ({a['confidence']:.1%})" for a in alternatives])
            print(f"   Alternatives: {alt_text}")
        print()
    
    # Summary
    accuracy = correct / total
    avg_confidence = sum(confidence_scores) / len(confidence_scores)
    
    print("=" * 60)
    print("📈 SUMMARY")
    print("=" * 60)
    print(f"Accuracy:         {accuracy:.1%} ({correct}/{total})")
    print(f"Avg Confidence:   {avg_confidence:.1%}")
    print(f"Low Confidence:   {low_confidence_count}/{total} ({low_confidence_count/total:.1%})")
    print()
    
    # Confidence distribution
    high_conf = sum(1 for c in confidence_scores if c >= 0.80)
    medium_conf = sum(1 for c in confidence_scores if 0.60 <= c < 0.80)
    low_conf = sum(1 for c in confidence_scores if c < 0.60)
    
    print("Confidence Distribution:")
    print(f"  🟢 High (≥80%):    {high_conf}/{total} ({high_conf/total:.1%})")
    print(f"  🟡 Medium (60-80%): {medium_conf}/{total} ({medium_conf/total:.1%})")
    print(f"  🔴 Low (<60%):     {low_conf}/{total} ({low_conf/total:.1%})")
    print()
    
    if avg_confidence >= 0.80 and accuracy >= 0.85:
        print("✅ EXCELLENT! Model có confidence cao và accuracy tốt")
    elif avg_confidence >= 0.70 and accuracy >= 0.75:
        print("✅ GOOD! Model hoạt động tốt")
    else:
        print("⚠️  Model cần cải thiện thêm")
        print("   Suggestions:")
        print("   1. Tăng số lượng training data")
        print("   2. Thêm nhiều merchant patterns vào OPTIMIZED_CATEGORIES")
        print("   3. Retrain model với data mới")


if __name__ == "__main__":
    test_predictions()
