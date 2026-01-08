import 'api_service.dart';
import '../models/dashboard_model.dart';

class DashboardService {
  final ApiService _apiService = ApiService();

  static final DashboardService _instance = DashboardService._internal();
  factory DashboardService() => _instance;
  DashboardService._internal();

  /// Lấy tổng quan dashboard (thu nhập, chi tiêu, số dư)
  Future<DashboardSummary> getSummary() async {
    try {
      print('📡 DashboardService: Calling /dashboard/summary...');
      final response = await _apiService.get('/dashboard/summary');
      print('📦 DashboardService: Raw response: $response');
      print('📦 DashboardService: Response type: ${response.runtimeType}');
      
      // Xử lý response có thể được wrap trong object data
      dynamic data = response;
      if (response is Map && response.containsKey('data')) {
        data = response['data'];
        print('📦 DashboardService: Extracted data from wrapper: $data');
      }
      
      // Parse summary từ response
      final summary = DashboardSummary.fromJson(
        data is Map<String, dynamic> ? data : (data as Map).cast<String, dynamic>()
      );
      print('✅ DashboardService: Summary parsed - Income: ${summary.totalIncome}, Expense: ${summary.totalExpense}');
      return summary;
    } catch (e, stackTrace) {
      print('❌ Error getting dashboard summary: $e');
      print('📍 Stack trace: $stackTrace');
      rethrow;
    }
  }

  /// Lấy chi tiêu theo danh mục
  Future<List<CategorySpending>> getSpendingByCategory() async {
    try {
      print('📡 DashboardService: Calling /dashboard/spending-by-category...');
      final response = await _apiService.get('/dashboard/spending-by-category');
      print('📦 DashboardService: Raw response: $response');
      print('📦 DashboardService: Response type: ${response.runtimeType}');
      
      List<CategorySpending> categories = [];
      
      if (response is List) {
        print('📦 DashboardService: Response is List with ${response.length} items');
        categories = response
            .map((item) => CategorySpending.fromJson(item))
            .toList();
      } else if (response is Map) {
        // Xử lý response được wrap trong object
        if (response.containsKey('data')) {
          final data = response['data'];
          print('📦 DashboardService: Found data wrapper: $data');
          if (data is List) {
            categories = data.map((item) => CategorySpending.fromJson(item)).toList();
          }
        } else if (response.containsKey('topExpenseCategories')) {
          final cats = response['topExpenseCategories'] as List;
          print('📦 DashboardService: Found topExpenseCategories with ${cats.length} items');
          categories = cats.map((item) => CategorySpending.fromJson(item)).toList();
        } else if (response.containsKey('categories')) {
          final cats = response['categories'] as List;
          print('📦 DashboardService: Found categories key with ${cats.length} items');
          categories = cats.map((item) => CategorySpending.fromJson(item)).toList();
        }
      }
      
      print('✅ DashboardService: Parsed ${categories.length} categories');
      for (var cat in categories) {
        print('   - ${cat.categoryName}: ${cat.amount} (${cat.percentage}%)');
      }
      
      return categories;
    } catch (e, stackTrace) {
      print('❌ Error getting spending by category: $e');
      print('📍 Stack trace: $stackTrace');
      rethrow;
    }
  }

  /// Lấy ngân sách theo danh mục
  Future<List<CategoryBudget>> getCategoryBudgets() async {
    try {
      print('📡 DashboardService: Calling /dashboard/category-budgets...');
      final response = await _apiService.get('/dashboard/category-budgets');
      print('📦 DashboardService: Category Budgets raw response: $response');
      
      if (response is List) {
        final budgets = response
            .map((item) => CategoryBudget.fromJson(item))
            .toList();
        print('✅ DashboardService: Loaded ${budgets.length} category budgets');
        return budgets;
      }
      
      return [];
    } catch (e, stackTrace) {
      print('❌ Error getting category budgets: $e');
      print('📍 Stack trace: $stackTrace');
      return [];
    }
  }

  /// Lấy xu hướng hàng tháng
  Future<List<MonthlyTrend>> getMonthlyTrend({int months = 6}) async {
    try {
      final response = await _apiService.get(
        '/dashboard/monthly-trend?months=$months',
      );
      
      if (response is List) {
        return response
            .map((item) => MonthlyTrend.fromJson(item))
            .toList();
      } else if (response is Map && response['trends'] != null) {
        final trends = response['trends'] as List;
        return trends
            .map((item) => MonthlyTrend.fromJson(item))
            .toList();
      }
      
      return [];
    } catch (e) {
      print('❌ Error getting monthly trend: $e');
      rethrow;
    }
  }

  /// Lấy lưu lượng tiền tệ
  Future<CashFlow> getCashFlow({String period = 'monthly'}) async {
    try {
      print('📡 DashboardService: Calling /dashboard/cash-flow...');
      final response = await _apiService.get(
        '/dashboard/cash-flow?period=$period',
      );
      print('📦 DashboardService: Cash Flow raw response: $response');
      print('📦 DashboardService: Cash Flow response type: ${response.runtimeType}');
      
      // Xử lý response có thể được wrap
      dynamic data = response;
      if (response is Map && response.containsKey('data')) {
        data = response['data'];
        print('📦 DashboardService: Extracted cash flow data: $data');
      }
      
      final cashFlow = CashFlow.fromJson(
        data is Map<String, dynamic> ? data : (data as Map).cast<String, dynamic>()
      );
      print('✅ DashboardService: Cash Flow parsed');
      print('   - Budget: ${cashFlow.budgetUsed}/${cashFlow.budgetLimit}');
      print('   - Percentage: ${cashFlow.budgetPercentage}%');
      print('   - Income: ${cashFlow.totalIncome}');
      print('   - Expense: ${cashFlow.totalExpense}');
      print('   - Savings: ${cashFlow.savings}');
      
      return cashFlow;
    } catch (e, stackTrace) {
      print('❌ Error getting cash flow: $e');
      print('📍 Stack trace: $stackTrace');
      rethrow;
    }
  }

  /// Lấy danh sách ngân sách
  Future<List<BudgetItem>> getBudgets() async {
    try {
      print('📡 DashboardService: Calling /budgets...');
      final response = await _apiService.get('/budgets');
      print('📦 DashboardService: Budgets response: $response');
      
      List<BudgetItem> budgets = [];
      
      if (response is List) {
        budgets = response.map((item) => BudgetItem.fromJson(item)).toList();
      } else if (response is Map) {
        if (response.containsKey('data')) {
          final data = response['data'];
          if (data is List) {
            budgets = data.map((item) => BudgetItem.fromJson(item)).toList();
          }
        } else if (response.containsKey('budgets')) {
          final items = response['budgets'] as List;
          budgets = items.map((item) => BudgetItem.fromJson(item)).toList();
        }
      }
      
      print('✅ DashboardService: Parsed ${budgets.length} budgets');
      return budgets;
    } catch (e, stackTrace) {
      print('❌ Error getting budgets: $e');
      print('📍 Stack trace: $stackTrace');
      return []; // Trả về list rỗng thay vì throw error
    }
  }

  /// Lấy ngân sách đang hoạt động
  Future<BudgetItem?> getActiveBudget() async {
    try {
      print('📡 DashboardService: Calling /budgets/active...');
      final response = await _apiService.get('/budgets/active');
      print('📦 DashboardService: Active budget response: $response');
      
      if (response == null) {
        print('⚠️ No active budget found');
        return null;
      }

      BudgetItem? budget;
      
      if (response is Map) {
        // Response trực tiếp là budget object
        if (response.containsKey('id')) {
          budget = BudgetItem.fromJson(
            response is Map<String, dynamic> ? response : response.cast<String, dynamic>()
          );
        } 
        // Response có wrapper data
        else if (response.containsKey('data')) {
          final data = response['data'];
          if (data is Map) {
            budget = BudgetItem.fromJson(
              data is Map<String, dynamic> ? data : data.cast<String, dynamic>()
            );
          }
        }
      }
      
      if (budget != null) {
        print('✅ Active budget: ${budget.name} - ${budget.spent}/${budget.amount} (${budget.percentage}%)');
      }
      return budget;
    } catch (e, stackTrace) {
      print('❌ Error getting active budget: $e');
      print('📍 Stack trace: $stackTrace');
      return null;
    }
  }

  /// Lấy tất cả dữ liệu dashboard trong một lần gọi
  Future<Map<String, dynamic>> getAllDashboardData() async {
    try {
      final summary = await getSummary();
      final categories = await getSpendingByCategory();
      
      return {
        'summary': summary,
        'categories': categories,
      };
    } catch (e) {
      print('❌ Error getting all dashboard data: $e');
      rethrow;
    }
  }
}
