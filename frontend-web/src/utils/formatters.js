import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/vi';

dayjs.extend(relativeTime);
dayjs.locale('vi');

// Format currency (VND)
export const formatCurrency = (amount) => {
    if (amount === null || amount === undefined) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
    }).format(amount);
};

// Format number with thousand separator
export const formatNumber = (number) => {
    if (number === null || number === undefined) return '0';
    return new Intl.NumberFormat('vi-VN').format(number);
};

// Format date
export const formatDate = (date, format = 'DD/MM/YYYY') => {
    if (!date) return '-';
    return dayjs(date).format(format);
};

// Format date and time
export const formatDateTime = (date) => {
    if (!date) return '-';
    return dayjs(date).format('DD/MM/YYYY HH:mm');
};

// Format relative time (e.g., "2 hours ago")
export const formatRelativeTime = (date) => {
    if (!date) return '-';
    return dayjs(date).fromNow();
};

// Format phone number
export const formatPhoneNumber = (phone) => {
    if (!phone) return '-';
    // Format: 0123 456 789
    return phone.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3');
};

// Format percentage
export const formatPercentage = (value, decimals = 1) => {
    if (value === null || value === undefined) return '0%';
    return `${value.toFixed(decimals)}%`;
};

// Truncate text
export const truncateText = (text, maxLength = 50) => {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return `${text.substring(0, maxLength)}...`;
};

// Get status color
export const getStatusColor = (status) => {
    const colorMap = {
        ACTIVE: 'success',
        INACTIVE: 'default',
        LOCKED: 'error',
        PENDING: 'warning',
    };
    return colorMap[status] || 'default';
};

// Get transaction type color
export const getTransactionTypeColor = (type) => {
    return type === 'INCOME' ? 'success' : 'error';
};

// Parse error message
export const parseErrorMessage = (error) => {
    if (error.response?.data?.message) {
        return error.response.data.message;
    }
    if (error.message) {
        return error.message;
    }
    return 'Đã xảy ra lỗi. Vui lòng thử lại.';
};
