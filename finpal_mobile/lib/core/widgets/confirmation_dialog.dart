import 'package:flutter/material.dart';
import '../constants/app_colors.dart';

/// Reusable confirmation dialog widget
/// Usage: ConfirmationDialog.show(context, ...)
class ConfirmationDialog {
  static Future<bool?> show(
    BuildContext context, {
    required String title,
    required String message,
    String confirmText = 'Xác nhận',
    String cancelText = 'Hủy',
    Color confirmColor = AppColors.primary,
    VoidCallback? onConfirm,
  }) {
    return showDialog<bool>(
      context: context,
      builder: (BuildContext context) {
        return Dialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(24),
          ),
          child: Container(
            width: 350,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(24),
              border: Border.all(
                color: Colors.black.withOpacity(0.1),
                width: 1.145,
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
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Header
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 32.85, 20, 0),
                  child: Column(
                    children: [
                      // Title
                      Text(
                        title,
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Colors.black,
                          height: 28 / 18,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 8),
                      // Message
                      Text(
                        message,
                        style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.normal,
                          color: Color(0xFF717182),
                          height: 20 / 14,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 46),
                // Footer buttons
                Row(
                  children: [
                    // Cancel button
                    Expanded(
                      child: InkWell(
                        onTap: () => Navigator.of(context).pop(false),
                        borderRadius: const BorderRadius.only(
                          bottomLeft: Radius.circular(24),
                        ),
                        child: Container(
                          height: 48,
                          decoration: BoxDecoration(
                            color: Colors.white,
                            border: Border(
                              top: BorderSide(
                                color: Colors.black.withOpacity(0.1),
                                width: 1.145,
                              ),
                              right: BorderSide(
                                color: Colors.black.withOpacity(0.1),
                                width: 1.145,
                              ),
                            ),
                            borderRadius: const BorderRadius.only(
                              bottomLeft: Radius.circular(24),
                            ),
                          ),
                          child: Center(
                            child: Text(
                              cancelText,
                              style: const TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                                color: Colors.black,
                                height: 20 / 14,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                    // Confirm button
                    Expanded(
                      child: InkWell(
                        onTap: () {
                          Navigator.of(context).pop(true);
                          onConfirm?.call();
                        },
                        borderRadius: const BorderRadius.only(
                          bottomRight: Radius.circular(24),
                        ),
                        child: Container(
                          height: 48,
                          decoration: BoxDecoration(
                            color: confirmColor,
                            borderRadius: const BorderRadius.only(
                              bottomRight: Radius.circular(24),
                            ),
                          ),
                          child: Center(
                            child: Text(
                              confirmText,
                              style: const TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                                color: Colors.white,
                                height: 20 / 14,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
