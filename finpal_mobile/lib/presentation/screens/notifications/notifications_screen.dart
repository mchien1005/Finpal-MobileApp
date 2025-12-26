import 'dart:convert';
import 'package:flutter/material.dart';
import '../../../core/widgets/success_notification_dialog.dart';
import 'package:finpal_mobile/core/constants/app_colors.dart';
import 'package:finpal_mobile/data/services/notification_settings_service.dart';
import 'package:finpal_mobile/data/models/notification_settings.dart';
class NotificationsScreen extends StatefulWidget {
  const NotificationsScreen({super.key});

  @override
  State<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends State<NotificationsScreen> {
  final NotificationSettingsService _service = NotificationSettingsService();
  bool _isLoading = true;

  // Notification Channel Settings
  bool _pushNotifications = true;
  bool _pushEnabled = true;

  // Notification Type Settings
  bool _transactionAlerts = true;
  bool _budgetAlerts = true;
  bool _goalReminders = true;
  bool _securityAlerts = true;
  bool _savingsTips = true;
  bool _spendingInsights = true;

  // Periodic Reports
  bool _weeklyReports = false;
  bool _monthlyReports = true;

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    setState(() {
      _isLoading = true;
    });
    try {
      final NotificationSettings settings = await _service.getSettings();
      setState(() {
        _pushNotifications = settings.pushNotifications;
        _pushEnabled = settings.pushEnabled;

        _transactionAlerts = settings.transactionAlerts;
        _budgetAlerts = settings.budgetAlerts;
        _goalReminders = settings.goalReminders;
        _securityAlerts = settings.securityAlerts;
        _savingsTips = settings.savingsTips;
        _spendingInsights = settings.spendingInsights;

        _weeklyReports = settings.weeklyReports;
        _monthlyReports = settings.monthlyReports;
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Lỗi tải cài đặt: $e')),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      body: SafeArea(
        child: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment(0.26, -0.97),
              end: Alignment(-0.26, 0.97),
              colors: [Color(0xFFEFF6FF), Color(0xFFE0E7FF)],
            ),
          ),
          child: Column(
            children: [
              // App Bar
              Container(
                decoration: BoxDecoration(
                  color: AppColors.primary,
                  borderRadius: const BorderRadius.only(
                    bottomLeft: Radius.circular(24),
                    bottomRight: Radius.circular(24),
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 15,
                      offset: const Offset(0, 10),
                    ),
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 6,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(16, 8, 16, 20),
                  child: Row(
                    children: [
                      InkWell(
                        onTap: () => Navigator.pop(context),
                        borderRadius: BorderRadius.circular(8),
                        child: const Padding(
                          padding: EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 8,
                          ),
                          child: Row(
                            children: [
                              Icon(
                                Icons.arrow_back_ios,
                                color: Colors.white,
                                size: 14,
                              ),
                              SizedBox(width: 6),
                              Text(
                                'Quay lại',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 14,
                                  fontFamily: 'Arimo',
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const Spacer(),
                      const Text(
                        'Cài đặt thông báo',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          fontFamily: 'Arimo',
                        ),
                      ),
                      const Spacer(),
                      const SizedBox(width: 100),
                    ],
                  ),
                ),
              ),

              // Content
              Expanded(
                child: _isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : SingleChildScrollView(
                        padding: const EdgeInsets.fromLTRB(16, 24, 16, 24),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                      // Notification Channels Section
                      const Padding(
                        padding: EdgeInsets.only(left: 8, bottom: 12),
                        child: Text(
                          'Kênh thông báo',
                          style: TextStyle(
                            fontSize: 16,
                            color: Color(0xFF364153),
                            fontFamily: 'Arimo',
                          ),
                        ),
                      ),
                      Container(
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(14),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withOpacity(0.1),
                              blurRadius: 6,
                              offset: const Offset(0, 4),
                            ),
                            BoxShadow(
                              color: Colors.black.withOpacity(0.1),
                              blurRadius: 4,
                              offset: const Offset(0, 2),
                            ),
                          ],
                        ),
                        child: Column(
                          children: [
                            _buildNotificationItem(
                              icon: Icons.notifications_active_outlined,
                              iconColor: const Color(0xFF111827),
                              title: 'Tất cả thông báo',
                              subtitle: 'Bật/tắt tất cả các thông báo',
                              value: _pushEnabled,
                              onChanged: (value) {
                                setState(() {
                                  _pushEnabled = value;
                                  // when turning off, disable all channels and types
                                  _pushNotifications = value;
                                  _transactionAlerts = value;
                                  _budgetAlerts = value;
                                  _goalReminders = value;
                                  _securityAlerts = value;
                                  _savingsTips = value;
                                  _spendingInsights = value;
                                  _weeklyReports = value;
                                  _monthlyReports = value;
                                });
                              },
                              showDivider: true,
                            ),
                            // SMS and Email notification channels removed
                            _buildNotificationItem(
                              icon: Icons.notifications_outlined,
                              iconColor: const Color(0xFFA855F7),
                              title: 'Thông báo đẩy',
                              subtitle: 'Nhận thông báo trên thiết bị',
                              value: _pushNotifications,
                              onChanged: (value) {
                                setState(() {
                                  _pushNotifications = value;
                                });
                              },
                            ),
                          ],
                        ),
                      ),

                      const SizedBox(height: 24),

                      // Notification Types Section
                      const Padding(
                        padding: EdgeInsets.only(left: 8, bottom: 12),
                        child: Text(
                          'Loại thông báo',
                          style: TextStyle(
                            fontSize: 16,
                            color: Color(0xFF364153),
                            fontFamily: 'Arimo',
                          ),
                        ),
                      ),
                      Container(
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(14),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withOpacity(0.1),
                              blurRadius: 6,
                              offset: const Offset(0, 4),
                            ),
                            BoxShadow(
                              color: Colors.black.withOpacity(0.1),
                              blurRadius: 4,
                              offset: const Offset(0, 2),
                            ),
                          ],
                        ),
                        child: Column(
                          children: [
                            _buildNotificationItem(
                              icon: Icons.trending_up,
                              iconColor: const Color(0xFFEF4444),
                              title: 'Cảnh báo giao dịch',
                              subtitle: 'Thông báo khi có giao dịch mới',
                              value: _transactionAlerts,
                              onChanged: (value) {
                                setState(() {
                                  _transactionAlerts = value;
                                });
                              },
                              showDivider: true,
                            ),
                            _buildNotificationItem(
                              icon: Icons.trending_down,
                              iconColor: const Color(0xFFF97316),
                              title: 'Cảnh báo ngân sách',
                              subtitle: 'Thông báo khi vượt ngân sách',
                              value: _budgetAlerts,
                              onChanged: (value) {
                                setState(() {
                                  _budgetAlerts = value;
                                });
                              },
                              showDivider: true,
                            ),
                            _buildNotificationItem(
                              icon: Icons.track_changes,
                              iconColor: const Color(0xFF8B5CF6),
                              title: 'Nhắc nhở mục tiêu',
                              subtitle: 'Nhắc nhở về mục tiêu tiết kiệm',
                              value: _goalReminders,
                              onChanged: (value) {
                                setState(() {
                                  _goalReminders = value;
                                });
                              },
                              showDivider: true,
                            ),
                            _buildNotificationItem(
                              icon: Icons.lightbulb_outline,
                              iconColor: const Color(0xFF06B6D4),
                              title: 'Gợi ý tiết kiệm thông minh',
                              subtitle: 'Gợi ý cách tiết kiệm và mục tiêu',
                              value: _savingsTips,
                              onChanged: (value) {
                                setState(() {
                                  _savingsTips = value;
                                });
                              },
                              showDivider: true,
                            ),
                            _buildNotificationItem(
                              icon: Icons.analytics_outlined,
                              iconColor: const Color(0xFFEF4444),
                              title: 'Phân tích chi tiêu',
                              subtitle: 'Insights và phân tích chi tiêu của bạn',
                              value: _spendingInsights,
                              onChanged: (value) {
                                setState(() {
                                  _spendingInsights = value;
                                });
                              },
                              showDivider: true,
                            ),
                            _buildNotificationItem(
                              icon: Icons.shield_outlined,
                              iconColor: const Color(0xFFF59E0B),
                              title: 'Cảnh báo bảo mật',
                              subtitle: 'Thông báo về hoạt động bất thường',
                              value: _securityAlerts,
                              onChanged: (value) {
                                setState(() {
                                  _securityAlerts = value;
                                });
                              },
                            ),
                          ],
                        ),
                      ),

                      const SizedBox(height: 24),

                      // Periodic Reports Section
                      const Padding(
                        padding: EdgeInsets.only(left: 8, bottom: 12),
                        child: Text(
                          'Báo cáo định kỳ',
                          style: TextStyle(
                            fontSize: 16,
                            color: Color(0xFF364153),
                            fontFamily: 'Arimo',
                          ),
                        ),
                      ),
                      Container(
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(14),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withOpacity(0.1),
                              blurRadius: 6,
                              offset: const Offset(0, 4),
                            ),
                            BoxShadow(
                              color: Colors.black.withOpacity(0.1),
                              blurRadius: 4,
                              offset: const Offset(0, 2),
                            ),
                          ],
                        ),
                        child: Column(
                          children: [
                            _buildNotificationItem(
                              icon: Icons.calendar_view_week_outlined,
                              iconColor: const Color(0xFF06B6D4),
                              title: 'Báo cáo tuần',
                              subtitle: 'Tóm tắt chi tiêu hàng tuần',
                              value: _weeklyReports,
                              onChanged: (value) {
                                setState(() {
                                  _weeklyReports = value;
                                });
                              },
                              showDivider: true,
                            ),
                            _buildNotificationItem(
                              icon: Icons.calendar_month_outlined,
                              iconColor: const Color(0xFF8B5CF6),
                              title: 'Báo cáo tháng',
                              subtitle: 'Tóm tắt chi tiêu hàng tháng',
                              value: _monthlyReports,
                              onChanged: (value) {
                                setState(() {
                                  _monthlyReports = value;
                                });
                              },
                            ),
                          ],
                        ),
                      ),

                      const SizedBox(height: 24),

                      // Save Button
                      SizedBox(
                        width: double.infinity,
                        height: 36,
                        child: ElevatedButton(
                          onPressed: _handleSaveSettings,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF155DFC),
                            foregroundColor: Colors.white,
                            elevation: 0,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(8),
                            ),
                          ),
                          child: const Text(
                            'Lưu cài đặt',
                            style: TextStyle(
                              fontFamily: 'Arimo',
                              fontSize: 14,
                              fontWeight: FontWeight.normal,
                            ),
                          ),
                        ),
                      ),

                      const SizedBox(height: 8),
                      SizedBox(
                        width: double.infinity,
                        height: 36,
                        child: TextButton(
                          onPressed: _handleResetSettings,
                          child: const Text('Reset về mặc định'),
                        ),
                      ),
                    ],
                        ),
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNotificationItem({
    required IconData icon,
    required Color iconColor,
    required String title,
    required String subtitle,
    required bool value,
    required ValueChanged<bool> onChanged,
    bool showDivider = false,
  }) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
          child: Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: const Color(0xFFF9FAFB),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(
                  icon,
                  size: 20,
                  color: iconColor,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(
                        fontSize: 14,
                        color: Color(0xFF101828),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      subtitle,
                      style: const TextStyle(
                        fontSize: 14,
                        color: Color(0xFF6A7282),
                        fontFamily: 'Arimo',
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 16),
              Switch(
                value: value,
                onChanged: onChanged,
                activeColor: Colors.white,
                activeTrackColor: const Color(0xFF030213),
                inactiveThumbColor: Colors.white,
                inactiveTrackColor: const Color(0xFFCBCED4),
              ),
            ],
          ),
        ),
        if (showDivider)
          const Divider(
            height: 1,
            thickness: 1,
            color: Color(0xFFF3F4F6),
          ),
      ],
    );
  }

  void _handleSaveSettings() {
    _saveSettings();
  }

  Future<void> _saveSettings() async {
    final settings = NotificationSettings(
      pushNotifications: _pushNotifications,
      pushEnabled: _pushEnabled,
      transactionAlerts: _transactionAlerts,
      budgetAlerts: _budgetAlerts,
      goalReminders: _goalReminders,
      securityAlerts: _securityAlerts,
      savingsTips: _savingsTips,
      spendingInsights: _spendingInsights,
      weeklyReports: _weeklyReports,
      monthlyReports: _monthlyReports,
    );
    final body = settings.toJson();

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => const Center(child: CircularProgressIndicator()),
    );

    try {
      print('🔁 Sending notification settings');
      await _service.updateSettings(settings);
      Navigator.of(context).pop(); // close loading
      SuccessNotificationDialog.show(
        context,
        message: 'Đã lưu cài đặt thành công',
      );
    } catch (e) {
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Lỗi lưu cài đặt: $e')),
      );
    }
  }

  Future<void> _handleResetSettings() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Xác nhận'),
        content: const Text('Bạn có muốn reset cài đặt về mặc định không?'),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Hủy')),
          TextButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Xác nhận')),
        ],
      ),
    );

    if (confirmed != true) return;

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => const Center(child: CircularProgressIndicator()),
    );

    try {
      await _service.resetSettings();
      Navigator.of(context).pop();
      await _loadSettings();
      SuccessNotificationDialog.show(
        context,
        message: 'Đã reset về mặc định',
      );
    } catch (e) {
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Lỗi reset cài đặt: $e')),
      );
    }
  }
}
