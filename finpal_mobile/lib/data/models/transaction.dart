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
      transactionDate: _parseDateTime(json['transactionDate'] as String),
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

DateTime _parseDateTime(String ts) {
  // If the timestamp contains timezone info (Z or +/-) parse and convert to local.
  // If it lacks timezone information, assume the server already returned a local
  // timestamp and parse it as-is (no UTC conversion) so the displayed time matches
  // what the server intended.
  final tzMatch = RegExp(r'[Zz]|[+-]\d{2}:?\d{2}');
  if (tzMatch.hasMatch(ts)) {
    return DateTime.parse(ts).toLocal();
  }

  // No timezone present. Parse components and construct a local DateTime so
  // Dart does not treat the string as UTC and shift by timezone.
  final match = RegExp(r'^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?').firstMatch(ts);
  if (match != null) {
    final y = int.parse(match.group(1)!);
    final m = int.parse(match.group(2)!);
    final d = int.parse(match.group(3)!);
    final hh = int.parse(match.group(4)!);
    final mm = int.parse(match.group(5)!);
    final ss = match.group(6) != null ? int.parse(match.group(6)!) : 0;
    return DateTime(y, m, d, hh, mm, ss);
  }

  // Fallback to default parser
  return DateTime.parse(ts);
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
