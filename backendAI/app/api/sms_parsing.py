"""
API endpoint cho SMS parsing bằng AI
Phân tích tin nhắn SMS ngân hàng và trích xuất thông tin giao dịch
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional
import re
from datetime import datetime
import google.generativeai as genai
from app.config import settings

router = APIRouter()

# Cấu hình Google Gemini AI
if settings.GEMINI_API_KEY:
    genai.configure(api_key=settings.GEMINI_API_KEY)


class SMSParseRequest(BaseModel):
    """Request body để parse SMS"""
    sms_content: str
    sender: Optional[str] = None


class SMSParseResponse(BaseModel):
    """Response chứa thông tin giao dịch đã parse"""
    success: bool
    data: Optional[dict] = None
    error: Optional[str] = None


# Danh sách các ngân hàng Việt Nam phổ biến
VIETNAM_BANKS = {
    "VCB": ["Vietcombank", "VCB"],
    "TCB": ["Techcombank", "TCB"],
    "BIDV": ["BIDV"],
    "ACB": ["ACB"],
    "MBB": ["MB Bank", "MBBank", "MB"],
    "CTG": ["Vietinbank", "CTG", "VietinBank"],
    "VPB": ["VPBank", "VP Bank"],
    "TPB": ["TPBank", "TP Bank", "Tien Phong Bank"],
    "SCB": ["SCB", "Sacombank"],
    "SHB": ["SHB", "Saigon Hanoi Bank"],
    "HDB": ["HDBank", "HD Bank"],
    "OCB": ["OCB", "Orient Commercial Bank"],
    "MSB": ["MSB", "Maritime Bank"],
    "VIB": ["VIB", "Vietnam International Bank"],
    "SeABank": ["SeABank", "SeA Bank"],
    "ABB": ["ABBank", "An Binh Bank"],
    "NAB": ["Nam A Bank", "NAB"],
    "EIB": ["Eximbank", "EIB"],
    "NCB": ["NCB", "National Citizen Bank"],
    "PGB": ["PG Bank", "PGB"],
    "BVB": ["Bao Viet Bank", "BVB"],
    "AGRIBANK": ["Agribank", "VBA"],
}


def detect_bank_code(sms_content: str, sender: str = None) -> str:
    """Phát hiện mã ngân hàng từ nội dung SMS hoặc sender"""
    sms_upper = sms_content.upper()
    
    for code, names in VIETNAM_BANKS.items():
        for name in names:
            if name.upper() in sms_upper:
                return code
    
    # Nếu không tìm được từ nội dung, thử từ sender
    if sender:
        sender_upper = sender.upper()
        for code, names in VIETNAM_BANKS.items():
            for name in names:
                if name.upper() in sender_upper:
                    return code
    
    return "UNKNOWN"


def parse_with_regex(sms_content: str) -> Optional[dict]:
    """
    Thử parse SMS bằng các regex patterns phổ biến
    Trả về None nếu không match
    """
    result = {}
    
    # Pattern 1: Số tiền với dấu + hoặc -
    amount_pattern = r'([+-])\s*([\d,\.]+)\s*(?:VND|đ|VNĐ|dong)'
    amount_match = re.search(amount_pattern, sms_content, re.IGNORECASE)
    if amount_match:
        result['type'] = 'INCOME' if amount_match.group(1) == '+' else 'EXPENSE'
        amount_str = amount_match.group(2).replace(',', '').replace('.', '')
        result['amount'] = amount_str
    
    # Pattern 2: Số tiền không có dấu (cần xác định từ context)
    if 'amount' not in result:
        amount_pattern2 = r'(\d{1,3}(?:[,\.]\d{3})+|\d+)\s*(?:VND|đ|VNĐ|dong)'
        amount_match2 = re.search(amount_pattern2, sms_content, re.IGNORECASE)
        if amount_match2:
            amount_str = amount_match2.group(1).replace(',', '').replace('.', '')
            result['amount'] = amount_str
            # Xác định loại từ context
            if any(word in sms_content.upper() for word in ['NHAN', 'CREDITED', 'CHUYEN DEN', 'NHẬN']):
                result['type'] = 'INCOME'
            else:
                result['type'] = 'EXPENSE'
    
    # Pattern 3: Số tài khoản
    account_pattern = r'(?:TK|T/K|STK|Tài khoản)\s*[:\s]*(\d{4,}[xX\d]*)'
    account_match = re.search(account_pattern, sms_content, re.IGNORECASE)
    if account_match:
        result['account'] = account_match.group(1)[-4:]  # Lấy 4 số cuối
    
    # Pattern 4: Thời gian
    datetime_patterns = [
        r'(\d{2}[-/]\d{2}[-/]\d{4})\s+(\d{2}:\d{2}(?::\d{2})?)',  # DD-MM-YYYY HH:MM
        r'(\d{2}:\d{2}(?::\d{2})?)\s+(\d{2}[-/]\d{2}[-/]\d{4})',  # HH:MM DD-MM-YYYY
        r'luc\s+(\d{2}:\d{2})\s+(\d{2}/\d{2}/\d{4})',            # luc HH:MM DD/MM/YYYY
        r'vao\s+(\d{2}:\d{2})\s+(\d{2}/\d{2}/\d{4})',            # vao HH:MM DD/MM/YYYY
    ]
    for pattern in datetime_patterns:
        dt_match = re.search(pattern, sms_content, re.IGNORECASE)
        if dt_match:
            result['datetime_raw'] = dt_match.group(0)
            break
    
    # Pattern 5: Nội dung/ND
    nd_pattern = r'(?:ND|Noi dung|Content)[:\s]*([^\n]+)'
    nd_match = re.search(nd_pattern, sms_content, re.IGNORECASE)
    if nd_match:
        result['merchant'] = nd_match.group(1).strip()[:100]  # Giới hạn 100 ký tự
    
    if 'amount' in result:
        return result
    return None


async def parse_with_ai(sms_content: str) -> Optional[dict]:
    """
    Parse SMS bằng Google Gemini AI
    Fallback khi regex không hoạt động
    """
    if not settings.GEMINI_API_KEY:
        raise ValueError("GEMINI_API_KEY not configured")
    
    prompt = f"""Phân tích tin nhắn SMS ngân hàng sau và trích xuất thông tin giao dịch.
Trả về JSON với các trường:
- amount: số tiền (chỉ số, không có dấu phân cách)
- type: "INCOME" (tiền vào) hoặc "EXPENSE" (tiền ra)
- bank_code: mã ngân hàng viết tắt (VCB, TCB, BIDV, ACB, MBB, CTG, v.v.)
- account: 4 số cuối tài khoản (nếu có)
- merchant: nội dung/mô tả giao dịch (nếu có)
- datetime: thời gian giao dịch format ISO (YYYY-MM-DDTHH:MM:SS) nếu có

Chỉ trả về JSON, không có text khác.

SMS: {sms_content}
"""
    
    try:
        model = genai.GenerativeModel('gemini-2.0-flash-lite')
        response = model.generate_content(prompt)
        
        # Parse JSON từ response
        response_text = response.text.strip()
        # Loại bỏ markdown code block nếu có
        if response_text.startswith('```'):
            response_text = response_text.split('```')[1]
            if response_text.startswith('json'):
                response_text = response_text[4:]
        response_text = response_text.strip()
        
        import json
        result = json.loads(response_text)
        return result
        
    except Exception as e:
        raise ValueError(f"AI parsing failed: {str(e)}")


@router.post("/parse-sms", response_model=SMSParseResponse)
async def parse_sms(request: SMSParseRequest):
    """
    Parse SMS ngân hàng để trích xuất thông tin giao dịch
    
    Chiến lược:
    1. Thử parse bằng regex (nhanh)
    2. Fallback sang AI nếu regex thất bại
    
    Returns:
        SMSParseResponse với thông tin giao dịch
    """
    sms_content = request.sms_content
    sender = request.sender
    
    if not sms_content or len(sms_content.strip()) < 10:
        raise HTTPException(status_code=400, detail="SMS content too short")
    
    # Phát hiện ngân hàng
    bank_code = detect_bank_code(sms_content, sender)
    
    # Bước 1: Thử parse bằng regex
    try:
        regex_result = parse_with_regex(sms_content)
        if regex_result and 'amount' in regex_result:
            regex_result['bank_code'] = bank_code
            regex_result['parse_method'] = 'regex'
            return SMSParseResponse(success=True, data=regex_result)
    except Exception as e:
        print(f"⚠️ Regex parsing failed: {e}")
    
    # Bước 2: Fallback sang AI
    try:
        ai_result = await parse_with_ai(sms_content)
        if ai_result and 'amount' in ai_result:
            ai_result['bank_code'] = ai_result.get('bank_code', bank_code)
            ai_result['parse_method'] = 'ai'
            return SMSParseResponse(success=True, data=ai_result)
    except Exception as e:
        print(f"❌ AI parsing failed: {e}")
        return SMSParseResponse(
            success=False,
            error=f"Không thể phân tích SMS: {str(e)}"
        )
    
    return SMSParseResponse(
        success=False,
        error="Không thể trích xuất thông tin giao dịch từ SMS"
    )


@router.get("/supported-banks")
async def get_supported_banks():
    """
    Lấy danh sách các ngân hàng được hỗ trợ
    """
    return {
        "banks": [
            {"code": code, "names": names}
            for code, names in VIETNAM_BANKS.items()
        ],
        "total": len(VIETNAM_BANKS)
    }
