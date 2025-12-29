import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../data/services/transaction_service.dart';
import '../../../data/services/category_cache_service.dart';
import '../../../data/models/transaction.dart';
import '../../../data/models/category.dart';
import '../../../core/utils/category_icon_helper.dart';
import 'edit_transaction_dialog.dart';
import '../../../core/widgets/success_notification_dialog.dart';
import '../../../core/widgets/confirmation_dialog.dart';
import 'dart:async';
import 'package:intl/intl.dart';
import '../../../core/services/transaction_event_bus.dart';

class SearchTransactionScreen extends StatefulWidget {
  const SearchTransactionScreen({super.key});

  @override
  State<SearchTransactionScreen> createState() =>
      _SearchTransactionScreenState();
}

class _SearchTransactionScreenState extends State<SearchTransactionScreen> {
  final TextEditingController _searchController = TextEditingController();
  final TransactionService _service = TransactionService();
  final CategoryCacheService _categoryCache = CategoryCacheService.instance;
  late final StreamSubscription<void> _eventSub;
  bool _isLoading = false;
  List<Transaction> _results = [];
  String? _error;
  int _page = 0;
  int _size = 10;
  int _totalPages = 0;
  String _lastKeyword = '';

  @override
  void dispose() {
    _eventSub.cancel();
    _searchController.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    // Preload categories so we can map categoryId -> category quickly
    _categoryCache.preloadAllCategories().catchError((_) {});
    _eventSub = TransactionEventBus.instance.onUpdated.listen((_) {
      if (mounted) _search(resetPage: true);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: AppColors.primary,
        toolbarHeight: 72,
        title: Container(
          height: 48,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(14),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.08),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Row(
            children: [
              const SizedBox(width: 8),
              Icon(Icons.search, color: AppColors.primary),
              const SizedBox(width: 8),
              Expanded(
                child: TextField(
                  controller: _searchController,
                  autofocus: true,
                  style: const TextStyle(color: Colors.black87),
                  cursorColor: AppColors.primary,
                  keyboardType: TextInputType.text,
                  onChanged: (_) => setState(() {}),
                  onSubmitted: (_) => _search(resetPage: true),
                  decoration: InputDecoration(
                    hintText: 'Tìm giao dịch theo mô tả, ghi chú hoặc danh mục...',
                    hintStyle: TextStyle(color: Colors.grey[600]),
                    border: InputBorder.none,
                    isDense: true,
                    contentPadding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
              ),
              if (_searchController.text.isNotEmpty)
                IconButton(
                  icon: const Icon(Icons.close, size: 18),
                  color: Colors.grey[600],
                  onPressed: () {
                    _searchController.clear();
                    setState(() {
                      _results = [];
                      _error = null;
                    });
                  },
                ),
              IconButton(
                icon: Icon(Icons.arrow_forward, color: AppColors.primary),
                onPressed: () => _search(resetPage: true),
              ),
            ],
          ),
        ),
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const SizedBox(height: 12),
            if (_isLoading) const LinearProgressIndicator(),
            if (_error != null)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 12),
                child: Text(_error!, style: TextStyle(color: AppColors.textSecondary)),
              ),
            Expanded(
              child: _results.isEmpty && !_isLoading
                  ? Center(
                      child: Text(
                        'Nhập từ khóa và nhấn tìm',
                        style: TextStyle(color: AppColors.textSecondary),
                      ),
                    )
                  : ListView.separated(
                      itemCount: _results.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 8),
                      itemBuilder: (context, index) => _buildResultCard(_results[index]),
                    ),
            ),
            if (_totalPages > 1)
              Padding(
                padding: const EdgeInsets.only(top: 8.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Trang ${_page + 1} / ${_totalPages}'),
                    Row(
                      children: [
                        TextButton(
                          onPressed: _page > 0 && !_isLoading
                              ? () => _search(resetPage: false, toPage: _page - 1)
                              : null,
                          child: const Text('Trước'),
                        ),
                        const SizedBox(width: 8),
                        TextButton(
                          onPressed: (_page + 1) < _totalPages && !_isLoading
                              ? () => _search(resetPage: false, toPage: _page + 1)
                              : null,
                          child: const Text('Sau'),
                        ),
                      ],
                    )
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }

  Future<void> _search({bool resetPage = true, int? toPage}) async {
    final text = _searchController.text.trim();
    if (text.isEmpty) return;

    if (resetPage) {
      _page = 0;
    }

    if (toPage != null) {
      _page = toPage;
    }

    setState(() {
      _isLoading = true;
      _error = null;
      if (resetPage) _results = [];
    });

    try {
      final resp = await _service.searchTransactions(text, page: _page, size: _size);

      List<dynamic> content;
      if (resp.containsKey('content') && resp['content'] is List) {
        content = resp['content'] as List<dynamic>;
      } else if (resp is List) {
        content = resp as List<dynamic>;
      } else {
        content = [];
      }

      final parsed = content.map((e) => Transaction.fromJson(e as Map<String, dynamic>)).toList();

      // Map categoryId -> category for results when category is missing
      final List<Transaction> mapped = [];
      for (final tx in parsed) {
        if (tx.category == null && tx.categoryId != null) {
          try {
            final categories = await _categoryCache.getCategories(tx.type);
            Category? match;
            for (final c in categories) {
              if (c.id == tx.categoryId) {
                match = c;
                break;
              }
            }
            if (match != null) {
              mapped.add(Transaction(
                id: tx.id,
                type: tx.type,
                amount: tx.amount,
                transactionSource: tx.transactionSource,
                categoryId: tx.categoryId,
                description: tx.description,
                merchant: tx.merchant,
                transactionDate: tx.transactionDate,
                isAuto: tx.isAuto,
                category: TransactionCategory(
                  id: match.id,
                  name: match.name,
                  icon: match.icon,
                ),
              ));
              continue;
            }
          } catch (_) {
            // ignore and fallthrough to original tx
          }
        }
        mapped.add(tx);
      }

      setState(() {
        _results = mapped;
        _lastKeyword = text;
        // try to read pagination info
        if (resp.containsKey('totalPages')) {
          _totalPages = (resp['totalPages'] as int);
        } else if (resp.containsKey('totalElements') && resp.containsKey('size')) {
          final total = (resp['totalElements'] as int);
          final size = (resp['size'] as int);
          _totalPages = (total / size).ceil();
        } else {
          _totalPages = parsed.isEmpty ? 0 : 1;
        }
      });
    } catch (e) {
      setState(() {
        _error = 'Lỗi khi tìm: ${e.toString()}';
        _results = [];
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Widget _buildResultCard(Transaction tx) {
    final iconWidget = CategoryIconHelper.emojiWidgetWithFallback(
      tx.category?.icon,
      tx.category?.name,
      size: 24,
    );
    final amount = tx.isIncome
        ? '+${NumberFormat('#,###', 'vi_VN').format(tx.amount)}đ'
        : '-${NumberFormat('#,###', 'vi_VN').format(tx.amount)}đ';
    final date = DateFormat('dd/MM/yyyy HH:mm').format(tx.transactionDate);

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFFF9FAFB),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  color: const Color(0xFFF3F4F6),
                  shape: BoxShape.circle,
                ),
                child: Center(child: iconWidget),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Flexible(
                          child: Text(
                            tx.merchant ?? tx.description ?? 'Giao dịch',
                            style: const TextStyle(
                              fontSize: 14,
                              color: AppColors.textPrimary,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Flexible(
                          child: Text(
                            '${tx.category?.name ?? 'Không phân loại'} • ${tx.transactionSource}',
                            style: const TextStyle(
                              fontSize: 12,
                              color: AppColors.textSecondary,
                            ),
                            overflow: TextOverflow.ellipsis,
                            maxLines: 1,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    amount,
                    style: TextStyle(
                      fontSize: 14,
                      color: tx.isIncome ? const Color(0xFF00A63E) : const Color(0xFFE7000B),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    date,
                    style: const TextStyle(fontSize: 12, color: AppColors.textSecondary),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () => _handleEdit(tx),
                  icon: const Icon(Icons.edit, size: 16),
                  label: const Text('Sửa'),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.textPrimary,
                    side: const BorderSide(color: Color(0xFFFFD230), width: 1.12),
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () => ConfirmationDialog.show(
                    context,
                    title: 'Xóa giao dịch',
                    message: 'Bạn có chắc chắn muốn xóa không? \nHành động này không thể hoàn tác.',
                    confirmText: 'Xóa',
                    cancelText: 'Hủy',
                    confirmColor: const Color(0xFFD7006E),
                    onConfirm: () async {
                      try {
                        await _service.deleteTransaction(tx.id);
                        if (mounted) {
                          SuccessNotificationDialog.show(context, message: 'Đã xóa giao dịch thành công');
                          _search(resetPage: true);
                          TransactionEventBus.instance.notifyUpdated(tx.id);
                        }
                      } catch (e) {
                        if (mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Lỗi: ${e.toString()}'), backgroundColor: Colors.red));
                        }
                      }
                    },
                  ),
                  icon: const Icon(Icons.delete, size: 16),
                  label: const Text('Xóa'),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.textPrimary,
                    backgroundColor: const Color(0xFFFFFAFA),
                    side: const BorderSide(color: Color(0xFFE7000B), width: 1.12),
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Future<void> _handleEdit(Transaction tx) async {
    final parentContext = context;

    await showDialog(
      context: parentContext,
      barrierDismissible: false,
      builder: (dialogContext) => EditTransactionDialog(
        title: tx.description ?? tx.merchant ?? 'Sửa giao dịch',
        amount: tx.amount,
        category: tx.category?.name ?? '',
        categoryId: tx.categoryId,
        account: tx.transactionSource,
        date: tx.transactionDate,
        isIncome: tx.isIncome,
        onSave: ({required double amount, required String source, required String category, required int? categoryId, required String description, required DateTime date}) async {
          Navigator.of(dialogContext).pop();

          showDialog(
            context: parentContext,
            barrierDismissible: false,
            builder: (_) => const Center(child: CircularProgressIndicator()),
          );

          try {
            await _service.updateTransaction(
              id: tx.id,
              type: tx.type,
              amount: amount,
              transactionSource: source,
              categoryId: categoryId,
              description: description,
              transactionDate: date,
            );

            await _search(resetPage: true);

            if (!mounted) return;

            Navigator.of(parentContext).pop(); // close loading

            SuccessNotificationDialog.show(
              parentContext,
              message: 'Đã cập nhật giao dịch thành công',
            );

            TransactionEventBus.instance.notifyUpdated(tx.id);
          } catch (e) {
            if (!mounted) return;
            Navigator.of(parentContext).pop();
            ScaffoldMessenger.of(parentContext).showSnackBar(
              SnackBar(content: Text('Lỗi khi cập nhật: ${e.toString()}')),
            );
          }
        },
        onCancel: () => Navigator.of(dialogContext).pop(),
      ),
    );
  }

  Future<void> _handleDelete(Transaction tx) async {
    final confirmed = await showDialog<bool>(
          context: context,
          builder: (c) => AlertDialog(
            title: const Text('Xác nhận'),
            content: const Text('Bạn có chắc muốn xóa giao dịch này?'),
            actions: [
              TextButton(onPressed: () => Navigator.of(c).pop(false), child: const Text('Hủy')),
              TextButton(onPressed: () => Navigator.of(c).pop(true), child: const Text('Xóa')),
            ],
          ),
        ) ??
        false;

    if (!confirmed) return;

    try {
      await _service.deleteTransaction(tx.id);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Đã xóa giao dịch')));
          await _search(resetPage: true);
          TransactionEventBus.instance.notifyUpdated(tx.id);
        }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Lỗi khi xóa: ${e.toString()}')));
      }
    }
  }

}
