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
  final double progressPercentage;
  final double remainingAmount;
  final String status;
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
    required this.progressPercentage,
    required this.remainingAmount,
    required this.status,
    required this.createdAt,
    required this.updatedAt,
  });

  factory BudgetResponse.fromJson(Map<String, dynamic> json) {
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
      progressPercentage: (json['progressPercentage'] as num?)?.toDouble() ?? 0,
      remainingAmount: (json['remainingAmount'] as num?)?.toDouble() ?? 0,
      status: json['status'] as String? ?? 'ACTIVE',
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
    );
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
