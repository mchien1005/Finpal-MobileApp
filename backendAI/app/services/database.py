"""
Database Service - Kết nối và truy vấn MySQL Database

Service này cung cấp kết nối đến MySQL database để lấy dữ liệu thật
thay vì sử dụng dữ liệu mẫu từ file CSV.

Các chức năng:
- Kết nối MySQL database
- Lấy transactions của user (bảng giao_dich)
- Lấy budgets của user (bảng ngan_sach)
- Lấy categories (bảng danh_muc)
- Lấy notification templates (bảng mau_thong_bao)

Tên bảng (tiếng Việt):
- nguoi_dung: Users
- giao_dich: Transactions  
- danh_muc: Categories
- ngan_sach: Budgets
- muc_tieu_tiet_kiem: Savings Goals
- mau_thong_bao: Notification Templates
"""

from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import QueuePool
import pandas as pd
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta
import logging
import re

from app.config import settings

logger = logging.getLogger(__name__)


class DatabaseService:
    """
    Database Service - Quản lý kết nối và truy vấn MySQL
    
    Sử dụng schema tiếng Việt:
    - nguoi_dung (users)
    - giao_dich (transactions)
    - danh_muc (categories)
    - ngan_sach (budgets)
    - muc_tieu_tiet_kiem (savings_goals)
    - mau_thong_bao (notification_templates)
    """
    
    _instance = None
    _engine = None
    _session_factory = None
    
    def __new__(cls):
        """Singleton pattern - Chỉ tạo một instance duy nhất"""
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance
    
    def __init__(self):
        """Khởi tạo kết nối database"""
        if self._engine is None:
            self._initialize_connection()
    
    def _initialize_connection(self):
        """
        Khởi tạo connection pool đến MySQL
        """
        try:
            # Tạo connection string
            connection_string = (
                f"mysql+pymysql://{settings.DB_USER}:{settings.DB_PASSWORD}"
                f"@{settings.DB_HOST}:{settings.DB_PORT}/{settings.DB_NAME}"
                "?charset=utf8mb4"
            )
            
            # Tạo engine với connection pooling
            self._engine = create_engine(
                connection_string,
                poolclass=QueuePool,
                pool_size=5,
                max_overflow=10,
                pool_timeout=30,
                pool_recycle=1800,  # Recycle connections sau 30 phút
                echo=settings.DEBUG  # Log SQL queries trong debug mode
            )
            
            # Tạo session factory
            self._session_factory = sessionmaker(bind=self._engine)
            
            # Test connection
            with self._engine.connect() as conn:
                conn.execute(text("SELECT 1"))
                
            logger.info(f"✅ Kết nối MySQL thành công: {settings.DB_HOST}:{settings.DB_PORT}/{settings.DB_NAME}")
            
        except Exception as e:
            logger.error(f"❌ Lỗi kết nối MySQL: {str(e)}")
            raise
    
    def get_session(self):
        """Lấy database session"""
        return self._session_factory()
    
    # ======================== TRANSACTIONS ========================
    
    def get_user_transactions(
        self, 
        user_id: int, 
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None,
        transaction_type: Optional[str] = None
    ) -> pd.DataFrame:
        """
        Lấy transactions của user từ MySQL (bảng giao_dich)
        
        Args:
            user_id: ID của user
            start_date: Ngày bắt đầu (optional)
            end_date: Ngày kết thúc (optional)
            transaction_type: Loại giao dịch: 'EXPENSE', 'INCOME' (optional)
            
        Returns:
            DataFrame chứa transactions với columns:
            - id, user_id, amount, category, category_id, transaction_type
            - description, timestamp, merchant_name
        """
        query = """
            SELECT 
                gd.id,
                gd.id_nguoi_dung as user_id,
                gd.so_tien as amount,
                dm.ten_danh_muc as category,
                gd.id_danh_muc as category_id,
                gd.loai as transaction_type,
                gd.mo_ta as description,
                gd.ngay_giao_dich as timestamp,
                gd.don_vi_chap_nhan as merchant_name
            FROM giao_dich gd
            LEFT JOIN danh_muc dm ON gd.id_danh_muc = dm.id
            WHERE gd.id_nguoi_dung = :user_id
        """
        
        params = {"user_id": user_id}
        
        if start_date:
            query += " AND gd.ngay_giao_dich >= :start_date"
            params["start_date"] = start_date
            
        if end_date:
            query += " AND gd.ngay_giao_dich <= :end_date"
            params["end_date"] = end_date
            
        if transaction_type:
            query += " AND gd.loai = :transaction_type"
            params["transaction_type"] = transaction_type
            
        query += " ORDER BY gd.ngay_giao_dich DESC"
        
        try:
            with self._engine.connect() as conn:
                df = pd.read_sql(text(query), conn, params=params)
                
            logger.info(f"Loaded {len(df)} transactions for user {user_id}")
            return df
            
        except Exception as e:
            logger.error(f"Error loading transactions for user {user_id}: {str(e)}")
            return pd.DataFrame()
    
    def get_user_expense_transactions(
        self, 
        user_id: int,
        months: int = 3
    ) -> pd.DataFrame:
        """
        Lấy expense transactions của user trong N tháng gần nhất
        
        Args:
            user_id: ID của user
            months: Số tháng lấy dữ liệu (mặc định 3 tháng)
            
        Returns:
            DataFrame chứa expense transactions
        """
        start_date = datetime.now() - timedelta(days=months * 30)
        
        return self.get_user_transactions(
            user_id=user_id,
            start_date=start_date,
            transaction_type="EXPENSE"
        )
    
    # ======================== BUDGETS ========================
    
    def get_user_budgets(self, user_id: int) -> pd.DataFrame:
        """
        Lấy budgets của user (bảng ngan_sach)
        
        Args:
            user_id: ID của user
            
        Returns:
            DataFrame chứa budgets với thông tin:
            - id, name, amount, spent_amount, category_id, category_name
            - start_date, end_date, alert_threshold
        """
        query = """
            SELECT 
                ns.id,
                ns.ten_ngan_sach as name,
                ns.so_tien as amount,
                ns.id_danh_muc as category_id,
                dm.ten_danh_muc as category_name,
                ns.ngay_bat_dau as start_date,
                ns.ngay_ket_thuc as end_date,
                ns.nguong_canh_bao as alert_threshold,
                COALESCE(SUM(gd.so_tien), 0) as spent_amount
            FROM ngan_sach ns
            LEFT JOIN danh_muc dm ON ns.id_danh_muc = dm.id
            LEFT JOIN giao_dich gd ON (
                gd.id_nguoi_dung = ns.id_nguoi_dung 
                AND gd.id_danh_muc = ns.id_danh_muc
                AND gd.loai = 'EXPENSE'
                AND gd.ngay_giao_dich BETWEEN ns.ngay_bat_dau AND ns.ngay_ket_thuc
            )
            WHERE ns.id_nguoi_dung = :user_id
                AND ns.dang_hoat_dong = 1
                AND CURDATE() BETWEEN ns.ngay_bat_dau AND ns.ngay_ket_thuc
            GROUP BY ns.id
        """
        
        try:
            with self._engine.connect() as conn:
                df = pd.read_sql(text(query), conn, params={"user_id": user_id})
                
            logger.info(f"Loaded {len(df)} active budgets for user {user_id}")
            return df
            
        except Exception as e:
            logger.error(f"Error loading budgets for user {user_id}: {str(e)}")
            return pd.DataFrame()
    
    # ======================== SAVINGS GOALS ========================
    
    def get_user_savings_goals(self, user_id: int) -> pd.DataFrame:
        """
        Lấy savings goals của user (bảng muc_tieu_tiet_kiem)
        
        Args:
            user_id: ID của user
            
        Returns:
            DataFrame chứa savings goals
        """
        query = """
            SELECT 
                id,
                ten_muc_tieu as name,
                so_tien_muc_tieu as target_amount,
                so_tien_hien_tai as current_amount,
                han_chot as deadline,
                trang_thai as status,
                ngay_tao as created_at
            FROM muc_tieu_tiet_kiem
            WHERE id_nguoi_dung = :user_id
                AND trang_thai = 'ACTIVE'
            ORDER BY han_chot ASC
        """
        
        try:
            with self._engine.connect() as conn:
                df = pd.read_sql(text(query), conn, params={"user_id": user_id})
                
            logger.info(f"Loaded {len(df)} savings goals for user {user_id}")
            return df
            
        except Exception as e:
            logger.error(f"Error loading savings goals for user {user_id}: {str(e)}")
            return pd.DataFrame()
    
    # ======================== CATEGORIES ========================
    
    def get_categories(self) -> Dict[int, str]:
        """
        Lấy mapping category_id -> category_name (bảng danh_muc)
        
        Returns:
            Dict mapping category ID to name
        """
        query = "SELECT id, ten_danh_muc as name FROM danh_muc"
        
        try:
            with self._engine.connect() as conn:
                df = pd.read_sql(text(query), conn)
                
            return dict(zip(df['id'], df['name']))
            
        except Exception as e:
            logger.error(f"Error loading categories: {str(e)}")
            return {}
    
    # ======================== NOTIFICATION TEMPLATES ========================
    
    def get_notification_templates(self, template_type: Optional[str] = None) -> pd.DataFrame:
        """
        Lấy notification templates từ bảng mau_thong_bao
        
        Args:
            template_type: Loại template: 'WARNING', 'ALERT', 'SUCCESS', 'INFO' (optional)
            
        Returns:
            DataFrame chứa templates với columns:
            - id, ma_mau (template_code), tieu_de (title), noi_dung_mau (content_template)
            - loai (type), so_lan_gui (send_count), trang_thai (status)
        """
        query = """
            SELECT 
                id,
                ma_mau as template_code,
                tieu_de as title,
                noi_dung_mau as content_template,
                loai as type,
                so_lan_gui as send_count,
                trang_thai as status
            FROM mau_thong_bao
            WHERE trang_thai = 'ACTIVE'
        """
        
        params = {}
        
        if template_type:
            query += " AND loai = :template_type"
            params["template_type"] = template_type
            
        query += " ORDER BY id"
        
        try:
            with self._engine.connect() as conn:
                df = pd.read_sql(text(query), conn, params=params)
                
            logger.info(f"Loaded {len(df)} notification templates")
            return df
            
        except Exception as e:
            logger.error(f"Error loading notification templates: {str(e)}")
            return pd.DataFrame()
    
    def get_template_by_code(self, template_code: str) -> Optional[Dict[str, Any]]:
        """
        Lấy một template theo mã (ma_mau)
        
        Args:
            template_code: Mã template (ví dụ: 'NOT001', 'BUDGET_WARNING')
            
        Returns:
            Dict chứa thông tin template hoặc None
        """
        query = """
            SELECT 
                id,
                ma_mau as template_code,
                tieu_de as title,
                noi_dung_mau as content_template,
                loai as type
            FROM mau_thong_bao
            WHERE ma_mau = :template_code
                AND trang_thai = 'ACTIVE'
            LIMIT 1
        """
        
        try:
            with self._engine.connect() as conn:
                result = conn.execute(text(query), {"template_code": template_code})
                row = result.fetchone()
                
            if row:
                return {
                    "id": row[0],
                    "template_code": row[1],
                    "title": row[2],
                    "content_template": row[3],
                    "type": row[4]
                }
            return None
            
        except Exception as e:
            logger.error(f"Error loading template {template_code}: {str(e)}")
            return None
    
    def render_notification_template(
        self, 
        template_code: str, 
        **kwargs
    ) -> Optional[Dict[str, str]]:
        """
        Render notification template với dữ liệu thực
        
        Thay thế các placeholder trong template:
        - {amount} → số tiền
        - {category} → danh mục
        - {percentage} → phần trăm
        - {budget_name} → tên ngân sách
        - {days_remaining} → số ngày còn lại
        - {goal_name} → tên mục tiêu
        - v.v.
        
        Args:
            template_code: Mã template
            **kwargs: Các giá trị để thay thế vào template
            
        Returns:
            Dict với 'title' và 'content' đã render
            
        Example:
            render_notification_template(
                'BUDGET_WARNING',
                amount=500000,
                category='Ăn uống',
                percentage=80
            )
        """
        template = self.get_template_by_code(template_code)
        
        if not template:
            logger.warning(f"Template {template_code} not found")
            return None
        
        title = template['title']
        content = template['content_template']
        
        # Thay thế các placeholder
        for key, value in kwargs.items():
            placeholder = "{" + key + "}"
            
            # Format số tiền với dấu phẩy
            if key in ['amount', 'spent_amount', 'budget_amount', 'target_amount', 
                       'current_amount', 'monthly_savings', 'potential_savings']:
                if isinstance(value, (int, float)):
                    formatted_value = f"{value:,.0f}đ"
                else:
                    formatted_value = str(value)
            elif key == 'percentage':
                formatted_value = f"{value:.0f}%"
            else:
                formatted_value = str(value)
            
            title = title.replace(placeholder, formatted_value)
            content = content.replace(placeholder, formatted_value)
        
        # Cập nhật số lần gửi
        try:
            update_query = """
                UPDATE mau_thong_bao 
                SET so_lan_gui = so_lan_gui + 1 
                WHERE ma_mau = :template_code
            """
            with self._engine.connect() as conn:
                conn.execute(text(update_query), {"template_code": template_code})
                conn.commit()
        except Exception as e:
            logger.warning(f"Could not update send count for {template_code}: {e}")
        
        return {
            "title": title,
            "content": content,
            "type": template['type']
        }
    
    def increment_template_send_count(self, template_code: str) -> bool:
        """
        Tăng số lần gửi của template
        
        Args:
            template_code: Mã template
            
        Returns:
            True nếu thành công
        """
        try:
            query = """
                UPDATE mau_thong_bao 
                SET so_lan_gui = so_lan_gui + 1 
                WHERE ma_mau = :template_code
            """
            with self._engine.connect() as conn:
                conn.execute(text(query), {"template_code": template_code})
                conn.commit()
            return True
        except Exception as e:
            logger.error(f"Error incrementing send count: {e}")
            return False
    
    # ======================== UTILITY ========================
    
    def check_connection(self) -> bool:
        """
        Kiểm tra kết nối database
        
        Returns:
            True nếu kết nối OK, False nếu lỗi
        """
        try:
            with self._engine.connect() as conn:
                conn.execute(text("SELECT 1"))
            return True
        except Exception:
            return False
    
    def get_table_names(self) -> List[str]:
        """
        Lấy danh sách các bảng trong database
        
        Returns:
            List tên các bảng
        """
        try:
            query = "SHOW TABLES"
            with self._engine.connect() as conn:
                result = conn.execute(text(query))
                return [row[0] for row in result]
        except Exception as e:
            logger.error(f"Error getting table names: {e}")
            return []


# Singleton instance
_db_service: Optional[DatabaseService] = None


def get_database_service() -> DatabaseService:
    """
    Lấy singleton instance của DatabaseService
    
    Returns:
        DatabaseService instance
    """
    global _db_service
    if _db_service is None:
        _db_service = DatabaseService()
    return _db_service
