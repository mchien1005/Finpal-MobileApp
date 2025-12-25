class DashboardSummary {
  final double totalIncome;
  final double totalExpense;
  final double balance;
  final double savingsProgress;
  final double budgetUsed;
  final double budgetLimit;

  DashboardSummary({
    required this.totalIncome,
    required this.totalExpense,
    required this.balance,
    this.savingsProgress = 0.0,
    this.budgetUsed = 0.0,
    this.budgetLimit = 0.0,
  });

  factory DashboardSummary.fromJson(Map<String, dynamic> json) {
    return DashboardSummary(
      totalIncome: (json['totalIncome'] ?? json['monthlyIncome'] ?? 0).toDouble(),
      totalExpense: (json['totalExpense'] ?? json['monthlyExpense'] ?? 0).toDouble(),
      balance: (json['balance'] ?? json['totalBalance'] ?? 0).toDouble(),
      savingsProgress: (json['savingsProgress'] ?? json['netSavings'] ?? 0).toDouble(),
      budgetUsed: (json['budgetUsed'] ?? json['monthlyExpense'] ?? 0).toDouble(),
      budgetLimit: (json['budgetLimit'] ?? 0).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'totalIncome': totalIncome,
      'totalExpense': totalExpense,
      'balance': balance,
      'savingsProgress': savingsProgress,
      'budgetUsed': budgetUsed,
      'budgetLimit': budgetLimit,
    };
  }

  double get budgetProgress {
    if (budgetLimit <= 0) return 0.0;
    return budgetUsed / budgetLimit;
  }
}

class CategorySpending {
  final String category;
  final String categoryName;
  final double amount;
  final double percentage;
  final String color;
  final int? transactionCount;

  CategorySpending({
    required this.category,
    required this.categoryName,
    required this.amount,
    required this.percentage,
    this.color = '#6C63FF',
    this.transactionCount,
  });

  factory CategorySpending.fromJson(Map<String, dynamic> json) {
    return CategorySpending(
      category: json['category'] ?? json['categoryName'] ?? '',
      categoryName: json['categoryName'] ?? json['category'] ?? 'Khác',
      amount: (json['amount'] ?? json['totalAmount'] ?? 0).toDouble(),
      percentage: (json['percentage'] ?? 0).toDouble(),
      color: json['color'] ?? json['categoryColor'] ?? '#6C63FF',
      transactionCount: json['transactionCount'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'category': category,
      'categoryName': categoryName,
      'amount': amount,
      'percentage': percentage,
      'color': color,
      'transactionCount': transactionCount,
    };
  }
}

class MonthlyTrend {
  final int year;
  final int monthNumber;
  final String monthName;
  final String month; // Formatted month for display (e.g., "T11", "T12")
  final double income;
  final double expense;
  final double netSavings;
  final int incomeCount;
  final int expenseCount;

  MonthlyTrend({
    required this.year,
    required this.monthNumber,
    required this.monthName,
    required this.month,
    required this.income,
    required this.expense,
    required this.netSavings,
    required this.incomeCount,
    required this.expenseCount,
  });

  factory MonthlyTrend.fromJson(Map<String, dynamic> json) {
    final monthNum = json['month'] ?? 0;
    final monthName = json['monthName']?.toString() ?? '';
    
    // Format month for display: T1, T2, ..., T12
    final displayMonth = 'T$monthNum';
    
    return MonthlyTrend(
      year: json['year'] ?? 0,
      monthNumber: monthNum,
      monthName: monthName,
      month: displayMonth,
      income: (json['totalIncome'] ?? 0).toDouble(),
      expense: (json['totalExpense'] ?? 0).toDouble(),
      netSavings: (json['netSavings'] ?? 0).toDouble(),
      incomeCount: json['incomeCount'] ?? 0,
      expenseCount: json['expenseCount'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'year': year,
      'month': monthNumber,
      'monthName': monthName,
      'totalIncome': income,
      'totalExpense': expense,
      'netSavings': netSavings,
      'incomeCount': incomeCount,
      'expenseCount': expenseCount,
    };
  }
}

class CashFlow {
  final List<MonthlyTrend> monthlyData;
  final double averageIncome;
  final double averageExpense;
  final double totalIncome;
  final double totalExpense;
  final double savings;
  final double budgetLimit;
  final double budgetUsed;

  CashFlow({
    required this.monthlyData,
    this.averageIncome = 0.0,
    this.averageExpense = 0.0,
    this.totalIncome = 0.0,
    this.totalExpense = 0.0,
    this.savings = 0.0,
    this.budgetLimit = 0.0,
    this.budgetUsed = 0.0,
  });

  factory CashFlow.fromJson(Map<String, dynamic> json) {
    final monthlyList = (json['monthlyData'] ?? []) as List;
    return CashFlow(
      monthlyData:
          monthlyList.map((item) => MonthlyTrend.fromJson(item)).toList(),
      averageIncome: (json['averageIncome'] ?? 0).toDouble(),
      averageExpense: (json['averageExpense'] ?? 0).toDouble(),
      totalIncome: (json['totalIncome'] ?? json['income'] ?? 0).toDouble(),
      totalExpense: (json['totalExpense'] ?? json['expense'] ?? 0).toDouble(),
      savings: (json['savings'] ?? json['netSavings'] ?? 0).toDouble(),
      budgetLimit: (json['budgetLimit'] ?? json['budget'] ?? 0).toDouble(),
      budgetUsed: (json['budgetUsed'] ?? json['spent'] ?? json['totalExpense'] ?? 0).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'monthlyData': monthlyData.map((item) => item.toJson()).toList(),
      'averageIncome': averageIncome,
      'averageExpense': averageExpense,
      'totalIncome': totalIncome,
      'totalExpense': totalExpense,
      'savings': savings,
      'budgetLimit': budgetLimit,
      'budgetUsed': budgetUsed,
    };
  }

  double get budgetProgress {
    if (budgetLimit <= 0) return 0.0;
    return (budgetUsed / budgetLimit).clamp(0.0, 1.0);
  }

  double get budgetPercentage {
    if (budgetLimit <= 0) return 0.0;
    return (budgetUsed / budgetLimit * 100).clamp(0.0, 100.0);
  }
}

class BudgetItem {
  final int id;
  final String name;
  final String category;
  final double amount;
  final double spent;
  final String period;
  final DateTime startDate;
  final DateTime endDate;

  BudgetItem({
    required this.id,
    required this.name,
    required this.category,
    required this.amount,
    required this.spent,
    required this.period,
    required this.startDate,
    required this.endDate,
  });

  factory BudgetItem.fromJson(Map<String, dynamic> json) {
    return BudgetItem(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      category: json['category'] ?? '',
      amount: (json['amount'] ?? json['limit'] ?? 0).toDouble(),
      spent: (json['spent'] ?? json['used'] ?? 0).toDouble(),
      period: json['period'] ?? 'monthly',
      startDate: DateTime.parse(json['startDate'] ?? DateTime.now().toIso8601String()),
      endDate: DateTime.parse(json['endDate'] ?? DateTime.now().toIso8601String()),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'category': category,
      'amount': amount,
      'spent': spent,
      'period': period,
      'startDate': startDate.toIso8601String(),
      'endDate': endDate.toIso8601String(),
    };
  }

  double get progress => amount > 0 ? (spent / amount).clamp(0.0, 1.0) : 0.0;
  double get percentage => amount > 0 ? (spent / amount * 100).clamp(0.0, 100.0) : 0.0;
  double get remaining => (amount - spent).clamp(0.0, double.infinity);
}

class CategoryBudget {
  final int categoryId;
  final String categoryName;
  final double budgetAmount;
  final double spentAmount;
  final double percentage;
  final DateTime startDate;
  final DateTime endDate;
  final String status;

  CategoryBudget({
    required this.categoryId,
    required this.categoryName,
    required this.budgetAmount,
    required this.spentAmount,
    required this.percentage,
    required this.startDate,
    required this.endDate,
    required this.status,
  });

  factory CategoryBudget.fromJson(Map<String, dynamic> json) {
    return CategoryBudget(
      categoryId: json['categoryId'] ?? 0,
      categoryName: json['categoryName'] ?? 'Khác',
      budgetAmount: (json['budgetAmount'] ?? 0).toDouble(),
      spentAmount: (json['spentAmount'] ?? 0).toDouble(),
      percentage: (json['percentage'] ?? 0).toDouble(),
      startDate: DateTime.parse(json['startDate'] ?? DateTime.now().toIso8601String()),
      endDate: DateTime.parse(json['endDate'] ?? DateTime.now().toIso8601String()),
      status: json['status'] ?? 'ON_TRACK',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'categoryId': categoryId,
      'categoryName': categoryName,
      'budgetAmount': budgetAmount,
      'spentAmount': spentAmount,
      'percentage': percentage,
      'startDate': startDate.toIso8601String(),
      'endDate': endDate.toIso8601String(),
      'status': status,
    };
  }

  double get progress => budgetAmount > 0 ? (spentAmount / budgetAmount).clamp(0.0, 1.0) : 0.0;
  double get remaining => (budgetAmount - spentAmount).clamp(0.0, double.infinity);
  bool get isExceeded => status == 'EXCEEDED' || percentage >= 100;
  bool get isWarning => percentage >= 80 && percentage < 100;
  bool get isOnTrack => percentage < 80;
}
