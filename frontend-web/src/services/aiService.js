/**
 * AI Service - Kết nối với backendAI
 */

const AI_API_BASE_URL = import.meta.env.VITE_AI_API_URL || 'http://localhost:8000/api';

class AIService {
    /**
     * Lấy tổng quan stats cho admin dashboard
     */
    async getAdminStats() {
        try {
            const response = await fetch(`${AI_API_BASE_URL}/admin/ai/stats`);
            if (!response.ok) throw new Error('Failed to fetch stats');
            return await response.json();
        } catch (error) {
            console.error('Error fetching admin stats:', error);
            throw error;
        }
    }

    /**
     * Lấy danh sách tất cả AI models
     */
    async getModels() {
        try {
            const response = await fetch(`${AI_API_BASE_URL}/admin/ai/models`);
            if (!response.ok) throw new Error('Failed to fetch models');
            return await response.json();
        } catch (error) {
            console.error('Error fetching models:', error);
            throw error;
        }
    }

    /**
     * Lấy chi tiết một model
     */
    async getModelDetail(modelName) {
        try {
            const response = await fetch(`${AI_API_BASE_URL}/admin/ai/models/${encodeURIComponent(modelName)}`);
            if (!response.ok) throw new Error('Failed to fetch model detail');
            return await response.json();
        } catch (error) {
            console.error('Error fetching model detail:', error);
            throw error;
        }
    }

    /**
     * Lấy metrics của một model
     */
    async getModelMetrics(modelName) {
        try {
            const response = await fetch(`${AI_API_BASE_URL}/admin/ai/models/${encodeURIComponent(modelName)}/metrics`);
            if (!response.ok) throw new Error('Failed to fetch model metrics');
            return await response.json();
        } catch (error) {
            console.error('Error fetching model metrics:', error);
            throw error;
        }
    }

    /**
     * Lấy lịch sử accuracy trend
     * @param {number} days - Số ngày (default 7)
     */
    async getAccuracyHistory(days = 7) {
        try {
            const response = await fetch(`${AI_API_BASE_URL}/admin/ai/accuracy/history?days=${days}`);
            if (!response.ok) throw new Error('Failed to fetch accuracy history');
            return await response.json();
        } catch (error) {
            console.error('Error fetching accuracy history:', error);
            throw error;
        }
    }

    /**
     * Lấy prediction logs
     * @param {object} params - { page, page_size, model_name, correct_only }
     */
    async getPredictionLogs(params = {}) {
        try {
            const queryParams = new URLSearchParams();
            if (params.page) queryParams.append('page', params.page);
            if (params.page_size) queryParams.append('page_size', params.page_size);
            if (params.model_name) queryParams.append('model_name', params.model_name);
            if (params.correct_only !== undefined) queryParams.append('correct_only', params.correct_only);

            const response = await fetch(`${AI_API_BASE_URL}/admin/ai/predictions/logs?${queryParams}`);
            if (!response.ok) throw new Error('Failed to fetch prediction logs');
            return await response.json();
        } catch (error) {
            console.error('Error fetching prediction logs:', error);
            throw error;
        }
    }

    /**
     * Retrain tất cả hoặc một số models
     * @param {object} request - { model_names: string[], force: boolean }
     */
    async retrainModels(request = {}) {
        try {
            const response = await fetch(`${AI_API_BASE_URL}/admin/ai/retrain`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(request),
            });
            if (!response.ok) throw new Error('Failed to retrain models');
            return await response.json();
        } catch (error) {
            console.error('Error retraining models:', error);
            throw error;
        }
    }

    /**
     * Retrain một model cụ thể
     * @param {string} modelName - Tên model
     * @param {boolean} force - Force retrain
     */
    async retrainSingleModel(modelName, force = false) {
        try {
            const response = await fetch(`${AI_API_BASE_URL}/admin/ai/retrain/${encodeURIComponent(modelName)}?force=${force}`, {
                method: 'POST',
            });
            if (!response.ok) throw new Error('Failed to retrain model');
            return await response.json();
        } catch (error) {
            console.error('Error retraining model:', error);
            throw error;
        }
    }
}

export default new AIService();
