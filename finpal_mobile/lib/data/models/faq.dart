class Faq {
  final int id;
  final String question;
  final String answer;
  final String category;
  final int displayOrder;
  final bool isActive;
  final int helpfulCount;
  final int notHelpfulCount;
  final DateTime createdAt;
  final DateTime updatedAt;

  Faq({
    required this.id,
    required this.question,
    required this.answer,
    required this.category,
    required this.displayOrder,
    required this.isActive,
    required this.helpfulCount,
    required this.notHelpfulCount,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Faq.fromJson(Map<String, dynamic> json) {
    return Faq(
      id: _parseInt(json['id']),
      question: json['question']?.toString() ?? '',
      answer: json['answer']?.toString() ?? '',
      category: json['category']?.toString() ?? 'GENERAL',
      displayOrder: _parseInt(json['displayOrder']),
      isActive:
          json['isActive'] == true ||
          json['isActive'] == 1 ||
          json['isActive'] == '1',
      helpfulCount: _parseInt(json['helpfulCount']),
      notHelpfulCount: _parseInt(json['notHelpfulCount']),
      createdAt: _parseDateTime(json['createdAt']),
      updatedAt: _parseDateTime(json['updatedAt']),
    );
  }

  static int _parseInt(dynamic value) {
    if (value == null) return 0;
    if (value is int) return value;
    if (value is String) return int.tryParse(value) ?? 0;
    return 0;
  }

  static DateTime _parseDateTime(dynamic value) {
    if (value == null) return DateTime.now();
    if (value is DateTime) return value;
    if (value is String) {
      try {
        return DateTime.parse(value);
      } catch (e) {
        return DateTime.now();
      }
    }
    return DateTime.now();
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'question': question,
      'answer': answer,
      'category': category,
      'displayOrder': displayOrder,
      'isActive': isActive,
      'helpfulCount': helpfulCount,
      'notHelpfulCount': notHelpfulCount,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
    };
  }
}
