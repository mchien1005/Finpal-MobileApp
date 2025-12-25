class NotificationSettings {
  bool smsNotifications;
  bool emailNotifications;
  bool pushNotifications;

  bool transactionAlerts;
  bool budgetAlerts;
  bool goalReminders;
  bool securityAlerts;

  bool weeklyReports;
  bool monthlyReports;

  NotificationSettings({
    required this.smsNotifications,
    required this.emailNotifications,
    required this.pushNotifications,
    required this.transactionAlerts,
    required this.budgetAlerts,
    required this.goalReminders,
    required this.securityAlerts,
    required this.weeklyReports,
    required this.monthlyReports,
  });

  factory NotificationSettings.fromJson(Map<String, dynamic> json) {
    return NotificationSettings(
      smsNotifications: json['smsNotifications'] ?? json['sms'] ?? false,
      emailNotifications: json['emailNotifications'] ?? json['email'] ?? false,
      pushNotifications: json['pushNotifications'] ?? json['push'] ?? false,
      transactionAlerts: json['transactionAlerts'] ?? json['transaction'] ?? false,
      budgetAlerts: json['budgetAlerts'] ?? json['budget'] ?? false,
      goalReminders: json['goalReminders'] ?? json['goal'] ?? false,
      securityAlerts: json['securityAlerts'] ?? json['security'] ?? false,
      weeklyReports: json['weeklyReports'] ?? json['weekly'] ?? false,
      monthlyReports: json['monthlyReports'] ?? json['monthly'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'smsNotifications': smsNotifications,
      'emailNotifications': emailNotifications,
      'pushNotifications': pushNotifications,
      'transactionAlerts': transactionAlerts,
      'budgetAlerts': budgetAlerts,
      'goalReminders': goalReminders,
      'securityAlerts': securityAlerts,
      'weeklyReports': weeklyReports,
      'monthlyReports': monthlyReports,
    };
  }
}
