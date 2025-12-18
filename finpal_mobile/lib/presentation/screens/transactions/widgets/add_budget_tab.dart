import 'package:flutter/material.dart';
import '../../../../data/models/category.dart';
import 'budget_category_grid.dart';
import 'budget_form.dart';

/// Widget tab thêm ngân sách
/// BudgetCategoryGrid tự load danh mục riêng, không phụ thuộc vào tab giao dịch
class AddBudgetTab extends StatefulWidget {
  const AddBudgetTab({super.key});

  @override
  State<AddBudgetTab> createState() => _AddBudgetTabState();
}

class _AddBudgetTabState extends State<AddBudgetTab> {
  Category? _selectedCategory;
  bool _showForm = false;

  void _onCategorySelected(Category category) {
    setState(() {
      _selectedCategory = category;
      _showForm = true;
    });
  }

  void _onBack() {
    setState(() {
      _selectedCategory = null;
      _showForm = false;
    });
  }

  void _onSuccess() {
    setState(() {
      _selectedCategory = null;
      _showForm = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        children: [
          const SizedBox(height: 8),
          _showForm && _selectedCategory != null
              ? BudgetForm(
                  selectedCategory: _selectedCategory!,
                  onBack: _onBack,
                  onSuccess: _onSuccess,
                )
              : BudgetCategoryGrid(onCategorySelected: _onCategorySelected),
          const SizedBox(height: 100),
        ],
      ),
    );
  }
}
