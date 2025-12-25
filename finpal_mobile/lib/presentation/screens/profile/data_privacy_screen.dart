import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/widgets/confirmation_dialog.dart';
import '../../../data/services/user_request_service.dart';
import '../../../data/services/api_service.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

class DataPrivacyScreen extends StatelessWidget {
  const DataPrivacyScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      body: SafeArea(
        child: Container(
          color: const Color(0xFFEFF6FF),
          child: Column(
            children: [
              // Custom App Bar
              Container(
                decoration: BoxDecoration(
                  color: AppColors.primary,
                  borderRadius: const BorderRadius.only(
                    bottomLeft: Radius.circular(24),
                    bottomRight: Radius.circular(24),
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 15,
                      offset: const Offset(0, 10),
                    ),
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 6,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(16, 8, 16, 20),
                  child: Row(
                    children: [
                      InkWell(
                        onTap: () => Navigator.pop(context),
                        borderRadius: BorderRadius.circular(8),
                        child: const Padding(
                          padding: EdgeInsets.symmetric(
                            horizontal: 4,
                            vertical: 8,
                          ),
                          child: Row(
                            children: [
                              Icon(
                                Icons.arrow_back_ios,
                                color: Colors.white,
                                size: 14,
                              ),
                              SizedBox(width: 4),
                              Text(
                                'Quay lại',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 14,
                                  fontFamily: 'Arimo',
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const Spacer(),
                      const Text(
                        'Quyền riêng tư & Dữ liệu',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          fontFamily: 'Arimo',
                        ),
                      ),
                      const Spacer(),
                      const SizedBox(width: 60),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 24),

              // Content
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Export Data Card
                      _buildActionCard(
                        context: context,
                        icon: Icons.download_outlined,
                        title: 'Xuất dữ liệu cá nhân',
                        subtitle: 'Tải xuống toàn bộ dữ liệu của bạn',
                        onTap: () => _showExportDataDialog(context),
                      ),

                      const SizedBox(height: 12),

                      // Delete Account Card
                      _buildActionCard(
                        context: context,
                        icon: MdiIcons.trashCanOutline,
                        title: 'Yêu cầu xóa tài khoản',
                        subtitle: 'Xóa vĩnh viễn tài khoản và dữ liệu',
                        onTap: () => _showDeleteAccountDialog(context),
                      ),

                      const SizedBox(height: 24),

                      // Data Protection Info Section
                      _buildInfoSection(
                        icon: Icons.shield_outlined,
                        title: 'Bảo vệ dữ liệu của bạn',
                        content:
                            'Chúng tôi cam kết bảo vệ quyền riêng tư và dữ liệu cá nhân của bạn theo quy định GDPR và luật pháp Việt Nam.',
                      ),

                      const SizedBox(height: 20),

                      // Important Information Section
                      _buildImportantInfoSection(),

                      const SizedBox(height: 24),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildActionCard({
    required BuildContext context,
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(14),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 6,
              offset: const Offset(0, 4),
            ),
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 4,
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
                color: const Color(0xFFFDFDFD),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, size: 24, color: const Color(0xFF101828)),
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
                      color: Color(0xFF101828),
                      fontFamily: 'Arimo',
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    subtitle,
                    style: const TextStyle(
                      fontSize: 14,
                      color: Color(0xFF6A7282),
                      fontFamily: 'Arimo',
                    ),
                  ),
                ],
              ),
            ),
            const Icon(
              Icons.arrow_forward_ios,
              size: 16,
              color: Color(0xFF6A7282),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoSection({
    required IconData icon,
    required String title,
    required String content,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFE5E7EB), width: 1),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 24, color: const Color(0xFF6A7282)),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF101828),
                    fontFamily: 'Arimo',
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  content,
                  style: const TextStyle(
                    fontSize: 14,
                    color: Color(0xFF6A7282),
                    height: 1.5,
                    fontFamily: 'Arimo',
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildImportantInfoSection() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFE5E7EB), width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                MdiIcons.bookOpenPageVariantOutline,
                size: 24,
                color: Color(0xFF6A7282),
              ),
              SizedBox(width: 12),
              Text(
                'Thông tin quan trọng',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: Color(0xFF101828),
                  fontFamily: 'Arimo',
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          _buildBulletPoint(
            'Dữ liệu xuất bao gồm: thông tin hồ sơ, giao dịch, ngân sách, và mục tiêu tiết kiệm',
          ),
          const SizedBox(height: 8),
          _buildBulletPoint(
            'Yêu cầu xóa tài khoản sẽ được xử lý trong vòng 24-48 giờ',
          ),
          const SizedBox(height: 8),
          _buildBulletPoint(
            'Sau khi xóa tài khoản, dữ liệu sẽ bị xóa vĩnh viễn và không thể khôi phục',
          ),
        ],
      ),
    );
  }

  Widget _buildBulletPoint(String text) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.only(left: 36, top: 6),
          child: Icon(Icons.circle, size: 6, color: Color(0xFF6A7282)),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(
              fontSize: 14,
              color: Color(0xFF6A7282),
              height: 1.5,
              fontFamily: 'Arimo',
            ),
          ),
        ),
      ],
    );
  }

  void _showExportDataDialog(BuildContext context) async {
    final result = await ConfirmationDialog.show(
      context,
      title: 'Xuất dữ liệu',
      message:
          'Bạn có muốn xuất toàn bộ dữ liệu cá nhân của mình không? Dữ liệu sẽ được gửi đến email của bạn.',
      icon: MdiIcons.downloadOutline,
      iconColor: AppColors.primary,
      confirmColor: AppColors.primary,
      confirmText: 'Xác nhận',
      cancelText: 'Hủy',
    );

    if (result == true && context.mounted) {
      // Show loading dialog
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => const Center(
          child: CircularProgressIndicator(color: AppColors.primary),
        ),
      );

      try {
        final userRequestService = UserRequestService();
        await userRequestService.requestExportData();

        if (context.mounted) {
          Navigator.pop(context); // Close loading dialog
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text(
                'Yêu cầu xuất dữ liệu đã được gửi! Chúng tôi sẽ gửi dữ liệu về email của bạn.',
              ),
              backgroundColor: AppColors.primary,
            ),
          );
        }
      } on ApiException catch (e) {
        if (context.mounted) {
          Navigator.pop(context); // Close loading dialog
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(e.message), backgroundColor: Colors.red),
          );
        }
      } catch (e) {
        if (context.mounted) {
          Navigator.pop(context); // Close loading dialog
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Có lỗi xảy ra. Vui lòng thử lại sau.'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }

  void _showDeleteAccountDialog(BuildContext context) async {
    final result = await ConfirmationDialog.show(
      context,
      title: 'Xóa tài khoản',
      message:
          'Bạn có chắc chắn muốn xóa tài khoản?\n\n• Tất cả dữ liệu sẽ bị xóa vĩnh viễn\n• Không thể khôi phục sau khi xóa\n• Yêu cầu sẽ được xử lý trong 24-48 giờ',
      icon: Icons.warning_amber_rounded,
      iconColor: Colors.red,
      confirmColor: Colors.red,
      confirmText: 'Tiếp tục',
      cancelText: 'Hủy',
    );

    if (result == true && context.mounted) {
      _showDeleteConfirmationDialog(context);
    }
  }

  void _showDeleteConfirmationDialog(BuildContext context) async {
    final result = await ConfirmationDialog.showWithTextConfirmation(
      context,
      title: 'Xác nhận xóa tài khoản',
      message: 'Nhập "XÓA TÀI KHOẢN" để xác nhận:',
      confirmationText: 'XÓA TÀI KHOẢN',
      confirmText: 'Xóa tài khoản',
      cancelText: 'Hủy',
      confirmColor: Colors.red,
      icon: Icons.delete_forever_outlined,
      iconColor: Colors.red,
    );

    if (result == true && context.mounted) {
      // Show loading dialog
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) =>
            const Center(child: CircularProgressIndicator(color: Colors.red)),
      );

      try {
        final userRequestService = UserRequestService();
        await userRequestService.requestDeleteAccount();

        if (context.mounted) {
          Navigator.pop(context); // Close loading dialog
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text(
                'Yêu cầu xóa tài khoản đã được gửi. Chúng tôi sẽ xử lý trong 24-48 giờ.',
              ),
              backgroundColor: Colors.red,
            ),
          );
        }
      } on ApiException catch (e) {
        if (context.mounted) {
          Navigator.pop(context); // Close loading dialog
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(e.message), backgroundColor: Colors.red),
          );
        }
      } catch (e) {
        if (context.mounted) {
          Navigator.pop(context); // Close loading dialog
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Có lỗi xảy ra. Vui lòng thử lại sau.'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }
}
