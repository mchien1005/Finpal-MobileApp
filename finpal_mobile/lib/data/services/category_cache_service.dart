import '../models/category.dart';
import 'transaction_service.dart';

/// Service quản lý cache danh mục tập trung
/// Được chia sẻ giữa tất cả màn hình và widget trong app
///
/// Sử dụng:
/// - CategoryCacheService.instance.getCategories('EXPENSE')
/// - CategoryCacheService.instance.preloadAllCategories()
/// - CategoryCacheService.clearCache() // Khi cần refresh
class CategoryCacheService {
  // Singleton instance
  static final CategoryCacheService _instance =
      CategoryCacheService._internal();
  static CategoryCacheService get instance => _instance;
  CategoryCacheService._internal();

  // Cache danh mục theo loại (EXPENSE/INCOME)
  final Map<String, List<Category>> _cache = {};

  // Trạng thái loading để tránh gọi API nhiều lần cùng lúc
  final Map<String, bool> _loadingStatus = {};

  // Transaction service để gọi API
  final TransactionService _transactionService = TransactionService();

  /// Lấy danh mục theo loại (từ cache hoặc API)
  /// [type] - 'EXPENSE' hoặc 'INCOME'
  Future<List<Category>> getCategories(String type) async {
    // Chuẩn hóa type
    final normalizedType = type.toUpperCase();

    // Nếu đã có trong cache, trả về ngay
    if (_cache.containsKey(normalizedType) &&
        _cache[normalizedType]!.isNotEmpty) {
      return _cache[normalizedType]!;
    }

    // Nếu đang loading, đợi và thử lại
    if (_loadingStatus[normalizedType] == true) {
      await Future.delayed(const Duration(milliseconds: 100));
      return getCategories(normalizedType);
    }

    // Đánh dấu đang loading
    _loadingStatus[normalizedType] = true;

    try {
      final categories = await _transactionService.getCategories(
        type: normalizedType,
      );
      _cache[normalizedType] = categories;
      _loadingStatus[normalizedType] = false;
      return categories;
    } catch (e) {
      _loadingStatus[normalizedType] = false;
      rethrow;
    }
  }

  /// Preload tất cả danh mục (EXPENSE và INCOME)
  /// Gọi khi vào màn hình chính để load sẵn
  Future<void> preloadAllCategories() async {
    await Future.wait([getCategories('EXPENSE'), getCategories('INCOME')]);
  }

  /// Kiểm tra xem loại danh mục đã được cache chưa
  bool isCached(String type) {
    final normalizedType = type.toUpperCase();
    return _cache.containsKey(normalizedType) &&
        _cache[normalizedType]!.isNotEmpty;
  }

  /// Kiểm tra xem tất cả danh mục đã được cache chưa
  bool get isAllCached => isCached('EXPENSE') && isCached('INCOME');

  /// Xóa cache (gọi khi cần refresh - ví dụ: thêm/sửa danh mục)
  static void clearCache() {
    _instance._cache.clear();
    _instance._loadingStatus.clear();
  }

  /// Xóa cache theo loại cụ thể
  void clearCacheByType(String type) {
    final normalizedType = type.toUpperCase();
    _cache.remove(normalizedType);
    _loadingStatus.remove(normalizedType);
  }
}
