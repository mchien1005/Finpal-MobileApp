import 'package:flutter/material.dart';
import 'budget_list_card.dart';

/// Widget tab để quản lý ngân sách
/// Hiển thị danh sách ngân sách kèm nút thêm mới, sửa và xóa
class AddBudgetTab extends StatelessWidget {
  const AddBudgetTab({super.key});

  @override
  Widget build(BuildContext context) {
    return const SingleChildScrollView(
      child: Column(
        children: [
          SizedBox(height: 8),
          // Card hiển thị danh sách ngân sách với chức năng thêm, sửa, xóa
          BudgetListCard(),
          SizedBox(height: 100),
        ],
      ),
    );
  }
}
