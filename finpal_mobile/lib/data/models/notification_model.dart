/// Notification Model - Model cho thông báo từ API
///
/// Mapping từ API response:
/// - id: ID thông báo
/// - userId: ID user
/// - type: Loại thông báo (BUDGET_ALERT, SAVINGS_SUGGESTION, etc.)
/// - title: Tiêu đề
/// - content: Nội dung
/// - actionUrl: URL để navigate khi tap
/// - isRead: Đã đọc chưa
/// - priority: Độ ưu tiên (LOW, MEDIUM, HIGH, URGENT)
/// - createdAt: Thời gian tạo

class NotificationModel {
  final int id;
  final int userId;
  final String type;
  final String title;
  final String content;
  final String? actionUrl;
  final bool isRead;
  final String? readAt;
  final String priority;
  final String createdAt;

  NotificationModel({
    required this.id,
    required this.userId,
    required this.type,
    required this.title,
    required this.content,
    this.actionUrl,
    required this.isRead,
    this.readAt,
    required this.priority,
    required this.createdAt,
  });

  factory NotificationModel.fromJson(Map<String, dynamic> json) {
    return NotificationModel(
      id: json['id'] ?? 0,
      userId: json['userId'] ?? 0,
      type: json['type'] ?? 'SYSTEM',
      title: json['title'] ?? '',
      content: json['content'] ?? '',
      actionUrl: json['actionUrl'],
      isRead: json['isRead'] ?? false,
      readAt: json['readAt'],
      priority: json['priority'] ?? 'MEDIUM',
      createdAt: json['createdAt'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'userId': userId,
      'type': type,
      'title': title,
      'content': content,
      'actionUrl': actionUrl,
      'isRead': isRead,
      'readAt': readAt,
      'priority': priority,
      'createdAt': createdAt,
    };
  }

  /// Lấy thời gian hiển thị đẹp (VD: "5 phút trước", "1 giờ trước")
  String get displayTime {
    try {
      // Parse thời gian từ API
      // API trả về UTC time nhưng không có 'Z', ví dụ: "2025-12-18T16:00:00"
      // Cần thêm 'Z' để parse đúng là UTC
      String dateString = createdAt;
      if (!dateString.endsWith('Z') &&
          !dateString.contains('+') &&
          !dateString.contains('-', 10)) {
        dateString = '${dateString}Z'; // Thêm Z để đánh dấu UTC
      }

      final dateTime = DateTime.parse(dateString).toLocal();
      final now = DateTime.now();
      final difference = now.difference(dateTime);

      if (difference.inMinutes < 1) {
        return 'Vừa xong';
      } else if (difference.inMinutes < 60) {
        return '${difference.inMinutes} phút trước';
      } else if (difference.inHours < 24) {
        return '${difference.inHours} giờ trước';
      } else if (difference.inDays < 7) {
        return '${difference.inDays} ngày trước';
      } else {
        return '${dateTime.day}/${dateTime.month}/${dateTime.year}';
      }
    } catch (e) {
      return createdAt;
    }
  }

  /// Lấy loại icon dựa trên type
  String get iconType {
    switch (type) {
      case 'BUDGET_ALERT':
        return 'warning';
      case 'SAVINGS_SUGGESTION':
        return 'lightbulb';
      case 'SPENDING_INSIGHT':
        return 'analytics';
      case 'GOAL_REMINDER':
        return 'target';
      case 'ANOMALY_ALERT':
        return 'alert';
      case 'TRANSACTION':
        return 'transaction';
      case 'SYSTEM':
      default:
        return 'info';
    }
  }

  /// Copy với sửa đổi
  NotificationModel copyWith({
    int? id,
    int? userId,
    String? type,
    String? title,
    String? content,
    String? actionUrl,
    bool? isRead,
    String? readAt,
    String? priority,
    String? createdAt,
  }) {
    return NotificationModel(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      type: type ?? this.type,
      title: title ?? this.title,
      content: content ?? this.content,
      actionUrl: actionUrl ?? this.actionUrl,
      isRead: isRead ?? this.isRead,
      readAt: readAt ?? this.readAt,
      priority: priority ?? this.priority,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
