import 'package:flutter/material.dart';
import '../constants/app_colors.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:flutter_svg/flutter_svg.dart';

class CustomBottomNavBar extends StatelessWidget {
  final int currentIndex;
  final Function(int) onTap;

  const CustomBottomNavBar({
    super.key,
    required this.currentIndex,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      clipBehavior: Clip.none,
      alignment: Alignment.bottomCenter,
      children: [
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.08),
                blurRadius: 8,
                offset: const Offset(0, -2),
              ),
            ],
          ),
          child: SafeArea(
            child: SizedBox(
              height: 70,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  _buildNavItem(
                    icon: SvgPicture.asset('assets/icons/home.svg'),
                    label: 'Tổng quan',
                    index: 0,
                  ),
                  _buildNavItem(
                    icon: MdiIcons.formatListBulleted,
                    label: 'Lịch sử',
                    index: 1,
                  ),
                  const Expanded(child: SizedBox()), // Spacer for center button
                  _buildNavItem(
                    icon: SvgPicture.asset('assets/icons/brain.svg'),
                    label: 'AI Gợi ý',
                    index: 3,
                  ),
                  _buildNavItem(
                    icon: MdiIcons.bullseye,
                    label: 'Mục tiêu',
                    index: 4,
                  ),
                ],
              ),
            ),
          ),
        ),
        Positioned(bottom: 20, child: _buildCenterButton(context)),
      ],
    );
  }

  Widget _buildNavItem({
    required dynamic icon,
    required String label,
    required int index,
  }) {
    final isSelected = currentIndex == index;
    return Expanded(
      child: InkWell(
        onTap: () => onTap(index),
        child: Container(
          margin: const EdgeInsets.symmetric(horizontal: 4),
          padding: const EdgeInsets.symmetric(vertical: 4),
          decoration: BoxDecoration(
            color: isSelected
                ? AppColors.primary.withOpacity(0.1)
                : Colors.transparent,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              icon is IconData
                  ? Icon(
                      icon,
                      color: isSelected ? AppColors.primary : Colors.grey[600],
                      size: 24,
                    )
                  : SizedBox(
                      width: 24,
                      height: 24,
                      child: ColorFiltered(
                        colorFilter: ColorFilter.mode(
                          isSelected ? AppColors.primary : Colors.grey[600]!,
                          BlendMode.srcIn,
                        ),
                        child: icon,
                      ),
                    ),
              const SizedBox(height: 4),
              Text(
                label,
                style: TextStyle(
                  fontSize: 10,
                  color: isSelected ? AppColors.primary : Colors.grey[600],
                  fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildCenterButton(BuildContext context) {
    final isSelected = currentIndex == 2;
    return GestureDetector(
      onTap: () => onTap(2),
      child: Container(
        width: 62,
        height: 64,
        decoration: BoxDecoration(
          color: AppColors.primary,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: (isSelected ? AppColors.primary : Colors.grey).withOpacity(
                0.4,
              ),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.add_circle_outline, color: Colors.white, size: 28),
            const SizedBox(height: 2),
            const Text(
              'Thêm',
              style: TextStyle(
                fontSize: 10,
                color: Colors.white,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
