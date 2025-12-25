import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/models/dashboard_model.dart';
import 'dart:math' as math;

class CategoryChartSection extends StatefulWidget {
  final List<CategorySpending> categories;

  const CategoryChartSection({super.key, required this.categories});

  @override
  State<CategoryChartSection> createState() => _CategoryChartSectionState();
}

class _CategoryChartSectionState extends State<CategoryChartSection> {
  String _formatCurrency(double amount) {
    final currencyFormat = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '',
      decimalDigits: 0,
    );

    if (amount >= 1000000) {
      return '${(amount / 1000000).toStringAsFixed(1)}M';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toStringAsFixed(1)}K';
    }
    return currencyFormat.format(amount);
  }

  // Danh sách màu sắc đẹp cho biểu đồ
  final List<Color> _chartColors = [
    const Color(0xFF8B5CF6), // Tím
    const Color(0xFFF59E0B), // Cam
    const Color(0xFFEF4444), // Đỏ
    const Color(0xFF10B981), // Xanh lá
    const Color(0xFF3B82F6), // Xanh dương
    const Color(0xFFEC4899), // Hồng
    const Color(0xFF6366F1), // Indigo
    const Color(0xFF14B8A6), // Teal
  ];

  Color _getCategoryColor(int index) {
    return _chartColors[index % _chartColors.length];
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '',
      decimalDigits: 0,
    );

    // Lọc categories có amount > 0 và sắp xếp theo percentage giảm dần
    final validCategories =
        widget.categories.where((c) => c.amount > 0).toList()
          ..sort((a, b) => b.percentage.compareTo(a.percentage));

    final hasRealData = validCategories.isNotEmpty;

    if (!hasRealData) {
      return Container(
        padding: const EdgeInsets.all(32),
        decoration: BoxDecoration(
          color: AppColors.white,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: const Color(0x1A000000), width: 1.12),
        ),
        child: Column(
          children: [
            Icon(
              Icons.pie_chart_outline_rounded,
              size: 80,
              color: Colors.grey[300],
            ),
            const SizedBox(height: 16),
            const Text(
              'Chưa có dữ liệu chi tiêu',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Hãy thêm giao dịch đầu tiên của bạn!',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 14, color: AppColors.textSecondary),
            ),
          ],
        ),
      );
    }

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0x1A000000), width: 1.12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.only(left: 8, bottom: 16),
            child: Text(
              'Phân loại chi tiêu',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary,
              ),
            ),
          ),

          // Biểu đồ với labels sử dụng CustomPainter
          SizedBox(
            height: 320, // Tăng chiều cao
            child: LayoutBuilder(
              builder: (context, constraints) {
                final width = constraints.maxWidth;
                final height = 320.0;
                final centerX = width / 2;
                final centerY = height / 2;
                final radius = 75.0; // Tăng kích thước biểu đồ

                // Tính toán vị trí labels chia 2 bên
                final labelPositions = _calculateLabelPositions(
                  validCategories,
                  centerX,
                  centerY,
                  radius,
                  width,
                  height,
                );

                return Stack(
                  children: [
                    // CustomPainter vẽ đường lines
                    CustomPaint(
                      size: Size(width, height),
                      painter: LabelLinesPainter(
                        categories: validCategories,
                        labelPositions: labelPositions,
                        centerX: centerX,
                        centerY: centerY,
                        radius: radius,
                        colors: List.generate(
                          validCategories.length,
                          (i) => _getCategoryColor(i),
                        ),
                      ),
                    ),

                    // Biểu đồ tròn ở giữa
                    Positioned(
                      left: centerX - radius,
                      top: centerY - radius,
                      child: SizedBox(
                        width: radius * 2,
                        height: radius * 2,
                        child: PieChart(
                          PieChartData(
                            sectionsSpace: 1,
                            centerSpaceRadius: 0,
                            startDegreeOffset: -90,
                            borderData: FlBorderData(show: false),
                            sections: _buildPieSections(validCategories),
                          ),
                        ),
                      ),
                    ),

                    // Labels - xuống dòng: Tên\n%
                    ...labelPositions.map((pos) {
                      final index = pos['index'] as int;
                      final color = _getCategoryColor(index);
                      final category = validCategories[index];
                      final isLeft = pos['isLeft'] as bool;

                      return Positioned(
                        left: isLeft ? 0 : null,
                        right: isLeft ? null : 0,
                        top: (pos['labelY'] as double) - 14,
                        width: centerX - radius - 20,
                        child: Column(
                          crossAxisAlignment: isLeft
                              ? CrossAxisAlignment.end
                              : CrossAxisAlignment.start,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              category.categoryName,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w600,
                                color: color,
                              ),
                              textAlign: isLeft
                                  ? TextAlign.right
                                  : TextAlign.left,
                              overflow: TextOverflow.ellipsis,
                            ),
                            Text(
                              '${category.percentage.toStringAsFixed(0)}%',
                              style: TextStyle(
                                fontSize: 10,
                                fontWeight: FontWeight.w500,
                                color: color.withValues(alpha: 0.8),
                              ),
                              textAlign: isLeft
                                  ? TextAlign.right
                                  : TextAlign.left,
                            ),
                          ],
                        ),
                      );
                    }),
                  ],
                );
              },
            ),
          ),

          const SizedBox(height: 20),

          // Legend chi tiết
          ...validCategories.asMap().entries.map((entry) {
            final index = entry.key;
            final category = entry.value;
            final color = _getCategoryColor(index);

            return Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Row(
                children: [
                  Container(
                    width: 16,
                    height: 16,
                    decoration: BoxDecoration(
                      color: color,
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      category.categoryName,
                      style: const TextStyle(
                        fontSize: 14,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ),
                  Text(
                    '${currencyFormat.format(category.amount)} đ',
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  /// Tính toán vị trí labels chia 2 bên và phân bổ đều
  List<Map<String, dynamic>> _calculateLabelPositions(
    List<CategorySpending> categories,
    double centerX,
    double centerY,
    double radius,
    double width,
    double height,
  ) {
    final leftLabels = <Map<String, dynamic>>[];
    final rightLabels = <Map<String, dynamic>>[];

    double currentAngle = -90;

    // Bước 1: Chia labels thành 2 bên theo góc
    for (var i = 0; i < categories.length; i++) {
      final sweepAngle = categories[i].percentage / 100 * 360;
      final midAngle = currentAngle + sweepAngle / 2;
      final radians = midAngle * math.pi / 180;

      // Điểm trên biểu đồ
      final chartX = centerX + radius * math.cos(radians);
      final chartY = centerY + radius * math.sin(radians);

      // Normalize angle
      double normalizedAngle = midAngle;
      while (normalizedAngle > 180) normalizedAngle -= 360;
      while (normalizedAngle < -180) normalizedAngle += 360;

      final isLeft = normalizedAngle < -90 || normalizedAngle >= 90;

      final entry = {
        'index': i,
        'angle': midAngle,
        'radians': radians,
        'chartX': chartX,
        'chartY': chartY,
        'isLeft': isLeft,
      };

      if (isLeft) {
        leftLabels.add(entry);
      } else {
        rightLabels.add(entry);
      }

      currentAngle += sweepAngle;
    }

    // Bước 2: Sắp xếp theo góc và phân bổ Y đều trong mỗi bên
    leftLabels.sort(
      (a, b) => (a['angle'] as double).compareTo(b['angle'] as double),
    );
    rightLabels.sort(
      (a, b) => (a['angle'] as double).compareTo(b['angle'] as double),
    );

    const minY = 30.0;
    final maxY = height - 30.0;
    const labelHeight = 28.0; // Tăng vì có 2 dòng

    // Phân bổ labels bên trái
    if (leftLabels.isNotEmpty) {
      final spacing = (maxY - minY) / (leftLabels.length + 1);
      for (var i = 0; i < leftLabels.length; i++) {
        leftLabels[i]['labelY'] = minY + spacing * (i + 1);
        leftLabels[i]['labelX'] = centerX - radius - 20.0;
      }
    }

    // Phân bổ labels bên phải
    if (rightLabels.isNotEmpty) {
      final spacing = (maxY - minY) / (rightLabels.length + 1);
      for (var i = 0; i < rightLabels.length; i++) {
        rightLabels[i]['labelY'] = minY + spacing * (i + 1);
        rightLabels[i]['labelX'] = centerX + radius + 20.0;
      }
    }

    return [...leftLabels, ...rightLabels];
  }

  List<PieChartSectionData> _buildPieSections(
    List<CategorySpending> categories,
  ) {
    return categories.asMap().entries.map((entry) {
      final index = entry.key;
      final color = _getCategoryColor(index);

      return PieChartSectionData(
        value: entry.value.percentage,
        title: '',
        color: color,
        radius: 75, // Tăng radius
      );
    }).toList();
  }
}

/// Custom Painter để vẽ đường lines từ biểu đồ đến labels
class LabelLinesPainter extends CustomPainter {
  final List<CategorySpending> categories;
  final List<Map<String, dynamic>> labelPositions;
  final double centerX;
  final double centerY;
  final double radius;
  final List<Color> colors;

  LabelLinesPainter({
    required this.categories,
    required this.labelPositions,
    required this.centerX,
    required this.centerY,
    required this.radius,
    required this.colors,
  });

  @override
  void paint(Canvas canvas, Size size) {
    for (var pos in labelPositions) {
      final index = pos['index'] as int;
      final color = colors[index];
      final isLeft = pos['isLeft'] as bool;

      final paint = Paint()
        ..color = color
        ..strokeWidth = 1.2
        ..style = PaintingStyle.stroke;

      // Điểm bắt đầu trên biểu đồ
      final startX = pos['chartX'] as double;
      final startY = pos['chartY'] as double;

      // Điểm giữa (ra ngoài một chút)
      final radians = pos['radians'] as double;
      final midX = centerX + (radius + 15) * math.cos(radians);
      final midY = centerY + (radius + 15) * math.sin(radians);

      // Điểm kết thúc (ngang ra label)
      final endX = isLeft ? centerX - radius - 15 : centerX + radius + 15;
      final endY = pos['labelY'] as double;

      // Vẽ đường polyline: start -> mid -> end
      final path = Path()
        ..moveTo(startX, startY)
        ..lineTo(midX, midY)
        ..lineTo(endX, endY);

      canvas.drawPath(path, paint);

      // Vẽ chấm nhỏ ở cuối
      final dotPaint = Paint()
        ..color = color
        ..style = PaintingStyle.fill;
      canvas.drawCircle(Offset(endX, endY), 2.5, dotPaint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}
