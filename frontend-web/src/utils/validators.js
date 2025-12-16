// Email validation
export const validateEmail = (email) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
};

// Phone validation (Vietnam)
export const validatePhone = (phone) => {
    const regex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
    return regex.test(phone);
};

// Password validation
export const validatePassword = (password) => {
    // At least 8 characters, 1 uppercase, 1 lowercase, 1 number
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/;
    return regex.test(password);
};

// Username validation
export const validateUsername = (username) => {
    // 3-20 characters, alphanumeric and underscore
    const regex = /^[a-zA-Z0-9_]{3,20}$/;
    return regex.test(username);
};

// Required field validation
export const validateRequired = (value) => {
    if (typeof value === 'string') {
        return value.trim().length > 0;
    }
    return value !== null && value !== undefined;
};

// Min length validation
export const validateMinLength = (value, minLength) => {
    return value && value.length >= minLength;
};

// Max length validation
export const validateMaxLength = (value, maxLength) => {
    return !value || value.length <= maxLength;
};

// Number validation
export const validateNumber = (value) => {
    return !isNaN(parseFloat(value)) && isFinite(value);
};

// Positive number validation
export const validatePositiveNumber = (value) => {
    return validateNumber(value) && parseFloat(value) > 0;
};

// Form validation rules for Ant Design Form
export const formRules = {
    required: {
        required: true,
        message: 'Trường này là bắt buộc',
    },
    email: {
        type: 'email',
        message: 'Email không hợp lệ',
    },
    phone: {
        pattern: /(84|0[3|5|7|8|9])+([0-9]{8})\b/,
        message: 'Số điện thoại không hợp lệ',
    },
    username: {
        pattern: /^[a-zA-Z0-9_]{3,20}$/,
        message: 'Username phải từ 3-20 ký tự, chỉ chứa chữ, số và dấu gạch dưới',
    },
    password: {
        pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/,
        message: 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số',
    },
    minLength: (length) => ({
        min: length,
        message: `Tối thiểu ${length} ký tự`,
    }),
    maxLength: (length) => ({
        max: length,
        message: `Tối đa ${length} ký tự`,
    }),
    positiveNumber: {
        validator: (_, value) => {
            if (!value || parseFloat(value) > 0) {
                return Promise.resolve();
            }
            return Promise.reject(new Error('Giá trị phải lớn hơn 0'));
        },
    },
};
