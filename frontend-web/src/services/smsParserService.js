import api from './api';

/**
 * SMS Parser Service
 * Handles all API calls related to SMS Parser management
 */

// Get all SMS parsers
export const getAllParsers = async () => {
    try {
        const response = await api.get('/sms/parsers');
        return response.data;
    } catch (error) {
        console.error('Error fetching parsers:', error);
        throw error;
    }
};

// Get parser by ID
export const getParserById = async (id) => {
    try {
        const response = await api.get(`/sms/parsers/${id}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching parser:', error);
        throw error;
    }
};

// Create new SMS parser
export const createParser = async (parserData) => {
    try {
        console.log('Creating parser with data:', JSON.stringify(parserData, null, 2));
        const response = await api.post('/sms/parsers', parserData);
        console.log('Create parser response:', JSON.stringify(response.data, null, 2));
        return response.data;
    } catch (error) {
        console.error('Error creating parser:', error);
        console.error('Error response:', error.response?.data);
        throw error;
    }
};

// Update SMS parser
export const updateParser = async (id, parserData) => {
    try {
        const response = await api.put(`/sms/parsers/${id}`, parserData);
        return response.data;
    } catch (error) {
        console.error('Error updating parser:', error);
        throw error;
    }
};

// Delete SMS parser
export const deleteParser = async (id) => {
    try {
        const response = await api.delete(`/sms/parsers/${id}`);
        return response.data;
    } catch (error) {
        console.error('Error deleting parser:', error);
        throw error;
    }
};

// Test SMS parser with regex (detailed)
export const testParser = async (testData) => {
    try {
        const response = await api.post('/sms/parsers/test', testData);
        return response.data;
    } catch (error) {
        console.error('Error testing parser:', error);
        throw error;
    }
};

// Test SMS parser (simple)
export const testParserSimple = async (smsText) => {
    try {
        const response = await api.post('/sms/parsers/test-simple', { smsContent: smsText });
        console.log('Full Response:', JSON.stringify(response.data, null, 2));
        return response.data;
    } catch (error) {
        console.error('Error testing parser (simple):', error);
        throw error;
    }
};

export default {
    getAllParsers,
    getParserById,
    createParser,
    updateParser,
    deleteParser,
    testParser,
    testParserSimple,
};
