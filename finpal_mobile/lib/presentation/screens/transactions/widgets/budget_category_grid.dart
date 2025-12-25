import 'package:flutter/material.dart';
import '../../../../core/constants/app_colors.dart';
import '../../../../core/utils/category_icon_helper.dart';
import '../../../../data/models/category.dart';
import '../../../../data/services/transaction_service.dart';

/// Widget hiển thị grid các danh mục ngân sách để chọn
class BudgetCategoryGrid extends StatefulWidget {
  final Function(Category) onCategorySelected;

  /// Có hiển thị container bọc ngoài hay không
  /// Đặt false khi sử dụng bên trong card khác
  final bool showContainer;

  /// Có hiển thị tiêu đề hay không
  final bool showTitle;

  const BudgetCategoryGrid({
    super.key,
    required this.onCategorySelected,
    this.showContainer = true,
    this.showTitle = true,
  });

  @override
  State<BudgetCategoryGrid> createState() => _BudgetCategoryGridState();
}

class _BudgetCategoryGridState extends State<BudgetCategoryGrid> {
  final _transactionService = TransactionService();

  List<Category> _categories = [];
  bool _isLoading = true;

  // Cache static để lưu categories, chia sẻ giữa tất cả instances
  static List<Category>? _cachedCategories;

  @override
  void initState() {
    super.initState();
    _loadCategories();
  }

  /// Tải danh mục chi tiêu từ API (sử dụng cache nếu đã có)
  Future<void> _loadCategories() async {
    // Kiểm tra cache trước - nếu đã có thì dùng luôn
    if (_cachedCategories != null && _cachedCategories!.isNotEmpty) {
      setState(() {
        _categories = _cachedCategories!;
        _isLoading = false;
      });
      return; // Không cần gọi API
    }

    setState(() {
      _isLoading = true;
    });

    try {
      // Ngân sách chỉ áp dụng cho chi tiêu (EXPENSE)
      final categories = await _transactionService.getCategories(
        type: 'EXPENSE',
      );

      // Lưu vào cache static
      _cachedCategories = categories;

      setState(() {
        _categories = categories;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Không thể tải danh mục: ${e.toString()}'),
            backgroundColor: Colors.orange,
          ),
        );
      }
    }
  }

  /// Xóa cache (gọi khi cần refresh dữ liệu)
  static void clearCache() {
    _cachedCategories = null;
  }

  @override
  Widget build(BuildContext context) {
    final content = Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (widget.showTitle) ...[
          const Text(
            'Chọn danh mục ngân sách',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 20),
        ],

        // Grid danh mục
        _isLoading
            ? const Center(
                child: Padding(
                  padding: EdgeInsets.all(40),
                  child: CircularProgressIndicator(color: Color(0xFFD7006E)),
                ),
              )
            : _categories.isEmpty
            ? const Center(
                child: Padding(
                  padding: EdgeInsets.all(40),
                  child: Text(
                    'Không có danh mục nào',
                    style: TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 14,
                    ),
                  ),
                ),
              )
            : GridView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3, // 3 cột để hiển thị nhiều hơn
                  crossAxisSpacing: 10,
                  mainAxisSpacing: 10,
                  childAspectRatio: 0.9, // Thu nhỏ chiều cao
                ),
                itemCount: _categories.length,
                itemBuilder: (context, index) {
                  final category = _categories[index];
                  return _BudgetCategoryCard(
                    category: category,
                    onTap: () => widget.onCategorySelected(category),
                  );
                },
              ),
      ],
    );

    // Nếu không hiển thị container, chỉ trả về nội dung
    if (!widget.showContainer) {
      return Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        child: content,
      );
    }

    // Hiển thị với container
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: content,
    );
  }
}

/// Widget card hiển thị một danh mục ngân sách
class _BudgetCategoryCard extends StatelessWidget {
  final Category category;
  final VoidCallback onTap;

  const _BudgetCategoryCard({required this.category, required this.onTap});

  @override
  Widget build(BuildContext context) {
    // Lấy emoji và màu từ API thông qua CategoryIconHelper
    final emoji = CategoryIconHelper.getEmoji(category.icon);
    final iconColor = CategoryIconHelper.getColor(
      category.icon,
      category.name,
      colorFromApi: category.color,
    );
    final bgColor = CategoryIconHelper.getBackgroundColor(iconColor);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: bgColor,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: iconColor.withValues(alpha: 0.2), width: 1),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(10),
                boxShadow: [
                  BoxShadow(
                    color: iconColor.withValues(alpha: 0.2),
                    blurRadius: 4,
                    offset: const Offset(0, 2),
                  ),
                ],
              ),
              child: Center(
                child: Text(emoji, style: const TextStyle(fontSize: 22)),
              ),
            ),
            const SizedBox(height: 6),
            Text(
              category.name,
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w500,
                color: iconColor.withValues(alpha: 0.9),
              ),
              textAlign: TextAlign.center,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}
