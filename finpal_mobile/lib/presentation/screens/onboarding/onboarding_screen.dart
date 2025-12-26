import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/constants/app_colors.dart';
import '../home/dashboard_screen.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final PageController _pageController = PageController();
  int _currentPage = 0;

  // Trạng thái quyền SMS
  bool _smsPermissionGranted = false;
  bool _isRequestingPermission = false;

  @override
  void initState() {
    super.initState();
    _checkSmsPermission();
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  /// Kiểm tra trạng thái quyền SMS hiện tại
  Future<void> _checkSmsPermission() async {
    final status = await Permission.sms.status;
    setState(() {
      _smsPermissionGranted = status.isGranted;
    });
  }

  /// Yêu cầu quyền đọc SMS
  Future<void> _requestSmsPermission() async {
    if (_isRequestingPermission) return;

    setState(() {
      _isRequestingPermission = true;
    });

    try {
      final status = await Permission.sms.request();

      setState(() {
        _smsPermissionGranted = status.isGranted;
        _isRequestingPermission = false;
      });

      if (mounted) {
        if (status.isGranted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('✅ Đã cấp quyền đọc SMS thành công!'),
              backgroundColor: Colors.green,
            ),
          );
        } else if (status.isPermanentlyDenied) {
          // Người dùng đã từ chối vĩnh viễn, cần mở Settings
          _showOpenSettingsDialog();
        }
      }
    } catch (e) {
      setState(() {
        _isRequestingPermission = false;
      });
      debugPrint('❌ Lỗi khi yêu cầu quyền SMS: $e');
    }
  }

  /// Hiển thị dialog mở Settings khi quyền bị từ chối vĩnh viễn
  void _showOpenSettingsDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Cần cấp quyền'),
        content: const Text(
          'Bạn đã từ chối quyền đọc SMS. Để sử dụng tính năng tự động ghi nhận giao dịch, vui lòng cấp quyền trong Cài đặt.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Để sau'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              openAppSettings();
            },
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
            child: const Text(
              'Mở Cài đặt',
              style: TextStyle(color: Colors.white),
            ),
          ),
        ],
      ),
    );
  }

  void _nextPage() {
    if (_currentPage < 3) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      _completeOnboarding();
    }
  }

  void _skipOnboarding() {
    _completeOnboarding();
  }

  Future<void> _completeOnboarding() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('onboarding_completed', true);
    if (mounted) {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (context) => const DashboardScreen()),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      body: SafeArea(
        child: Column(
          children: [
            // Page indicators
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 16),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(
                      4,
                      (index) => Container(
                        margin: const EdgeInsets.symmetric(horizontal: 4),
                        width: 87,
                        height: 4,
                        decoration: BoxDecoration(
                          color: _currentPage == index
                              ? Colors.white
                              : Colors.white.withOpacity(0.3),
                          borderRadius: BorderRadius.circular(100),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Bước ${_currentPage + 1} / 4',
                    style: const TextStyle(
                      fontSize: 14,
                      color: Colors.white,
                      fontWeight: FontWeight.normal,
                    ),
                  ),
                ],
              ),
            ),

            // Content
            Expanded(
              child: PageView(
                controller: _pageController,
                onPageChanged: (index) {
                  setState(() {
                    _currentPage = index;
                  });
                },
                children: [
                  _buildPage1(),
                  _buildPage2(),
                  _buildPage3(),
                  _buildPage4(),
                ],
              ),
            ),

            // Skip button
            Padding(
              padding: const EdgeInsets.only(bottom: 24),
              child: TextButton(
                onPressed: _skipOnboarding,
                child: Text(
                  'Bỏ qua hướng dẫn',
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.white.withOpacity(0.75),
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPage1() {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 5,
            offset: const Offset(10, 10),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Logo
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              color: AppColors.primary,
              borderRadius: BorderRadius.circular(16),
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: Image.asset('assets/images/logo.png', fit: BoxFit.cover),
            ),
          ),
          const SizedBox(height: 24),

          // Title
          const Text(
            'Chào mừng đến với FinPal!',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.normal,
              color: Colors.black,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // Description
          const Text(
            'Trợ lý tài chính thông minh giúp bạn quản lý chi tiêu tự động, tiết kiệm thời gian và đạt được mục tiêu tài chính.',
            style: TextStyle(
              fontSize: 16,
              color: Color(0xFF4A5565),
              height: 1.5,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 32),

          // Features list
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildFeatureItem('Tự động ghi nhận chi tiêu từ SMS ngân hàng'),
              const SizedBox(height: 12),
              _buildFeatureItem('AI phân tích và đưa ra gợi ý tiết kiệm'),
              const SizedBox(height: 12),
              _buildFeatureItem('Theo dõi mục tiêu và báo cáo chi tiết'),
            ],
          ),
          const SizedBox(height: 32),

          // Next button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: _nextPage,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primary,
                padding: const EdgeInsets.symmetric(vertical: 12),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              child: const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    'Bắt đầu',
                    style: TextStyle(fontSize: 14, color: Colors.white),
                  ),
                  SizedBox(width: 8),
                  Icon(Icons.arrow_forward, size: 16, color: Colors.white),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFeatureItem(String text) {
    return Row(
      children: [
        Container(
          width: 20,
          height: 20,
          decoration: const BoxDecoration(
            color: Color(0xFF10B981),
            shape: BoxShape.circle,
          ),
          child: const Icon(Icons.check, color: Colors.white, size: 14),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(fontSize: 14, color: Colors.black),
          ),
        ),
      ],
    );
  }

  Widget _buildPage2() {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(10, 10),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Icon
          Container(
            width: 80,
            height: 80,
            decoration: const BoxDecoration(
              color: Color(0xFF10B981),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.shield_outlined,
              color: Colors.white,
              size: 40,
            ),
          ),
          const SizedBox(height: 24),

          // Title
          const Text(
            'Bảo mật & Quyền riêng tư',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.normal,
              color: Colors.black,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // Description
          const Text(
            'Chúng tôi cam kết bảo vệ thông tin tài chính của bạn',
            style: TextStyle(
              fontSize: 16,
              color: Color(0xFF4A5565),
              height: 1.5,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 32),

          // Security features
          _buildSecurityItem(
            Icons.lock_outline,
            'Mã hóa dữ liệu:',
            'Tất cả giao dịch được mã hóa AES-256',
            const Color(0xFFDCFCE7),
            const Color(0xFF10B981),
          ),
          const SizedBox(height: 16),
          _buildSecurityItem(
            Icons.visibility_outlined,
            'Không thu thập nhạy cảm:',
            'Không yêu cầu mật khẩu/OTP ngân hàng',
            const Color(0xFFDEE9FC),
            const Color(0xFF155DFC),
          ),
          const SizedBox(height: 16),
          _buildSecurityItem(
            Icons.visibility_off_outlined,
            'Chỉ đọc SMS:',
            'Không gửi hoặc xóa tin nhắn của bạn',
            const Color(0xFFFCE7F3),
            const Color(0xFFD946EF),
          ),
          const SizedBox(height: 24),

          // Buttons
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: () {
                    _pageController.previousPage(
                      duration: const Duration(milliseconds: 300),
                      curve: Curves.easeInOut,
                    );
                  },
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    side: BorderSide(color: Colors.grey.shade300),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: const Text(
                    'Quay lại',
                    style: TextStyle(fontSize: 14, color: Colors.black),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: ElevatedButton(
                  onPressed: _nextPage,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'Tiếp tục',
                        style: TextStyle(fontSize: 14, color: Colors.white),
                      ),
                      SizedBox(width: 8),
                      Icon(Icons.arrow_forward, size: 16, color: Colors.white),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSecurityItem(
    IconData icon,
    String title,
    String description,
    Color bgColor,
    Color iconColor,
  ) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          Icon(icon, color: iconColor, size: 24),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: iconColor,
                  ),
                ),
                Text(
                  description,
                  style: TextStyle(
                    fontSize: 12,
                    color: iconColor.withOpacity(0.8),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPage3() {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(10, 10),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Icon
          Container(
            width: 80,
            height: 80,
            decoration: const BoxDecoration(
              color: Color(0xFFF97316),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.sms_outlined,
              color: Colors.white,
              size: 40,
            ),
          ),
          const SizedBox(height: 24),

          // Title
          const Text(
            'Cấp quyền đọc SMS',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.normal,
              color: Colors.black,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // Description
          const Text(
            'Để tự động ghi nhận giao dịch từ ngân hàng',
            style: TextStyle(
              fontSize: 16,
              color: Color(0xFF4A5565),
              height: 1.5,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 32),

          // Permission box
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFFF3F4F6),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              children: [
                const Text(
                  'Cấp quyền để tự động quét SMS ngân hàng',
                  style: TextStyle(
                    fontSize: 14,
                    color: Color(0xFF6B7280),
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 16),
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Cho phép đọc SMS',
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                                color: Colors.black,
                              ),
                            ),
                            Text(
                              _smsPermissionGranted
                                  ? '✅ Đã cấp quyền'
                                  : 'Tự động ghi nhận giao dịch',
                              style: TextStyle(
                                fontSize: 12,
                                color: _smsPermissionGranted
                                    ? Colors.green
                                    : const Color(0xFF6B7280),
                              ),
                            ),
                          ],
                        ),
                      ),
                      _isRequestingPermission
                          ? const SizedBox(
                              width: 24,
                              height: 24,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: AppColors.primary,
                              ),
                            )
                          : Switch(
                              value: _smsPermissionGranted,
                              onChanged: (value) {
                                // Khi toggle switch, luôn request permission
                                if (!_smsPermissionGranted) {
                                  _requestSmsPermission();
                                } else {
                                  // Nếu đã có quyền, hướng dẫn user vào Settings để tắt
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(
                                      content: Text(
                                        'Để tắt quyền SMS, vui lòng vào Cài đặt > Ứng dụng > FinPal > Quyền',
                                      ),
                                      backgroundColor: Colors.orange,
                                    ),
                                  );
                                }
                              },
                              activeColor: AppColors.primary,
                            ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Info
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFFFEF3C7),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              children: [
                const Icon(
                  Icons.info_outline,
                  color: Color(0xFFF59E0B),
                  size: 20,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Bạn có thể bỏ qua bước này và nhập chi tiêu thủ công. AI vẫn sẽ tự động phân loại và đưa ra gợi ý.',
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.brown.shade700,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 32),

          // Buttons
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: () {
                    _pageController.previousPage(
                      duration: const Duration(milliseconds: 300),
                      curve: Curves.easeInOut,
                    );
                  },
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    side: BorderSide(color: Colors.grey.shade300),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: const Text(
                    'Quay lại',
                    style: TextStyle(fontSize: 14, color: Colors.black),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: ElevatedButton(
                  onPressed: _nextPage,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'Tiếp tục',
                        style: TextStyle(fontSize: 14, color: Colors.white),
                      ),
                      SizedBox(width: 8),
                      Icon(Icons.arrow_forward, size: 16, color: Colors.white),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPage4() {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(10, 10),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Icon
          Container(
            width: 80,
            height: 80,
            decoration: const BoxDecoration(
              color: Color(0xFFA855F7),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.lightbulb_outline,
              color: Colors.white,
              size: 40,
            ),
          ),
          const SizedBox(height: 24),

          // Title
          const Text(
            'Sẵn sàng bắt đầu!',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.normal,
              color: Colors.black,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // Description
          const Text(
            'Khám phá các tính năng thông minh của FinPal',
            style: TextStyle(
              fontSize: 16,
              color: Color(0xFF4A5565),
              height: 1.5,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 32),

          // Features
          _buildSmartFeature(
            Icons.psychology_outlined,
            const Color(0xFF3B82F6),
            'AI phân loại thông minh',
            'Tự động nhận diện "GRAB" là Di chuyển, "SHOPEE" là Mua sắm',
          ),
          const SizedBox(height: 16),
          _buildSmartFeature(
            Icons.track_changes,
            const Color(0xFF10B981),
            'Cảnh báo chủ động',
            '"Bạn đã chi 70% hạn mức. Ăn uống, còn 10 ngày nữa hết tháng"',
          ),
          const SizedBox(height: 16),
          _buildSmartFeature(
            Icons.savings_outlined,
            const Color(0xFFA855F7),
            'Gợi ý tiết kiệm',
            '"Giảm trà sữa từ 200k xuống 100k/tuần, tiết kiệm 400k/tháng"',
          ),
          const SizedBox(height: 20),

          // Start button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: _completeOnboarding,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primary,
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              child: const Text(
                'Bắt đầu sử dụng FinPal',
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSmartFeature(
    IconData icon,
    Color color,
    String title,
    String description,
  ) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFFF9FAFB),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: color,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(icon, color: Colors.white, size: 24),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: Colors.black,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  description,
                  style: const TextStyle(
                    fontSize: 12,
                    color: Color(0xFF6B7280),
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
