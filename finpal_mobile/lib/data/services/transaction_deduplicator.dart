import 'dart:collection';
import 'package:shared_preferences/shared_preferences.dart';

/// Service xử lý chống trùng lặp giao dịch từ nhiều nguồn
/// (SMS và thông báo ứng dụng)
/// Lưu trữ lịch sử để tồn tại qua các lần khởi động lại app
class TransactionDeduplicator {
  static final TransactionDeduplicator _instance =
      TransactionDeduplicator._internal();
  factory TransactionDeduplicator() => _instance;
  TransactionDeduplicator._internal();

  /// Key để lưu trữ các giao dịch đã xử lý trong SharedPreferences
  static const String _storageKey = 'processed_transaction_keys';

  /// Tập hợp các key giao dịch đã được xử lý
  /// Định dạng key: "soTien_ngay_tenNganHang" (đã chuẩn hóa)
  final Set<String> _processedTransactionKeys = HashSet<String>();

  /// Cờ đánh dấu đã load từ storage chưa
  bool _isInitialized = false;

  /// Khoảng thời gian để xem xét giao dịch là trùng lặp (tính bằng phút)
  static const int duplicateTimeWindowMinutes = 5;

  /// Số lượng key tối đa để lưu trữ (tránh tăng không giới hạn)
  static const int maxStoredKeys = 1000;

  /// Khởi tạo và load các key đã lưu từ storage
  Future<void> initialize() async {
    if (_isInitialized) return;

    try {
      final prefs = await SharedPreferences.getInstance();
      final savedKeys = prefs.getStringList(_storageKey);

      if (savedKeys != null && savedKeys.isNotEmpty) {
        _processedTransactionKeys.addAll(savedKeys);
        print('📋 Đã load ${savedKeys.length} key giao dịch từ storage');
      }

      _isInitialized = true;
    } catch (e) {
      print('⚠️ Lỗi khi load lịch sử chống trùng: $e');
      _isInitialized = true;
    }
  }

  /// Lưu các key vào persistent storage
  Future<void> _saveToStorage() async {
    try {
      final prefs = await SharedPreferences.getInstance();

      // Chuyển sang list và giới hạn kích thước
      var keysList = _processedTransactionKeys.toList();
      if (keysList.length > maxStoredKeys) {
        // Chỉ giữ lại các key gần nhất
        keysList = keysList.sublist(keysList.length - maxStoredKeys);
        _processedTransactionKeys.clear();
        _processedTransactionKeys.addAll(keysList);
      }

      await prefs.setStringList(_storageKey, keysList);
      print('💾 Đã lưu ${keysList.length} key giao dịch vào storage');
    } catch (e) {
      print('⚠️ Lỗi khi lưu lịch sử chống trùng: $e');
    }
  }

  /// Tạo key duy nhất cho một giao dịch
  /// Key này được dùng để xác định giao dịch trùng lặp
  String generateTransactionKey({
    required double amount,
    required DateTime date,
    required String bankName,
  }) {
    // Chuẩn hóa ngày theo độ chính xác phút (bỏ qua giây)
    final normalizedDate = DateTime(
      date.year,
      date.month,
      date.day,
      date.hour,
      date.minute,
    );

    // Tạo key với số tiền (2 chữ số thập phân), ngày, và ngân hàng
    final key =
        '${amount.toStringAsFixed(2)}_${normalizedDate.millisecondsSinceEpoch}_${bankName.toLowerCase().trim()}';
    return key;
  }

  /// Kiểm tra xem giao dịch đã được xử lý chưa
  Future<bool> isDuplicate({
    required double amount,
    required DateTime date,
    required String bankName,
  }) async {
    // Đảm bảo đã load từ storage
    await initialize();

    final key = generateTransactionKey(
      amount: amount,
      date: date,
      bankName: bankName,
    );

    // Kiểm tra khớp chính xác
    if (_processedTransactionKeys.contains(key)) {
      return true;
    }

    // Kiểm tra trong khoảng thời gian (±5 phút)
    for (
      int i = -duplicateTimeWindowMinutes;
      i <= duplicateTimeWindowMinutes;
      i++
    ) {
      final adjustedDate = date.add(Duration(minutes: i));
      final adjustedKey = generateTransactionKey(
        amount: amount,
        date: adjustedDate,
        bankName: bankName,
      );
      if (_processedTransactionKeys.contains(adjustedKey)) {
        return true;
      }
    }

    return false;
  }

  /// Đánh dấu giao dịch đã xử lý và lưu vào storage
  Future<void> markAsProcessed({
    required double amount,
    required DateTime date,
    required String bankName,
  }) async {
    await initialize();

    final key = generateTransactionKey(
      amount: amount,
      date: date,
      bankName: bankName,
    );
    _processedTransactionKeys.add(key);

    // Lưu vào persistent storage
    await _saveToStorage();
  }

  /// Kiểm tra và đánh dấu giao dịch trong một thao tác
  /// Trả về true nếu giao dịch là MỚI (không trùng lặp)
  /// Trả về false nếu giao dịch là TRÙNG LẶP
  Future<bool> checkAndMark({
    required double amount,
    required DateTime date,
    required String bankName,
  }) async {
    if (await isDuplicate(amount: amount, date: date, bankName: bankName)) {
      print('⚠️ Phát hiện giao dịch trùng lặp: $amount từ $bankName lúc $date');
      return false; // Là trùng lặp
    }

    await markAsProcessed(amount: amount, date: date, bankName: bankName);
    print('✅ Đã ghi nhận giao dịch mới: $amount từ $bankName lúc $date');
    return true; // Là mới
  }

  /// Parse số tiền từ chuỗi tin nhắn
  /// Hỗ trợ các định dạng: "+1,000,000 VND", "-500000đ", "1.000.000"
  double? parseAmountFromMessage(String message) {
    // Regex để tìm các pattern số tiền
    final amountRegex = RegExp(
      r'([+-]?\s*[\d,\.]+)\s*(?:VND|đ|dong|vnđ)?',
      caseSensitive: false,
    );

    final matches = amountRegex.allMatches(message);
    for (final match in matches) {
      final amountStr = match.group(1) ?? '';
      // Xóa khoảng trắng và chuẩn hóa
      String normalized = amountStr.replaceAll(' ', '');

      // Xử lý định dạng số Việt Nam (1.000.000 = 1000000)
      // Phát hiện nếu dùng dấu chấm làm phân cách hàng nghìn
      if (normalized.contains('.') && !normalized.contains(',')) {
        // Có thể là định dạng Việt Nam với dấu chấm
        normalized = normalized.replaceAll('.', '');
      } else if (normalized.contains(',') && !normalized.contains('.')) {
        // Dùng dấu phẩy làm phân cách hàng nghìn
        normalized = normalized.replaceAll(',', '');
      } else if (normalized.contains(',') && normalized.contains('.')) {
        // Định dạng hỗn hợp - giả sử dấu phẩy là phân cách hàng nghìn
        normalized = normalized.replaceAll(',', '');
      }

      final amount = double.tryParse(normalized);
      if (amount != null && amount >= 1000) {
        // Số tiền giao dịch tối thiểu (để lọc nhiễu)
        return amount.abs(); // Luôn trả về số dương
      }
    }
    return null;
  }

  /// Parse ngày từ chuỗi tin nhắn
  /// Hỗ trợ các định dạng: "25/11/2025", "13:11 25/11/25", "25-11-2025"
  DateTime? parseDateFromMessage(String message) {
    // Thử các pattern ngày khác nhau
    final patterns = [
      // Ngày giờ đầy đủ: "13:11 25/11/2025"
      RegExp(r'(\d{1,2}):(\d{2})\s*(\d{1,2})[/\-](\d{1,2})[/\-](\d{2,4})'),
      // Chỉ ngày: "25/11/2025" hoặc "25-11-2025"
      RegExp(r'(\d{1,2})[/\-](\d{1,2})[/\-](\d{2,4})'),
    ];

    for (final pattern in patterns) {
      final match = pattern.firstMatch(message);
      if (match != null) {
        try {
          if (match.groupCount >= 5) {
            // Ngày giờ đầy đủ
            final hour = int.parse(match.group(1)!);
            final minute = int.parse(match.group(2)!);
            final day = int.parse(match.group(3)!);
            final month = int.parse(match.group(4)!);
            var year = int.parse(match.group(5)!);
            if (year < 100) year += 2000; // Xử lý năm 2 chữ số
            return DateTime(year, month, day, hour, minute);
          } else if (match.groupCount >= 3) {
            // Chỉ ngày
            final day = int.parse(match.group(1)!);
            final month = int.parse(match.group(2)!);
            var year = int.parse(match.group(3)!);
            if (year < 100) year += 2000;
            return DateTime(year, month, day);
          }
        } catch (e) {
          continue;
        }
      }
    }

    // Mặc định trả về null nếu không tìm thấy ngày
    return null;
  }

  /// Xóa tất cả bản ghi giao dịch đã xử lý
  /// Hữu ích khi test hoặc khi user muốn quét lại
  Future<void> clearHistory() async {
    _processedTransactionKeys.clear();

    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_storageKey);
      print('🗑️ Đã xóa lịch sử chống trùng lặp giao dịch');
    } catch (e) {
      print('⚠️ Lỗi khi xóa lịch sử chống trùng: $e');
    }
  }

  /// Lấy số lượng giao dịch đã xử lý
  int get processedCount => _processedTransactionKeys.length;
}
