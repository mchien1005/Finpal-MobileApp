import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../core/constants/app_colors.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

class NotificationItem {
  final Widget icon;
  final Color iconColor;
  final Color iconBgColor;
  final String title;
  final String message;
  final String time;
  bool isRead;

  NotificationItem({
    required this.icon,
    required this.iconColor,
    required this.iconBgColor,
    required this.title,
    required this.message,
    required this.time,
    this.isRead = false,
  });
}

class NotificationScreen extends StatefulWidget {
  const NotificationScreen({super.key});

  @override
  State<NotificationScreen> createState() => _NotificationScreenState();
}

class _NotificationScreenState extends State<NotificationScreen> {
  final List<NotificationItem> _unreadNotifications = [
    NotificationItem(
      icon: SvgPicture.asset(
        'assets/icons/arrow_down.svg',
        width: 24,
        height: 24,
      ),
      iconColor: const Color(0xFFEF4444),
      iconBgColor: const Color(0xFFFEE2E2),
      title: 'Giao dịch mới',
      message: 'Bạn vừa chi tiêu 450.000đ tại Shopee',
      time: '5 phút trước',
      isRead: false,
    ),
    NotificationItem(
      icon: const Icon(Icons.warning_amber_outlined, size: 24),
      iconColor: const Color(0xFFF54900),
      iconBgColor: const Color(0xFFFFEDD5),
      title: 'Cảnh báo ngân sách',
      message: 'Bạn đã sử dụng 85% ngân sách "Mua sắm" tháng này',
      time: '1 giờ trước',
      isRead: false,
    ),
    NotificationItem(
      icon: Icon(MdiIcons.bullseye, size: 24),
      iconColor: const Color(0xFF00A63E),
      iconBgColor: const Color(0xFFDCFCE7),
      title: 'Mục tiêu đạt được',
      message: 'Chúc mừng! Bạn đã đạt 50% mục tiêu "Du lịch Đà Lạt"',
      time: '3 giờ trước',
      isRead: false,
    ),
  ];

  final List<NotificationItem> _readNotifications = [
    NotificationItem(
      icon: SvgPicture.asset(
        'assets/icons/arrow_up.svg',
        width: 24,
        height: 24,
      ),
      iconColor: const Color(0xFFEFF6FF),
      iconBgColor: const Color(0xFFEFF6FF),
      title: 'Nhận tiền',
      message: 'Bạn vừa nhận được 2.000.000đ từ Nguyễn Văn A',
      time: '5 giờ trước',
      isRead: true,
    ),
    NotificationItem(
      icon: const Icon(Icons.shield_outlined, size: 24),
      iconColor: const Color(0xFFD08700),
      iconBgColor: const Color(0xFFFEF9C3),
      title: 'Đăng nhập mới',
      message: 'Thiết bị mới đã đăng nhập vào tài khoản của bạn',
      time: '1 ngày trước',
      isRead: true,
    ),
    NotificationItem(
      icon: const Icon(Icons.info_outline, size: 24),
      iconColor: const Color(0xFF4F39F6),
      iconBgColor: const Color(0xFFE0E7FF),
      title: 'Cập nhật hệ thống',
      message: 'FinPal đã cập nhật lên phiên bản 1.0.0 với nhiều tính năng mới',
      time: '2 ngày trước',
      isRead: true,
    ),
    NotificationItem(
      icon: SvgPicture.asset(
        'assets/icons/arrow_down.svg',
        width: 24,
        height: 24,
      ),
      iconColor: const Color(0xFFFEE2E2),
      iconBgColor: const Color(0xFFFEE2E2),
      title: 'Vượt ngân sách',
      message: 'Bạn đã vượt ngân sách "Ăn uống" 200.000đ trong tuần này',
      time: '3 ngày trước',
      isRead: true,
    ),
  ];

  void _markAsRead(NotificationItem notification) {
    setState(() {
      notification.isRead = true;
      _unreadNotifications.remove(notification);
      _readNotifications.insert(0, notification);
    });
  }

  void _markAllAsRead() {
    setState(() {
      _readNotifications.insertAll(0, _unreadNotifications);
      _unreadNotifications.clear();
    });
  }

  void _deleteNotification(NotificationItem notification) {
    setState(() {
      _unreadNotifications.remove(notification);
      _readNotifications.remove(notification);
    });
  }

  void _deleteAllNotifications() {
    setState(() {
      _unreadNotifications.clear();
      _readNotifications.clear();
    });
  }

  void _showNotificationOptions(NotificationItem notification, bool isRead) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) => Container(
        padding: const EdgeInsets.fromLTRB(0, 6, 0, 20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Drag handle
            Container(
              width: 48,
              height: 4,
              decoration: BoxDecoration(
                color: const Color(0xFFD1D5DC),
                borderRadius: BorderRadius.circular(100),
              ),
            ),
            const SizedBox(height: 14),
            // Title with close button
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Stack(
                children: [
                  const Center(
                    child: Text(
                      'Tuỳ chọn',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        fontFamily: 'Arimo',
                        color: Color(0xFF101828),
                      ),
                    ),
                  ),
                  Positioned(
                    right: 0,
                    child: InkWell(
                      onTap: () => Navigator.pop(context),
                      borderRadius: BorderRadius.circular(20),
                      child: const Padding(
                        padding: EdgeInsets.all(4),
                        child: Icon(
                          Icons.close,
                          color: Color(0xFF4A5565),
                          size: 20,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            // Divider
            Container(height: 1, color: const Color(0xFF99A1AF)),
            const SizedBox(height: 8),
            // Options
            InkWell(
              onTap: () {
                Navigator.pop(context);
                if (isRead) {
                  setState(() {
                    notification.isRead = false;
                    _readNotifications.remove(notification);
                    _unreadNotifications.insert(0, notification);
                  });
                } else {
                  _markAsRead(notification);
                }
              },
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 8,
                ),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: const Color(0xFFECEEF2),
                        borderRadius: BorderRadius.circular(100),
                      ),
                      child: const Icon(
                        Icons.check,
                        size: 20,
                        color: Color(0xFF101828),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Text(
                      isRead ? 'Đánh dấu chưa đọc' : 'Đánh dấu đã đọc',
                      style: const TextStyle(
                        fontSize: 16,
                        fontFamily: 'Arimo',
                        color: Color(0xFF101828),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            InkWell(
              onTap: () {
                Navigator.pop(context);
                _deleteNotification(notification);
              },
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 8,
                ),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: const Color(0xFFECECF0),
                        borderRadius: BorderRadius.circular(100),
                      ),
                      child: Icon(
                        MdiIcons.trashCanOutline,
                        size: 20,
                        color: const Color(0xFF101828),
                      ),
                    ),
                    const SizedBox(width: 16),
                    const Text(
                      'Xoá thông báo',
                      style: TextStyle(
                        fontSize: 16,
                        fontFamily: 'Arimo',
                        color: Color(0xFF101828),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      body: SafeArea(
        child: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [Color(0xFFEFF6FF), Color(0xFFE0E7FF)],
            ),
          ),
          child: Column(
            children: [
              // Custom App Bar
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
                  padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
                  child: Column(
                    children: [
                      // Title row with back button
                      Row(
                        children: [
                          InkWell(
                            onTap: () => Navigator.pop(context),
                            borderRadius: BorderRadius.circular(8),
                            child: const Padding(
                              padding: EdgeInsets.symmetric(
                                horizontal: 4,
                                vertical: 8,
                              ),
                              child: Row(
                                children: [
                                  Icon(
                                    Icons.arrow_back_ios,
                                    color: Colors.white,
                                    size: 14,
                                  ),
                                  SizedBox(width: 4),
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
                            'Thông báo',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              fontFamily: 'Arimo',
                            ),
                          ),
                          const Spacer(),
                          const SizedBox(width: 80),
                        ],
                      ),
                      const SizedBox(height: 16),
                      // Action buttons row
                      Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          InkWell(
                            onTap: _markAllAsRead,
                            borderRadius: BorderRadius.circular(8),
                            child: const Padding(
                              padding: EdgeInsets.symmetric(
                                horizontal: 4,
                                vertical: 4,
                              ),
                              child: Row(
                                children: [
                                  Icon(
                                    Icons.done_all,
                                    size: 16,
                                    color: Colors.white,
                                  ),
                                  SizedBox(width: 4),
                                  Text(
                                    'Đọc tất cả',
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
                          const SizedBox(width: 16),
                          InkWell(
                            onTap: _deleteAllNotifications,
                            borderRadius: BorderRadius.circular(8),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 4,
                                vertical: 4,
                              ),
                              child: Row(
                                children: [
                                  Icon(
                                    MdiIcons.trashCanOutline,
                                    size: 16,
                                    color: Colors.white,
                                  ),
                                  const SizedBox(width: 4),
                                  const Text(
                                    'Xóa tất cả',
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
                        ],
                      ),
                    ],
                  ),
                ),
              ),

              // Notification List
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    // Unread section
                    if (_unreadNotifications.isNotEmpty) ...[
                      const Padding(
                        padding: EdgeInsets.only(left: 8, bottom: 10),
                        child: Text(
                          'Chưa đọc',
                          style: TextStyle(
                            fontSize: 14,
                            color: Color(0xFF364153),
                            fontFamily: 'Arimo',
                          ),
                        ),
                      ),
                      ..._unreadNotifications.map(
                        (notification) =>
                            _buildNotificationCard(notification, false),
                      ),
                      const SizedBox(height: 20),
                    ],

                    // Read section
                    if (_readNotifications.isNotEmpty) ...[
                      Container(
                        height: 1,
                        margin: const EdgeInsets.only(bottom: 9),
                        decoration: BoxDecoration(
                          color: Colors.black.withOpacity(0.1),
                        ),
                      ),
                      const Padding(
                        padding: EdgeInsets.only(left: 8, bottom: 10),
                        child: Text(
                          'Đã đọc',
                          style: TextStyle(
                            fontSize: 14,
                            color: Color(0xFF364153),
                            fontFamily: 'Arimo',
                          ),
                        ),
                      ),
                      ..._readNotifications.map(
                        (notification) =>
                            _buildNotificationCard(notification, true),
                      ),
                    ],
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNotificationCard(NotificationItem notification, bool isRead) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: isRead ? const Color(0xFFF9FAFB) : Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: isRead ? 3 : 6,
            offset: Offset(0, isRead ? 1 : 4),
          ),
          if (!isRead)
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 4,
              offset: const Offset(0, 2),
            ),
        ],
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Icon
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: notification.iconBgColor.withOpacity(isRead ? 0.6 : 1),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Center(
              child: IconTheme(
                data: IconThemeData(color: notification.iconColor, size: 24),
                child: notification.icon,
              ),
            ),
          ),
          const SizedBox(width: 12),
          // Content
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Title and delete button
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      notification.title,
                      style: TextStyle(
                        fontSize: 16,
                        color: isRead
                            ? const Color(0xFF364153)
                            : const Color(0xFF101828),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    InkWell(
                      onTap: () =>
                          _showNotificationOptions(notification, isRead),
                      borderRadius: BorderRadius.circular(4),
                      child: const Padding(
                        padding: EdgeInsets.all(4),
                        child: Icon(
                          Icons.more_horiz,
                          size: 16,
                          color: Color(0xFF99A1AF),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                // Message
                Text(
                  notification.message,
                  style: TextStyle(
                    fontSize: 14,
                    color: isRead
                        ? const Color(0xFF6A7282)
                        : const Color(0xFF4A5565),
                    fontFamily: 'Arimo',
                    height: 1.4,
                  ),
                ),
                const SizedBox(height: 4),
                // Time
                Text(
                  notification.time,
                  style: const TextStyle(
                    fontSize: 12,
                    color: Color(0xFF99A1AF),
                    fontFamily: 'Arimo',
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
