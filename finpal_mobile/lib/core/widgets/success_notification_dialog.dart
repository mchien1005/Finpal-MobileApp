import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

class SuccessNotificationDialog extends StatelessWidget {
  final String message;
  final VoidCallback? onConfirm;

  const SuccessNotificationDialog({
    super.key,
    required this.message,
    this.onConfirm,
  });

  static Future<void> show(
    BuildContext context, {
    required String message,
    VoidCallback? onConfirm,
  }) {
    return showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) =>
          SuccessNotificationDialog(message: message, onConfirm: onConfirm),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      elevation: 50,
      backgroundColor: Colors.white,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 17),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.25),
              blurRadius: 50,
              spreadRadius: 49,
              offset: const Offset(13, 5),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Success Icon with glow effect
            Stack(
              alignment: Alignment.center,
              children: [
                SvgPicture.asset(
                  'assets/icons/success.svg',
                  width: 60,
                  height: 60,
                ),
              ],
            ),
            const SizedBox(height: 14),

            // Message text
            SizedBox(
              height: 30,
              child: Center(
                child: Text(
                  message,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    fontFamily: 'Arimo',
                    fontSize: 16,
                    fontWeight: FontWeight.w400,
                    color: Color(0xFF101828),
                    height: 1.875, // 30px line height / 16px font size
                  ),
                ),
              ),
            ),
            const SizedBox(height: 14),

            // Confirm button
            SizedBox(
              width: 320,
              height: 48,
              child: ElevatedButton(
                onPressed: () {
                  Navigator.of(context).pop();
                  onConfirm?.call();
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF00C950),
                  foregroundColor: Colors.white,
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                  padding: EdgeInsets.zero,
                ),
                child: const Text(
                  'Đồng ý',
                  style: TextStyle(
                    fontFamily: 'Arimo',
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    height: 1.43, // 20px line height / 14px font size
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
