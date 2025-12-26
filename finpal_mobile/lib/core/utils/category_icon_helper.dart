import 'package:flutter/material.dart';

/// Utility class để map icon string từ API sang Emoji hoặc IconData
/// Ưu tiên sử dụng Emoji đa màu sắc sinh động
class CategoryIconHelper {
  /// Parse mã màu hex từ API (VD: #FF7043) thành Color
  /// Trả về null nếu không parse được
  static Color? parseHexColor(String? hexColor) {
    if (hexColor == null || hexColor.isEmpty) return null;

    try {
      // Loại bỏ # ở đầu nếu có
      String hex = hexColor.replaceFirst('#', '');

      // Thêm alpha channel nếu chỉ có 6 ký tự (RGB)
      if (hex.length == 6) {
        hex = 'FF$hex';
      }

      // Parse thành int và tạo Color
      return Color(int.parse(hex, radix: 16));
    } catch (e) {
      return null;
    }
  }

  /// Lấy Emoji đa màu cho danh mục (khuyên dùng)
  static String getEmoji(String? iconString) {
    if (iconString == null || iconString.isEmpty) {
      return '📦';
    }

    // Chuyển về lowercase để so sánh
    final icon = iconString.toLowerCase().trim();

    switch (icon) {
      // ========== CHI TIÊU ==========

      // Ăn uống
      case 'food':
      case 'restaurant':
      case 'dining':
      case 'meal':
      case 'eat':
        return '🍔';

      // Di chuyển
      case 'car':
      case 'transport':
      case 'transportation':
      case 'vehicle':
      case 'commute':
        return '🚗';

      // Mua sắm
      case 'shopping':
      case 'shop':
      case 'store':
      case 'bag':
      case 'cart':
        return '🛍️';

      // Giải trí
      case 'entertainment':
      case 'movie':
      case 'cinema':
      case 'fun':
      case 'game':
      case 'games':
        return '🎬';

      // Sức khỏe
      case 'health':
      case 'medical':
      case 'hospital':
      case 'medicine':
      case 'doctor':
      case 'pharmacy':
      case 'heart_pulse':
        return '💊';

      // Giáo dục / Học tập
      case 'education':
      case 'school':
      case 'study':
      case 'book':
      case 'learning':
      case 'tuition':
        return '🎓';

      // Hóa đơn & Tiện ích
      case 'bill':
      case 'bills':
      case 'receipt':
      case 'invoice':
      case 'utility':
      case 'utilities':
      case 'flash':
        return '📝';

      // Nhà ở
      case 'home':
      case 'house':
      case 'rent':
      case 'housing':
      case 'apartment':
      case 'accommodation':
        return '🏠';

      // Gia đình
      case 'family':
      case 'children':
      case 'kids':
      case 'account_group':
        return '👨‍👩‍👧‍👦';

      // Bảo hiểm
      case 'insurance':
      case 'protection':
      case 'shield':
      case 'shield_check':
        return '🛡️';

      // Đầu tư
      case 'investment':
      case 'invest':
      case 'stock':
      case 'trading':
      case 'chart_line':
        return '📈';

      // Quà tặng
      case 'gift':
      case 'present':
      case 'gifts':
        return '🎁';

      // Công việc
      case 'work':
      case 'job':
      case 'office':
      case 'briefcase':
        return '💼';

      // Làm đẹp
      case 'beauty':
      case 'cosmetic':
      case 'face_woman':
      case 'makeup':
      case 'salon':
        return '💄';

      // Thú cưng
      case 'pet':
      case 'pets':
      case 'animal':
      case 'dog':
      case 'cat':
        return '🐾';

      // Điện thoại
      case 'phone':
      case 'mobile':
      case 'telecom':
      case 'smartphone':
        return '📱';

      // Internet
      case 'internet':
      case 'wifi':
      case 'network':
      case 'data':
        return '📶';

      // Điện nước
      case 'electricity':
      case 'water':
      case 'electric':
        return '⚡';

      // Cafe
      case 'coffee':
      case 'cafe':
      case 'drink':
      case 'beverage':
        return '☕';

      // Du lịch
      case 'travel':
      case 'trip':
      case 'vacation':
      case 'flight':
      case 'holiday':
        return '✈️';

      // Thể thao
      case 'sport':
      case 'sports':
      case 'fitness':
      case 'gym':
      case 'exercise':
        return '🏋️';

      // Tiết kiệm
      case 'savings':
      case 'save':
      case 'saving':
        return '🐷';

      // Từ thiện
      case 'charity':
      case 'donate':
      case 'donation':
        return '❤️';

      // Khác
      case 'other':
      case 'others':
      case 'misc':
      case 'miscellaneous':
      case 'dots_horizontal':
        return '📦';

      // ========== THU NHẬP ==========

      // Lương
      case 'salary':
      case 'wage':
      case 'income':
      case 'wallet':
      case 'cash':
        return '💰';

      // Thưởng
      case 'bonus':
      case 'reward':
      case 'prize':
      case 'trophy':
        return '🏆';

      // Làm thêm
      case 'freelance':
      case 'parttime':
      case 'sidejob':
      case 'extra':
      case 'overtime':
      case 'clock_outline':
        return '⏰';

      // Kinh doanh
      case 'business':
      case 'business_income':
      case 'sell':
      case 'sales':
      case 'profit':
        return '🏪';

      case 'trending_up':
        return '📈';

      // Cho vay (thu)
      case 'loan':
      case 'lend':
      case 'lending':
      case 'debt':
      case 'debt_collect':
      case 'handshake':
        return '🤝';

      // Được tặng
      case 'receive':
      case 'received':
      case 'gift_received':
      case 'gift_open':
        return '🎀';

      // Ngân sách / Ví
      case 'budget':
      case 'money':
        return '💵';

      // Danh mục mặc định
      case 'category':
      default:
        return '📦';
    }
  }

  /// Widget hiển thị Emoji với size tùy chỉnh
  static Widget emojiWidget(String? iconString, {double size = 24}) {
    return Text(getEmoji(iconString), style: TextStyle(fontSize: size));
  }

  /// Widget hiển thị Emoji với fallback theo tên
  static Widget emojiWidgetWithFallback(
    String? iconString,
    String? name, {
    double size = 24,
  }) {
    final emoji = iconString != null && iconString.isNotEmpty
        ? getEmoji(iconString)
        : (name != null && name.isNotEmpty ? getEmoji(name) : '📦');
    return Text(emoji, style: TextStyle(fontSize: size));
  }

  /// Map icon string sang IconData (Material Icons) - fallback cho các trường hợp cần IconData
  static IconData getIcon(String? iconString) {
    if (iconString == null || iconString.isEmpty) {
      return Icons.category;
    }

    // Chuyển về lowercase để so sánh
    final icon = iconString.toLowerCase().trim();

    switch (icon) {
      // ========== CHI TIÊU ==========

      // Ăn uống
      case 'food':
      case 'restaurant':
      case 'dining':
      case 'meal':
      case 'eat':
        return Icons.restaurant;

      // Di chuyển
      case 'car':
      case 'transport':
      case 'transportation':
      case 'vehicle':
      case 'commute':
        return Icons.directions_car;

      // Mua sắm
      case 'shopping':
      case 'shop':
      case 'store':
      case 'bag':
      case 'cart':
        return Icons.shopping_bag;

      // Giải trí
      case 'entertainment':
      case 'movie':
      case 'cinema':
      case 'fun':
      case 'game':
      case 'games':
        return Icons.movie;

      // Sức khỏe
      case 'health':
      case 'medical':
      case 'hospital':
      case 'medicine':
      case 'doctor':
      case 'pharmacy':
      case 'heart_pulse':
        return Icons.local_hospital;

      // Giáo dục / Học tập
      case 'education':
      case 'school':
      case 'study':
      case 'book':
      case 'learning':
      case 'tuition':
        return Icons.school;

      // Hóa đơn & Tiện ích
      case 'bill':
      case 'bills':
      case 'receipt':
      case 'invoice':
      case 'utility':
      case 'utilities':
      case 'flash':
        return Icons.receipt_long;

      // Nhà ở
      case 'home':
      case 'house':
      case 'rent':
      case 'housing':
      case 'apartment':
      case 'accommodation':
        return Icons.home;

      // Gia đình
      case 'family':
      case 'children':
      case 'kids':
      case 'account_group':
        return Icons.family_restroom;

      // Bảo hiểm
      case 'insurance':
      case 'protection':
      case 'shield':
      case 'shield_check':
        return Icons.shield;

      // Đầu tư
      case 'investment':
      case 'invest':
      case 'stock':
      case 'trading':
      case 'chart_line':
        return Icons.trending_up;

      // Quà tặng
      case 'gift':
      case 'present':
      case 'gifts':
        return Icons.card_giftcard;

      // Công việc
      case 'work':
      case 'job':
      case 'office':
      case 'briefcase':
        return Icons.work;

      // Làm đẹp
      case 'beauty':
      case 'cosmetic':
      case 'face_woman':
      case 'makeup':
      case 'salon':
        return Icons.spa;

      // Thú cưng
      case 'pet':
      case 'pets':
      case 'animal':
      case 'dog':
      case 'cat':
        return Icons.pets;

      // Điện thoại
      case 'phone':
      case 'mobile':
      case 'telecom':
      case 'smartphone':
        return Icons.phone_android;

      // Internet
      case 'internet':
      case 'wifi':
      case 'network':
      case 'data':
        return Icons.wifi;

      // Điện nước
      case 'electricity':
      case 'water':
      case 'electric':
        return Icons.bolt;

      // Cafe
      case 'coffee':
      case 'cafe':
      case 'drink':
      case 'beverage':
        return Icons.local_cafe;

      // Du lịch
      case 'travel':
      case 'trip':
      case 'vacation':
      case 'flight':
      case 'holiday':
        return Icons.flight;

      // Thể thao
      case 'sport':
      case 'sports':
      case 'fitness':
      case 'gym':
      case 'exercise':
        return Icons.fitness_center;

      // Tiết kiệm
      case 'savings':
      case 'save':
      case 'saving':
        return Icons.savings;

      // Từ thiện
      case 'charity':
      case 'donate':
      case 'donation':
        return Icons.volunteer_activism;

      // Khác
      case 'other':
      case 'others':
      case 'misc':
      case 'miscellaneous':
      case 'dots_horizontal':
        return Icons.more_horiz;

      // ========== THU NHẬP ==========

      // Lương
      case 'salary':
      case 'wage':
      case 'income':
      case 'wallet':
      case 'cash':
        return Icons.account_balance_wallet;

      // Thưởng
      case 'bonus':
      case 'reward':
      case 'prize':
      case 'trophy':
        return Icons.emoji_events;

      // Làm thêm
      case 'freelance':
      case 'parttime':
      case 'sidejob':
      case 'extra':
      case 'overtime':
      case 'clock_outline':
        return Icons.access_time;

      // Kinh doanh
      case 'business':
      case 'business_income':
      case 'sell':
      case 'sales':
      case 'profit':
        return Icons.storefront;

      case 'trending_up':
        return Icons.trending_up;

      // Cho vay (thu)
      case 'loan':
      case 'lend':
      case 'lending':
      case 'debt':
      case 'debt_collect':
      case 'handshake':
        return Icons.payments;

      // Được tặng
      case 'receive':
      case 'received':
      case 'gift_received':
      case 'gift_open':
        return Icons.redeem;

      // Ngân sách / Ví
      case 'budget':
      case 'money':
        return Icons.account_balance_wallet;

      // Danh mục mặc định
      case 'category':
      default:
        return Icons.category;
    }
  }

  /// Alias cho getIcon - dùng khi cần fallback theo tên
  static IconData getIconWithFallback(String? iconString, String? name) {
    // Thử với icon string trước
    if (iconString != null && iconString.isNotEmpty) {
      return getIcon(iconString);
    }
    // Nếu không có icon string, thử dùng name làm icon string
    if (name != null && name.isNotEmpty) {
      return getIcon(name);
    }
    return Icons.category;
  }

  /// Lấy màu cho danh mục
  /// Ưu tiên sử dụng màu từ API (colorFromApi) nếu có
  /// Nếu không có sẽ fallback về màu dựa trên icon/name
  static Color getColor(
    String? iconString,
    String? name, {
    String? colorFromApi,
  }) {
    // Ưu tiên sử dụng màu từ API nếu có
    final apiColor = parseHexColor(colorFromApi);
    if (apiColor != null) return apiColor;

    final key = (iconString ?? name ?? '').toLowerCase().trim();

    switch (key) {
      // Ăn uống - Đỏ hồng
      case 'food':
      case 'restaurant':
        return const Color(0xFFFF6B6B);

      // Di chuyển - Cam
      case 'car':
      case 'transport':
        return const Color(0xFFFF8C42);

      // Mua sắm - Xanh ngọc
      case 'shopping':
        return const Color(0xFF4ECDC4);

      // Giải trí - Tím
      case 'entertainment':
      case 'movie':
        return const Color(0xFF9B59B6);

      // Sức khỏe - Hồng đậm
      case 'health':
      case 'medical':
      case 'heart_pulse':
        return const Color(0xFFE91E63);

      // Giáo dục - Xanh lá
      case 'education':
      case 'school':
        return const Color(0xFF2ECC71);

      // Hóa đơn & Tiện ích - Xanh dương
      case 'bill':
      case 'bills':
      case 'utility':
      case 'flash':
        return const Color(0xFF3498DB);

      // Nhà ở - Nâu
      case 'home':
      case 'house':
        return const Color(0xFF8D6E63);

      // Gia đình
      case 'family':
      case 'account_group':
        return const Color.fromARGB(255, 55, 223, 72);

      // Bảo hiểm - Xanh đậm
      case 'insurance':
      case 'shield_check':
        return const Color(0xFF1976D2);

      // Đầu tư - Xanh dương đậm
      case 'investment':
      case 'chart_line':
      case 'trending_up':
        return const Color.fromARGB(255, 255, 115, 0);

      // Quà tặng - Hồng
      case 'gift':
        return const Color(0xFFEC407A);

      // Công việc - Xám đậm
      case 'work':
      case 'briefcase':
        return const Color(0xFF546E7A);

      // Làm đẹp - Tím nhạt
      case 'beauty':
      case 'spa':
      case 'face_woman':
        return const Color(0xFFAB47BC);

      // Thú cưng - Nâu nhạt
      case 'pet':
      case 'pets':
        return const Color(0xFFBCAAA4);

      // Cafe - Nâu cafe
      case 'coffee':
      case 'cafe':
        return const Color(0xFF795548);

      // Du lịch - Xanh biển
      case 'travel':
      case 'flight':
        return const Color(0xFF00BCD4);

      // Thể thao - Xanh lục
      case 'sport':
      case 'sports':
      case 'fitness':
        return const Color(0xFF8BC34A);

      // Tiết kiệm - Xanh lá đậm
      case 'savings':
      case 'save':
        return const Color(0xFF388E3C);

      // Từ thiện - Hồng đỏ
      case 'charity':
      case 'donate':
        return const Color(0xFFD81B60);

      // Khác - Xám
      case 'other':
      case 'dots_horizontal':
        return const Color(0xFF78909C);

      // ========== THU NHẬP ==========

      // Lương - Xanh lá đậm
      case 'salary':
      case 'income':
      case 'cash':
        return const Color(0xFF4CAF50);

      // Thưởng - Vàng cam
      case 'bonus':
      case 'trophy':
        return const Color(0xFFFFB300);

      // Làm thêm - Xanh cyan
      case 'freelance':
      case 'clock_outline':
        return const Color(0xFF00ACC1);

      // Kinh doanh - Xanh lá sáng
      case 'business':
      case 'store':
        return const Color(0xFF66BB6A);

      // Cho vay - Tím than
      case 'loan':
      case 'handshake':
        return const Color(0xFF7E57C2);

      // Được tặng - Hồng cam
      case 'receive':
      case 'gift_open':
        return const Color(0xFFFF7043);

      default:
        return const Color(0xFF607D8B);
    }
  }

  /// Lấy màu nền dựa trên màu chính
  static Color getBackgroundColor(Color mainColor) {
    return mainColor.withValues(alpha: 0.15);
  }
}
