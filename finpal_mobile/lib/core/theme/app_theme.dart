import 'package:flutter/material.dart';

class AppColors {
  // Primary Colors
  static const Color primary = Color(0xFFD7006E);
  static const Color primaryLight = Color(0xFFEC4899);
  static const Color primaryLightBg = Color(0x26EC4899); // rgba(236,72,153,0.15)
  
  // Background Colors
  static const Color backgroundGradientStart = Color(0xFFFFFFFF);
  static const Color backgroundGradientMid = Color(0xFFFDFEFF);
  static const Color backgroundGradientEnd = Color(0xFFE0E7FF);
  
  // Card Colors
  static const Color cardIncome = Color(0xFF22C55E); // Green
  static const Color cardExpense = Color(0xFFEF4444); // Red
  static const Color cardBalance = Color(0xFF3B82F6); // Blue
  
  // Category Colors
  static const Color categoryFood = Color(0xFFEF4444); // Red-500
  static const Color categoryTransport = Color(0xFFF59E0B); // Amber-500
  static const Color categoryShopping = Color(0xFF8B5CF6); // Violet-500
  static const Color categoryEntertainment = Color(0xFFEC4899); // Pink-500
  static const Color categoryBills = Color(0xFF06B6D4); // Cyan-500
  
  // Text Colors
  static const Color textPrimary = Color(0xFF0A0A0A); // neutral-950
  static const Color textSecondary = Color(0xFF4A5565);
  static const Color textTertiary = Color(0xFF6A7282);
  
  // UI Colors
  static const Color white = Color(0xFFFFFFFF);
  static const Color gray200 = Color(0xFFE5E7EB);
  static const Color borderColor = Color(0x1A000000); // rgba(0,0,0,0.1)
  
  // Warning/Info Colors
  static const Color warningBg = Color(0xFFFFFBEB);
  static const Color warningBorder = Color(0xFFFEE685);
  static const Color warningOrange = Color(0xFFFE9A00);
  static const Color warningText = Color(0xFF7B3306);
  static const Color warningAccent = Color(0xFFBB4D00);
}

class AppTheme {
  static ThemeData get lightTheme {
    return ThemeData(
      primaryColor: AppColors.primary,
      scaffoldBackgroundColor: AppColors.white,
      useMaterial3: true,
      colorScheme: const ColorScheme.light(
        primary: AppColors.primary,
        secondary: AppColors.primaryLight,
        surface: AppColors.white,
      ),
      fontFamily: 'Arimo',
      textTheme: const TextTheme(
        displayLarge: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
        displayMedium: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        displaySmall: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
        headlineMedium: TextStyle(fontSize: 18, fontWeight: FontWeight.normal),
        titleLarge: TextStyle(fontSize: 16, fontWeight: FontWeight.normal),
        titleMedium: TextStyle(fontSize: 15, fontWeight: FontWeight.normal),
        bodyLarge: TextStyle(fontSize: 14, fontWeight: FontWeight.normal),
        bodyMedium: TextStyle(fontSize: 12, fontWeight: FontWeight.normal),
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.primary,
        foregroundColor: AppColors.white,
        elevation: 4,
      ),
    );
  }
}
