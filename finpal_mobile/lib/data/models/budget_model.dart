// Budget Model - Model cho ngân sách
// Dùng để tạo và quản lý ngân sách theo danh mục

/// Request model để tạo ngân sách
class BudgetRequest {
  final String name;
  final double amount;
  final String period; // WEEKLY, MONTHLY, YEARLY
  final DateTime startDate;
  final DateTime? endDate;
  final int? categoryId;

  BudgetRequest({
    required this.name,
    required this.amount,
    required this.period,
    required this.startDate,
    this.endDate,
    this.categoryId,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'amount': amount,
      'period': period,
      'startDate':
          '${startDate.year}-${startDate.month.toString().padLeft(2, '0')}-${startDate.day.toString().padLeft(2, '0')}',
      if (endDate != null)
        'endDate':
            '${endDate!.year}-${endDate!.month.toString().padLeft(2, '0')}-${endDate!.day.toString().padLeft(2, '0')}',
      if (categoryId != null) 'categoryId': categoryId,
    };
  }
}

// Response model từ API
class BudgetResponse {
  final int id;
  final int userId;
  final String name;
  final double amount;
  final double spentAmount;
  final String period;
  final DateTime startDate;
  final DateTime? endDate;
  final int? categoryId;
  final String? categoryName;
  final String? categoryIcon;
  final String? categoryColor; // Màu sắc từ CSDL (hex format: #FF7043)
  final double progressPercentage;
  final double remainingAmount;
  final String status;
  final int daysRemaining;
  final double usagePercentage;
  final bool isActive;
  final DateTime createdAt;
  final DateTime updatedAt;

  BudgetResponse({
    required this.id,
    required this.userId,
    required this.name,
    required this.amount,
    required this.spentAmount,
    required this.period,
    required this.startDate,
    this.endDate,
    this.categoryId,
    this.categoryName,
    this.categoryIcon,
    this.categoryColor,
    required this.progressPercentage,
    required this.remainingAmount,
    required this.status,
    required this.daysRemaining,
    required this.usagePercentage,
    this.isActive = true,
    required this.createdAt,
    required this.updatedAt,
  });

  factory BudgetResponse.fromJson(Map<String, dynamic> json) {
    // Parse daysRemaining an toàn
    int parseDaysRemaining() {
      final value = json['daysRemaining'];
      if (value == null) return 0;
      if (value is int) return value;
      if (value is num) return value.toInt();
      return 0;
    }

    // Parse usagePercentage an toàn
    double parseUsagePercentage() {
      final value = json['usagePercentage'];
      if (value == null) return 0;
      if (value is num) return value.toDouble();
      return 0;
    }

    // Parse progressPercentage an toàn (có thể dùng usagePercentage nếu không có)
    double parseProgressPercentage() {
      final progress = json['progressPercentage'];
      if (progress != null && progress is num) return progress.toDouble();
      final usage = json['usagePercentage'];
      if (usage != null && usage is num) return usage.toDouble();
      return 0;
    }

    return BudgetResponse(
      id: json['id'] as int,
      userId: json['userId'] as int,
      name: json['name'] as String,
      amount: (json['amount'] as num).toDouble(),
      spentAmount: (json['spentAmount'] as num?)?.toDouble() ?? 0,
      period: json['period'] as String,
      startDate: DateTime.parse(json['startDate'] as String),
      endDate: json['endDate'] != null
          ? DateTime.parse(json['endDate'] as String)
          : null,
      categoryId: json['categoryId'] as int?,
      categoryName: json['categoryName'] as String?,
      categoryIcon: json['categoryIcon'] as String?,
      categoryColor: json['categoryColor'] as String?,
      progressPercentage: parseProgressPercentage(),
      remainingAmount: (json['remainingAmount'] as num?)?.toDouble() ?? 0,
      status: json['status'] as String? ?? 'ACTIVE',
      daysRemaining: parseDaysRemaining(),
      usagePercentage: parseUsagePercentage(),
      isActive: json['isActive'] as bool? ?? true,
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : DateTime.now(),
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'] as String)
          : DateTime.now(),
    );
  }

  /// Lấy trạng thái hiển thị tiếng Việt
  String get statusDisplay {
    switch (status) {
      case 'EXCEEDED':
        return 'Vượt ngân sách';
      case 'WARNING':
        return 'Sắp hết';
      case 'OK':
        return 'Bình thường';
      case 'ACTIVE':
        return 'Đang hoạt động';
      default:
        return status;
    }
  }
}

// Danh sách các kỳ hạn ngân sách
enum BudgetPeriod {
  weekly('WEEKLY', 'Hàng tuần'),
  monthly('MONTHLY', 'Hàng tháng'),
  quaterly('QUATERLY', 'Hàng quý'),
  yearly('YEARLY', 'Hàng năm');

  final String value;
  final String displayName;
  const BudgetPeriod(this.value, this.displayName);
}
