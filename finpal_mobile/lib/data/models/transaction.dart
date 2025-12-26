class Transaction {
  final int id;
  final String type;
  final double amount;
  final String transactionSource;
  final int? categoryId;
  final String? description;
  final String? merchant;
  final DateTime transactionDate;
  final bool isAuto;
  final TransactionCategory? category;

  Transaction({
    required this.id,
    required this.type,
    required this.amount,
    required this.transactionSource,
    this.categoryId,
    this.description,
    this.merchant,
    required this.transactionDate,
    this.isAuto = false,
    this.category,
  });

  bool get isIncome => type.toUpperCase() == 'INCOME';
  bool get isExpense => type.toUpperCase() == 'EXPENSE';

  factory Transaction.fromJson(Map<String, dynamic> json) {
    return Transaction(
      id: json['id'] as int,
      type: json['type'] as String,
      amount: (json['amount'] as num).toDouble(),
      transactionSource: json['transactionSource'] as String,
      categoryId: json['categoryId'] as int?,
      description: json['description'] as String?,
      merchant: json['merchant'] as String?,
      transactionDate: DateTime.parse(json['transactionDate'] as String),
      isAuto: json['isAuto'] as bool? ?? false,
      category: json['category'] != null
          ? TransactionCategory.fromJson(json['category'] as Map<String, dynamic>)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'type': type,
      'amount': amount,
      'transactionSource': transactionSource,
      'categoryId': categoryId,
      'description': description,
      'merchant': merchant,
      'transactionDate': transactionDate.toIso8601String(),
      'isAuto': isAuto,
      'category': category?.toJson(),
    };
  }
}

class TransactionCategory {
  final int id;
  final String name;
  final String? icon;

  TransactionCategory({
    required this.id,
    required this.name,
    this.icon,
  });

  factory TransactionCategory.fromJson(Map<String, dynamic> json) {
    return TransactionCategory(
      id: json['id'] as int,
      name: json['name'] as String,
      icon: json['icon'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'icon': icon,
    };
  }
}
