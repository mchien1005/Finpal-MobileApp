class NotificationSettings {
  bool pushNotifications;
  bool pushEnabled;

  bool transactionAlerts;
  bool budgetAlerts;
  bool goalReminders;
  bool securityAlerts;
  bool savingsTips;
  bool spendingInsights;

  bool weeklyReports;
  bool monthlyReports;

  NotificationSettings({
    required this.pushNotifications,
    required this.pushEnabled,
    required this.transactionAlerts,
    required this.budgetAlerts,
    required this.goalReminders,
    required this.securityAlerts,
    required this.savingsTips,
    required this.spendingInsights,
    required this.weeklyReports,
    required this.monthlyReports,
  });

  factory NotificationSettings.fromJson(Map<String, dynamic> json) {
    bool parseBool(dynamic v) {
      if (v == null) return false;
      if (v is bool) return v;
      if (v is int) return v == 1;
      if (v is String) {
        final lower = v.toLowerCase();
        return lower == '1' || lower == 'true' || lower == 'yes' || lower == 'y';
      }
      return false;
    }

    return NotificationSettings(
      pushNotifications: parseBool(json['pushNotifications'] ?? json['push']),
      pushEnabled: parseBool(json['pushEnabled'] ?? json['push_enabled'] ?? json['push']),
      transactionAlerts: parseBool(json['transactionAlerts'] ?? json['transaction']),
      budgetAlerts: parseBool(json['budgetAlerts'] ?? json['budget']),
      goalReminders: parseBool(json['goalReminders'] ?? json['goal']),
      securityAlerts: parseBool(json['securityAlerts'] ?? json['security']),
      savingsTips: parseBool(json['savingsTips'] ?? json['savingsSuggestions'] ?? json['savings'] ?? json['savings_suggestions']),
      spendingInsights: parseBool(json['spendingInsights'] ?? json['spendingInsight'] ?? json['spending']),
      weeklyReports: parseBool(json['weeklyReport'] ?? json['weeklyReports'] ?? json['weekly'] ?? json['weeklyReportsInt'] ?? json['weekly_reports']),
      monthlyReports: parseBool(json['monthlyReport'] ?? json['monthlyReports'] ?? json['monthly'] ?? json['monthlyReportsInt'] ?? json['monthly_reports']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      // Long form
      'pushNotifications': pushNotifications,
      'pushEnabled': pushEnabled,
      'transactionAlerts': transactionAlerts,
      'budgetAlerts': budgetAlerts,
      'goalReminders': goalReminders,
      'securityAlerts': securityAlerts,
      'savingsTips': savingsTips,
      'spendingInsights': spendingInsights,
      'weeklyReport': weeklyReports,
      'monthlyReport': monthlyReports,

      // Short / alternative keys
      'push': pushNotifications,
      'push_enabled': pushEnabled,
      'transaction': transactionAlerts,
      'budget': budgetAlerts,
      'goal': goalReminders,
      'security': securityAlerts,
      'savings': savingsTips,
      'savings_suggestions': savingsTips,
      'spending': spendingInsights,
      'weekly': weeklyReports,
      'monthly': monthlyReports,

      // Numeric aliases (0/1)
      'weeklyReportInt': weeklyReports ? 1 : 0,
      'monthlyReportInt': monthlyReports ? 1 : 0,
      'savingsTipsInt': savingsTips ? 1 : 0,
      'spendingInsightsInt': spendingInsights ? 1 : 0,
    };
  }
}
