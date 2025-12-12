import 'package:flutter/material.dart';

class NotificationsScreen extends StatefulWidget {
  const NotificationsScreen({super.key});

  @override
  State<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends State<NotificationsScreen> {
  // Notification Channel Settings
  bool _smsNotifications = true;
  bool _emailNotifications = true;
  bool _pushNotifications = true;

  // Notification Type Settings
  bool _transactionAlerts = true;
  bool _budgetAlerts = true;
  bool _goalReminders = true;
  bool _securityAlerts = true;

  // Periodic Reports
  bool _weeklyReports = false;
  bool _monthlyReports = true;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
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
                  color: const Color(0xFFD7006E),
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
                child: SingleChildScrollView(
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
                              icon: Icons.message_outlined,
                              iconColor: const Color(0xFF10B981),
                              title: 'Thông báo SMS',
                              subtitle: 'Nhận thông báo qua tin nhắn',
                              value: _smsNotifications,
                              onChanged: (value) {
                                setState(() {
                                  _smsNotifications = value;
                                });
                              },
                              showDivider: true,
                            ),
                            _buildNotificationItem(
                              icon: Icons.email_outlined,
                              iconColor: const Color(0xFF3B82F6),
                              title: 'Thông báo Email',
                              subtitle: 'Nhận thông báo qua email',
                              value: _emailNotifications,
                              onChanged: (value) {
                                setState(() {
                                  _emailNotifications = value;
                                });
                              },
                              showDivider: true,
                            ),
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
    // TODO: Save settings to backend
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Đã lưu cài đặt thành công'),
        backgroundColor: Color(0xFF10B981),
        duration: Duration(seconds: 2),
      ),
    );
  }
}
