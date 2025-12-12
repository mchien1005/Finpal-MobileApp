import 'package:flutter/material.dart';

class HelpCenterScreen extends StatefulWidget {
  const HelpCenterScreen({super.key});

  @override
  State<HelpCenterScreen> createState() => _HelpCenterScreenState();
}

class _HelpCenterScreenState extends State<HelpCenterScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _expandedFaqId = '';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      body: Column(
        children: [
          // Header with gradient and search
          Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFFD7006E), Color(0xFFE91E8C)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.only(
                bottomLeft: Radius.circular(24),
                bottomRight: Radius.circular(24),
              ),
            ),
            child: SafeArea(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    // Top bar
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        InkWell(
                          onTap: () => Navigator.pop(context),
                          child: const Row(
                            children: [
                              Icon(
                                Icons.arrow_back_ios,
                                color: Colors.white,
                                size: 20,
                              ),
                              Text(
                                'Quay lại',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                ),
                              ),
                            ],
                          ),
                        ),
                        const Text(
                          'Trung tâm trợ giúp',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(width: 80), // For balance
                      ],
                    ),
                    const SizedBox(height: 16),

                    // Search bar
                    Container(
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: TextField(
                        controller: _searchController,
                        decoration: InputDecoration(
                          hintText: 'Tìm kiếm câu hỏi...',
                          hintStyle: TextStyle(
                            color: Colors.grey[400],
                            fontSize: 14,
                          ),
                          prefixIcon: Icon(
                            Icons.search,
                            color: Colors.grey[400],
                          ),
                          border: InputBorder.none,
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 12,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],
                ),
              ),
            ),
          ),

          // Content
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Contact section
                  const Text(
                    'Liên hệ hỗ trợ',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF0A0A0A),
                    ),
                  ),
                  const SizedBox(height: 12),

                  // Live chat
                  _buildContactCard(
                    icon: Icons.chat_bubble_outline,
                    iconColor: const Color(0xFF2196F3),
                    title: 'Chat trực tiếp',
                    subtitle: 'Phản hồi trong vài phút',
                    onTap: () {
                      // TODO: Open live chat
                    },
                  ),

                  const SizedBox(height: 12),

                  // Email
                  _buildContactCard(
                    icon: Icons.email_outlined,
                    iconColor: const Color(0xFF00A63E),
                    title: 'Email',
                    subtitle: 'support@finpal.vn',
                    onTap: () {
                      // TODO: Open email
                    },
                  ),

                  const SizedBox(height: 12),

                  // Hotline
                  _buildContactCard(
                    icon: Icons.phone_outlined,
                    iconColor: const Color(0xFFFF9800),
                    title: 'Hotline',
                    subtitle: '1900-xxxx (8h-22h)',
                    onTap: () {
                      // TODO: Open phone dialer
                    },
                  ),

                  const SizedBox(height: 24),

                  // FAQ section
                  const Text(
                    'Câu hỏi thường gặp',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF0A0A0A),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Bắt đầu',
                    style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 12),

                  Container(
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(16),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 10,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: Column(
                      children: [
                        _buildFaqItem(
                          id: 'faq1',
                          question: 'FinPal là gì?',
                          answer:
                              'FinPal là ứng dụng quản lý tài chính cá nhân thông minh, giúp bạn theo dõi thu chi, lập kế hoạch tiết kiệm và đạt được mục tiêu tài chính của mình.',
                        ),
                        const Divider(height: 1),
                        _buildFaqItem(
                          id: 'faq2',
                          question: 'Làm sao để bắt đầu sử dụng FinPal?',
                          answer:
                              'Bạn chỉ cần tải ứng dụng, đăng ký tài khoản và bắt đầu ghi chép các giao dịch của mình. FinPal sẽ tự động phân loại và phân tích chi tiêu của bạn.',
                        ),
                        const Divider(height: 1),
                        _buildFaqItem(
                          id: 'faq3',
                          question: 'FinPal có miễn phí không?',
                          answer:
                              'FinPal có phiên bản miễn phí với đầy đủ tính năng cơ bản. Chúng tôi cũng có gói Premium với các tính năng nâng cao cho người dùng có nhu cầu cao hơn.',
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Security & Privacy section
                  Text(
                    'Bảo mật & Quyền riêng tư',
                    style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 12),

                  Container(
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(16),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 10,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: Column(
                      children: [
                        _buildFaqItem(
                          id: 'faq4',
                          question: 'Dữ liệu của tôi có an toàn không?',
                          answer:
                              'Dữ liệu của bạn được mã hóa AES-256 và lưu trữ an toàn trên máy chủ. Chúng tôi không chia sẻ thông tin cá nhân của bạn với bên thứ ba.',
                        ),
                        const Divider(height: 1),
                        _buildFaqItem(
                          id: 'faq5',
                          question:
                              'FinPal có chia sẻ thông tin của tôi không?',
                          answer:
                              'Không, chúng tôi cam kết bảo vệ quyền riêng tư của bạn. Thông tin cá nhân chỉ được sử dụng để cải thiện trải nghiệm sử dụng ứng dụng.',
                        ),
                        const Divider(height: 1),
                        _buildFaqItem(
                          id: 'faq6',
                          question: 'Làm sao để bảo vệ tài khoản tốt hơn?',
                          answer:
                              'Bạn nên sử dụng mật khẩu mạnh, bật xác thực hai yếu tố và không chia sẻ thông tin đăng nhập với người khác.',
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Features section
                  Text(
                    'Tính năng',
                    style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 12),

                  Container(
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(16),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 10,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: Column(
                      children: [
                        _buildFaqItem(
                          id: 'faq7',
                          question:
                              'AI phân loại giao dịch hoạt động như thế nào?',
                          answer:
                              'AI của FinPal sẽ tự động phân tích và phân loại các giao dịch của bạn dựa trên nội dung SMS, lịch sử giao dịch và mẫu chi tiêu.',
                        ),
                        const Divider(height: 1),
                        _buildFaqItem(
                          id: 'faq8',
                          question: 'Tôi có thể chỉnh sửa giao dịch không?',
                          answer:
                              'Có, bạn có thể chỉnh sửa bất kỳ giao dịch nào bao gồm danh mục, số tiền, ghi chú và ngày tháng.',
                        ),
                        const Divider(height: 1),
                        _buildFaqItem(
                          id: 'faq9',
                          question: 'Làm sao để đặt mục tiêu tiết kiệm?',
                          answer:
                              'Vào mục Tiết kiệm, nhấn nút thêm mục tiêu mới, nhập tên, số tiền và thời gian mong muốn. FinPal sẽ giúp bạn theo dõi tiến độ.',
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Troubleshooting section
                  Text(
                    'Khắc phục sự cố',
                    style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 12),

                  Container(
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(16),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 10,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: Column(
                      children: [
                        _buildFaqItem(
                          id: 'faq10',
                          question: 'App không đọc được SMS từ ngân hàng?',
                          answer:
                              'Hãy kiểm tra quyền đọc SMS trong cài đặt điện thoại. Nếu vẫn không được, hãy thử khởi động lại ứng dụng hoặc liên hệ hỗ trợ.',
                        ),
                        const Divider(height: 1),
                        _buildFaqItem(
                          id: 'faq11',
                          question: 'Giao dịch bị phân loại sai?',
                          answer:
                              'Bạn có thể chỉnh sửa phân loại cho giao dịch đó. AI sẽ học từ thay đổi của bạn để cải thiện độ chính xác cho lần sau.',
                        ),
                        const Divider(height: 1),
                        _buildFaqItem(
                          id: 'faq12',
                          question: 'Quên mật khẩu thì làm sao?',
                          answer:
                              'Tại màn hình đăng nhập, nhấn "Quên mật khẩu" và làm theo hướng dẫn để đặt lại mật khẩu qua email hoặc số điện thoại.',
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 24),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildContactCard({
    required IconData icon,
    required Color iconColor,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 10,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: iconColor.withOpacity(0.1),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: iconColor, size: 24),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF0A0A0A),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
            Icon(Icons.arrow_forward_ios, size: 16, color: Colors.grey[400]),
          ],
        ),
      ),
    );
  }

  Widget _buildFaqItem({
    required String id,
    required String question,
    required String answer,
  }) {
    final isExpanded = _expandedFaqId == id;

    return InkWell(
      onTap: () {
        setState(() {
          _expandedFaqId = isExpanded ? '' : id;
        });
      },
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Text(
                    question,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                      color: Color(0xFF0A0A0A),
                    ),
                  ),
                ),
                Icon(
                  isExpanded
                      ? Icons.keyboard_arrow_up
                      : Icons.keyboard_arrow_down,
                  color: Colors.grey[600],
                ),
              ],
            ),
            if (isExpanded) ...[
              const SizedBox(height: 12),
              Text(
                answer,
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.grey[700],
                  height: 1.5,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
