/// Weekly Spending Trend Model
/// Model cho API: GET /api/insights/weekly-spending-trend/{user_id}

class WeeklySpendingTrendResponse {
  final int userId;
  final String weekStart;
  final String weekEnd;
  final List<DailySpending> dailySpending;
  final WeeklyInsight insight;

  WeeklySpendingTrendResponse({
    required this.userId,
    required this.weekStart,
    required this.weekEnd,
    required this.dailySpending,
    required this.insight,
  });

  factory WeeklySpendingTrendResponse.fromJson(Map<String, dynamic> json) {
    return WeeklySpendingTrendResponse(
      userId: json['user_id'] ?? 0,
      weekStart: json['week_start'] ?? json['week_start_date'] ?? '',
      weekEnd: json['week_end'] ?? json['week_end_date'] ?? '',
      dailySpending:
          (json['daily_spending'] as List<dynamic>?)
              ?.map((e) => DailySpending.fromJson(e as Map<String, dynamic>))
              .toList() ??
          [],
      insight: WeeklyInsight.fromJson(json['insight'] ?? {}),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'user_id': userId,
      'week_start': weekStart,
      'week_end': weekEnd,
      'daily_spending': dailySpending.map((e) => e.toJson()).toList(),
      'insight': insight.toJson(),
    };
  }
}

class DailySpending {
  final String dayOfWeek;
  final String date;
  final double totalSpending;
  final TopCategory? topCategory;

  DailySpending({
    required this.dayOfWeek,
    required this.date,
    required this.totalSpending,
    this.topCategory,
  });

  factory DailySpending.fromJson(Map<String, dynamic> json) {
    return DailySpending(
      dayOfWeek: json['day_of_week'] ?? '',
      date: json['date'] ?? '',
      totalSpending: (json['total_spending'] ?? json['total_amount'] ?? 0)
          .toDouble(),
      topCategory: json['top_category'] != null
          ? TopCategory.fromJson(json['top_category'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'day_of_week': dayOfWeek,
      'date': date,
      'total_spending': totalSpending,
      'top_category': topCategory?.toJson(),
    };
  }

  /// Lấy emoji cho danh mục hàng đầu
  String get emoji {
    if (topCategory == null) return '💰';

    switch (topCategory!.categoryName.toLowerCase()) {
      case 'ăn uống':
      case 'food':
        return '🍜';
      case 'cà phê':
      case 'cafe':
      case 'coffee':
        return '☕';
      case 'mua sắm':
      case 'shopping':
        return '🛍️';
      case 'di chuyển':
      case 'transportation':
        return '🚗';
      case 'giải trí':
      case 'entertainment':
        return '🎬';
      case 'pizza':
        return '🍕';
      default:
        return topCategory!.icon ?? '💰';
    }
  }

  /// Chuyển đổi từ viết tắt sang tên đầy đủ (T2 -> Thứ 2)
  String get dayName {
    switch (dayOfWeek.toLowerCase()) {
      case 'mon':
      case 'monday':
        return 'T2';
      case 'tue':
      case 'tuesday':
        return 'T3';
      case 'wed':
      case 'wednesday':
        return 'T4';
      case 'thu':
      case 'thursday':
        return 'T5';
      case 'fri':
      case 'friday':
        return 'T6';
      case 'sat':
      case 'saturday':
        return 'T7';
      case 'sun':
      case 'sunday':
        return 'CN';
      default:
        return dayOfWeek;
    }
  }
}

class TopCategory {
  final String categoryName;
  final double amount;
  final String? icon;

  TopCategory({required this.categoryName, required this.amount, this.icon});

  factory TopCategory.fromJson(Map<String, dynamic> json) {
    return TopCategory(
      categoryName: json['category_name'] ?? json['name'] ?? '',
      amount: (json['amount'] ?? 0).toDouble(),
      icon: json['icon'],
    );
  }

  Map<String, dynamic> toJson() {
    return {'category_name': categoryName, 'amount': amount, 'icon': icon};
  }
}

class WeeklyInsight {
  final String highestDay;
  final double highestAmount;
  final String message;

  WeeklyInsight({
    required this.highestDay,
    required this.highestAmount,
    required this.message,
  });

  factory WeeklyInsight.fromJson(Map<String, dynamic> json) {
    return WeeklyInsight(
      highestDay: json['highest_day'] ?? '',
      highestAmount: (json['highest_amount'] ?? 0).toDouble(),
      message: json['message'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'highest_day': highestDay,
      'highest_amount': highestAmount,
      'message': message,
    };
  }
}
