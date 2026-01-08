class Category {
  final int id;
  final String name;
  final String type; // INCOME or EXPENSE
  final int? parentId;
  final bool isSystem;
  final int displayOrder;
  final String? icon;
  final String? color;
  final DateTime? createdAt;
  final DateTime? updatedAt;
  final Category? parent;
  final List<Category>? subCategories;

  Category({
    required this.id,
    required this.name,
    required this.type,
    this.parentId,
    required this.isSystem,
    required this.displayOrder,
    this.icon,
    this.color,
    this.createdAt,
    this.updatedAt,
    this.parent,
    this.subCategories,
  });

  factory Category.fromJson(Map<String, dynamic> json) {
    return Category(
      id: json['id'] as int,
      name: json['name'] as String,
      type: json['type'] as String,
      parentId: json['parentId'] as int?,
      isSystem: json['isSystem'] as bool? ?? true,
      displayOrder: json['displayOrder'] as int? ?? 0,
      icon: json['icon'] as String?,
      color: json['color'] as String?,
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'] as String)
          : null,
      parent: json['parent'] != null
          ? Category.fromJson(json['parent'] as Map<String, dynamic>)
          : null,
      subCategories: json['subCategories'] != null
          ? (json['subCategories'] as List)
                .map((e) => Category.fromJson(e as Map<String, dynamic>))
                .toList()
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'type': type,
      'parentId': parentId,
      'isSystem': isSystem,
      'displayOrder': displayOrder,
      'icon': icon,
      'color': color,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
      'parent': parent?.toJson(),
      'subCategories': subCategories?.map((e) => e.toJson()).toList(),
    };
  }
}
