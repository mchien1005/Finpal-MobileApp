import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../data/services/transaction_service.dart';
import '../../../data/services/category_cache_service.dart';
import '../../../data/models/transaction.dart';
import '../../../data/models/category.dart';
import '../../../core/utils/category_icon_helper.dart';
import 'package:intl/intl.dart';

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
  bool _isLoading = false;
  List<Transaction> _results = [];
  String? _error;
  int _page = 0;
  int _size = 10;
  int _totalPages = 0;
  String _lastKeyword = '';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    // Preload categories so we can map categoryId -> category quickly
    _categoryCache.preloadAllCategories().catchError((_) {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: AppColors.primary,
        title: TextField(
          controller: _searchController,
          autofocus: true,
          style: const TextStyle(color: Colors.black87),
          cursorColor: AppColors.primary,
          keyboardType: TextInputType.text,
          onSubmitted: (_) => _search(resetPage: true),
          decoration: InputDecoration(
            hintText: 'Tìm giao dịch theo mô tả, ghi chú hoặc danh mục...',
            hintStyle: TextStyle(color: Colors.grey[600]),
            filled: true,
            fillColor: Colors.white,
            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide.none,
            ),
            suffixIcon: IconButton(
              icon: Icon(Icons.search, color: AppColors.primary),
              onPressed: () => _search(resetPage: true),
            ),
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

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: const Color(0xFFF3F4F6),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Center(child: iconWidget),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(tx.merchant ?? tx.description ?? 'Giao dịch',
                      style: const TextStyle(fontSize: 16)),
                ),
                Text(amount, style: const TextStyle(fontWeight: FontWeight.bold)),
              ],
            ),
            const SizedBox(height: 8),
            Text('Danh mục: ${tx.category?.name ?? 'Không phân loại'}'),
            Text('Nguồn: ${tx.transactionSource}'),
            Text('Ngày: $date'),
          ],
        ),
      ),
    );
  }
}
