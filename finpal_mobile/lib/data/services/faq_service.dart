import '../models/faq.dart';
import 'api_service.dart';

class FaqService {
  final ApiService _apiService = ApiService();

  /// Lấy tất cả FAQs đang active
  Future<List<Faq>> getAllFaqs() async {
    try {
      print('🔍 Fetching FAQs...');
      final response = await _apiService.get('/faqs');
      print('📦 FAQ Response: $response');
      print('📊 FAQ Response type: ${response.runtimeType}');

      // Check if response is already a List or a Map with data field
      final List<dynamic> faqList;
      if (response is List) {
        faqList = response;
      } else if (response is Map<String, dynamic>) {
        final dynamic data = response['data'] ?? response;
        faqList = data is List ? data : [];
      } else {
        faqList = [];
      }

      print('✅ Found ${faqList.length} FAQs');

      return faqList
          .map((json) => Faq.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      print('❌ Error loading FAQs: $e');
      rethrow;
    }
  }

  /// Lấy FAQ theo ID
  Future<Faq> getFaqById(int id) async {
    try {
      final response = await _apiService.get('/faqs/$id');

      // API có thể trả về object trực tiếp hoặc wrap trong data
      final Map<String, dynamic> faqData;
      if (response['data'] != null) {
        faqData = response['data'] as Map<String, dynamic>;
      } else {
        faqData = response;
      }

      return Faq.fromJson(faqData);
    } catch (e) {
      rethrow;
    }
  }

  /// Submit feedback cho FAQ (helpful = true, not helpful = false)
  Future<void> submitFeedback(int faqId, bool isHelpful) async {
    try {
      await _apiService.post('/faqs/$faqId/feedback', {'helpful': isHelpful});
    } catch (e) {
      rethrow;
    }
  }
}
