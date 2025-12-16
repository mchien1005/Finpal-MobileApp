import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../core/constants/app_colors.dart';
import '../../data/models/notification_model.dart';
import '../../data/services/notification_service.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

class NotificationScreen extends StatefulWidget {
  const NotificationScreen({super.key});

  @override
  State<NotificationScreen> createState() => _NotificationScreenState();
}

class _NotificationScreenState extends State<NotificationScreen> {
  final NotificationService _notificationService = NotificationService();

  List<NotificationModel> _unreadNotifications = [];
  List<NotificationModel> _readNotifications = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadNotifications();
  }

  Future<void> _loadNotifications() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      // Load tất cả notifications
      final allNotifications = await _notificationService.getNotifications();

      setState(() {
        _unreadNotifications = allNotifications
            .where((n) => !n.isRead)
            .toList();
        _readNotifications = allNotifications.where((n) => n.isRead).toList();
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _markAsRead(NotificationModel notification) async {
    try {
      await _notificationService.markAsRead(notification.id);

      setState(() {
        _unreadNotifications.removeWhere((n) => n.id == notification.id);
        _readNotifications.insert(0, notification.copyWith(isRead: true));
      });
    } catch (e) {
      _showSnackBar('Không thể đánh dấu đã đọc');
    }
  }

  Future<void> _markAllAsRead() async {
    try {
      await _notificationService.markAllAsRead();

      setState(() {
        for (var n in _unreadNotifications) {
          _readNotifications.insert(0, n.copyWith(isRead: true));
        }
        _unreadNotifications.clear();
      });

      _showSnackBar('Đã đánh dấu tất cả là đã đọc');
    } catch (e) {
      _showSnackBar('Không thể đánh dấu tất cả đã đọc');
    }
  }

  Future<void> _deleteNotification(NotificationModel notification) async {
    try {
      await _notificationService.deleteNotification(notification.id);

      setState(() {
        _unreadNotifications.removeWhere((n) => n.id == notification.id);
        _readNotifications.removeWhere((n) => n.id == notification.id);
      });

      _showSnackBar('Đã xóa thông báo');
    } catch (e) {
      _showSnackBar('Không thể xóa thông báo');
    }
  }

  Future<void> _deleteAllNotifications() async {
    // Show confirmation dialog
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Xác nhận'),
        content: const Text('Bạn có chắc muốn xóa tất cả thông báo đã đọc?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Hủy'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Xóa', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );

    if (confirm == true) {
      try {
        await _notificationService.deleteAllRead();

        setState(() {
          _readNotifications.clear();
        });

        _showSnackBar('Đã xóa tất cả thông báo đã đọc');
      } catch (e) {
        _showSnackBar('Không thể xóa thông báo');
      }
    }
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), duration: const Duration(seconds: 2)),
    );
  }

  void _showNotificationOptions(NotificationModel notification, bool isRead) {
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
            if (!isRead)
              InkWell(
                onTap: () {
                  Navigator.pop(context);
                  _markAsRead(notification);
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
                      const Text(
                        'Đánh dấu đã đọc',
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

  /// Lấy icon và màu dựa trên loại notification
  Map<String, dynamic> _getNotificationStyle(NotificationModel notification) {
    switch (notification.type) {
      case 'BUDGET_ALERT':
        return {
          'icon': const Icon(Icons.warning_amber_outlined, size: 24),
          'iconColor': const Color(0xFFF54900),
          'iconBgColor': const Color(0xFFFFEDD5),
        };
      case 'SAVINGS_SUGGESTION':
        return {
          'icon': const Icon(Icons.lightbulb_outline, size: 24),
          'iconColor': const Color(0xFF4F39F6),
          'iconBgColor': const Color(0xFFE0E7FF),
        };
      case 'SPENDING_INSIGHT':
        return {
          'icon': const Icon(Icons.analytics_outlined, size: 24),
          'iconColor': const Color(0xFF0EA5E9),
          'iconBgColor': const Color(0xFFE0F2FE),
        };
      case 'GOAL_REMINDER':
        return {
          'icon': Icon(MdiIcons.bullseye, size: 24),
          'iconColor': const Color(0xFF00A63E),
          'iconBgColor': const Color(0xFFDCFCE7),
        };
      case 'ANOMALY_ALERT':
        return {
          'icon': const Icon(Icons.notification_important_outlined, size: 24),
          'iconColor': const Color(0xFFEF4444),
          'iconBgColor': const Color(0xFFFEE2E2),
        };
      case 'TRANSACTION':
        return {
          'icon': SvgPicture.asset(
            'assets/icons/arrow_down.svg',
            width: 24,
            height: 24,
          ),
          'iconColor': const Color(0xFFEF4444),
          'iconBgColor': const Color(0xFFFEE2E2),
        };
      case 'SYSTEM':
      default:
        return {
          'icon': const Icon(Icons.info_outline, size: 24),
          'iconColor': const Color(0xFF6B7280),
          'iconBgColor': const Color(0xFFF3F4F6),
        };
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
                            onTap: _unreadNotifications.isNotEmpty
                                ? _markAllAsRead
                                : null,
                            borderRadius: BorderRadius.circular(8),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 4,
                                vertical: 4,
                              ),
                              child: Row(
                                children: [
                                  Icon(
                                    Icons.done_all,
                                    size: 16,
                                    color: _unreadNotifications.isNotEmpty
                                        ? Colors.white
                                        : Colors.white54,
                                  ),
                                  const SizedBox(width: 4),
                                  Text(
                                    'Đọc tất cả',
                                    style: TextStyle(
                                      color: _unreadNotifications.isNotEmpty
                                          ? Colors.white
                                          : Colors.white54,
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
                            onTap: _readNotifications.isNotEmpty
                                ? _deleteAllNotifications
                                : null,
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
                                    color: _readNotifications.isNotEmpty
                                        ? Colors.white
                                        : Colors.white54,
                                  ),
                                  const SizedBox(width: 4),
                                  Text(
                                    'Xóa đã đọc',
                                    style: TextStyle(
                                      color: _readNotifications.isNotEmpty
                                          ? Colors.white
                                          : Colors.white54,
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

              // Content
              Expanded(
                child: _isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : _error != null
                    ? _buildErrorWidget()
                    : _buildNotificationList(),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildErrorWidget() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.error_outline, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              'Không thể tải thông báo',
              style: TextStyle(
                fontSize: 16,
                color: Colors.grey[600],
                fontFamily: 'Arimo',
              ),
            ),
            const SizedBox(height: 8),
            Text(
              _error ?? '',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[500],
                fontFamily: 'Arimo',
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: _loadNotifications,
              icon: const Icon(Icons.refresh),
              label: const Text('Thử lại'),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primary,
                foregroundColor: Colors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNotificationList() {
    if (_unreadNotifications.isEmpty && _readNotifications.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.notifications_none, size: 80, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              'Không có thông báo',
              style: TextStyle(
                fontSize: 16,
                color: Colors.grey[600],
                fontFamily: 'Arimo',
              ),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadNotifications,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Unread section
          if (_unreadNotifications.isNotEmpty) ...[
            Padding(
              padding: const EdgeInsets.only(left: 8, bottom: 10),
              child: Row(
                children: [
                  const Text(
                    'Chưa đọc',
                    style: TextStyle(
                      fontSize: 14,
                      color: Color(0xFF364153),
                      fontFamily: 'Arimo',
                    ),
                  ),
                  const SizedBox(width: 8),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 2,
                    ),
                    decoration: BoxDecoration(
                      color: AppColors.primary,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Text(
                      '${_unreadNotifications.length}',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.white,
                        fontFamily: 'Arimo',
                      ),
                    ),
                  ),
                ],
              ),
            ),
            ..._unreadNotifications.map(
              (notification) => _buildNotificationCard(notification, false),
            ),
            const SizedBox(height: 20),
          ],

          // Read section
          if (_readNotifications.isNotEmpty) ...[
            Container(
              height: 1,
              margin: const EdgeInsets.only(bottom: 9),
              decoration: BoxDecoration(color: Colors.black.withOpacity(0.1)),
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
              (notification) => _buildNotificationCard(notification, true),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildNotificationCard(NotificationModel notification, bool isRead) {
    final style = _getNotificationStyle(notification);

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
              color: (style['iconBgColor'] as Color).withOpacity(
                isRead ? 0.6 : 1,
              ),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Center(
              child: IconTheme(
                data: IconThemeData(
                  color: style['iconColor'] as Color,
                  size: 24,
                ),
                child: style['icon'] as Widget,
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
                    Expanded(
                      child: Text(
                        notification.title,
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: isRead
                              ? FontWeight.normal
                              : FontWeight.w500,
                          color: isRead
                              ? const Color(0xFF364153)
                              : const Color(0xFF101828),
                          fontFamily: 'Arimo',
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
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
                  notification.content,
                  style: TextStyle(
                    fontSize: 14,
                    color: isRead
                        ? const Color(0xFF6A7282)
                        : const Color(0xFF4A5565),
                    fontFamily: 'Arimo',
                    height: 1.4,
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 4),
                // Time
                Text(
                  notification.displayTime,
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
