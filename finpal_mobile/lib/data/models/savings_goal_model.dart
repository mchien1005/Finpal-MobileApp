class SavingsGoalRequest {
  final String name;
  final String? description;
  final double targetAmount;
  final String deadline; // yyyy-MM-dd format

  SavingsGoalRequest({
    required this.name,
    this.description,
    required this.targetAmount,
    required this.deadline,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      if (description != null) 'description': description,
      'targetAmount': targetAmount,
      'deadline': deadline,
    };
  }
}

class SavingsContributionRequest {
  final double amount;
  final String? contributionDate; // yyyy-MM-dd format
  final String? notes;

  SavingsContributionRequest({
    required this.amount,
    this.contributionDate,
    this.notes,
  });

  Map<String, dynamic> toJson() {
    return {
      'amount': amount,
      if (contributionDate != null) 'contributionDate': contributionDate,
      if (notes != null) 'notes': notes,
    };
  }
}

class SavingsGoalResponse {
  final int id;
  final int? userId; // Optional because API might not return it
  final String name;
  final String? description;
  final double targetAmount;
  final double currentAmount;
  final String deadline;
  final String status;
  final DateTime createdAt;
  final DateTime updatedAt;
  final DateTime? completedAt;
  final double progressPercentage;
  final double remainingAmount;
  final int daysRemaining;
  final String progressStatus;
  final List<SavingsContributionResponse> recentContributions;

  SavingsGoalResponse({
    required this.id,
    this.userId, // Optional
    required this.name,
    this.description,
    required this.targetAmount,
    required this.currentAmount,
    required this.deadline,
    required this.status,
    required this.createdAt,
    required this.updatedAt,
    this.completedAt,
    required this.progressPercentage,
    required this.remainingAmount,
    required this.daysRemaining,
    required this.progressStatus,
    required this.recentContributions,
  });

  factory SavingsGoalResponse.fromJson(Map<String, dynamic> json) {
    return SavingsGoalResponse(
      id: json['id'] as int,
      userId: json['userId'] as int?, // Can be null
      name: json['name'] as String,
      description: json['description'] as String?,
      targetAmount: (json['targetAmount'] as num).toDouble(),
      currentAmount: (json['currentAmount'] as num).toDouble(),
      deadline: json['deadline'] as String,
      status: json['status'] as String,
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
      completedAt: json['completedAt'] != null
          ? DateTime.parse(json['completedAt'] as String)
          : null,
      progressPercentage: (json['progressPercentage'] as num).toDouble(),
      remainingAmount: (json['remainingAmount'] as num).toDouble(),
      daysRemaining: json['daysRemaining'] as int,
      progressStatus: json['progressStatus'] as String,
      recentContributions:
          (json['recentContributions'] as List<dynamic>?)
              ?.map(
                (e) => SavingsContributionResponse.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList() ??
          [],
    );
  }
}

class SavingsContributionResponse {
  final int id;
  final int savingsGoalId;
  final double amount;
  final String contributionDate;
  final String? notes;
  final DateTime createdAt;

  SavingsContributionResponse({
    required this.id,
    required this.savingsGoalId,
    required this.amount,
    required this.contributionDate,
    this.notes,
    required this.createdAt,
  });

  factory SavingsContributionResponse.fromJson(Map<String, dynamic> json) {
    return SavingsContributionResponse(
      id: json['id'] as int,
      savingsGoalId: json['savingsGoalId'] as int,
      amount: (json['amount'] as num).toDouble(),
      contributionDate: json['contributionDate'] as String,
      notes: json['notes'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
}
