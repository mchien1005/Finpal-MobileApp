import 'package:flutter/material.dart';
import '../constants/app_colors.dart';

class CustomDrawer extends StatefulWidget {
  final VoidCallback? onSettingsPressed;
  final VoidCallback? onLogoutPressed;
  final Function(String)? onLanguageChanged;
  final Function(bool)? onThemeChanged;

  const CustomDrawer({
    super.key,
    this.onSettingsPressed,
    this.onLogoutPressed,
    this.onLanguageChanged,
    this.onThemeChanged,
  });

  @override
  State<CustomDrawer> createState() => _CustomDrawerState();
}

class _CustomDrawerState extends State<CustomDrawer> {
  String _selectedLanguage = 'VI';
  bool _isDarkMode = false;

  @override
  Widget build(BuildContext context) {
    return Drawer(
      child: Container(
        color: Colors.white,
        child: SafeArea(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Menu',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: Colors.black,
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, size: 16),
                      onPressed: () => Navigator.pop(context),
                      padding: EdgeInsets.zero,
                      constraints: const BoxConstraints(),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 40),

              // Settings Button
              ListTile(
                leading: const Icon(Icons.settings_outlined, size: 20),
                title: const Text(
                  'Cài đặt',
                  style: TextStyle(fontSize: 14),
                ),
                onTap: widget.onSettingsPressed,
                contentPadding: const EdgeInsets.symmetric(horizontal: 16),
              ),

              // Language Selector
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.language_outlined, size: 20),
                        const SizedBox(width: 12),
                        const Text(
                          'Ngôn ngữ',
                          style: TextStyle(fontSize: 14),
                        ),
                      ],
                    ),
                    Container(
                      decoration: BoxDecoration(
                        color: Colors.grey[100],
                        borderRadius: BorderRadius.circular(10),
                      ),
                      padding: const EdgeInsets.all(4),
                      child: Row(
                        children: [
                          _buildLanguageButton('VI'),
                          const SizedBox(width: 8),
                          _buildLanguageButton('EN'),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              // Dark Mode Toggle
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.brightness_6_outlined, size: 20),
                        const SizedBox(width: 12),
                        const Text(
                          'Giao diện tối',
                          style: TextStyle(fontSize: 14),
                        ),
                      ],
                    ),
                    Switch(
                      value: _isDarkMode,
                      onChanged: (value) {
                        setState(() {
                          _isDarkMode = value;
                        });
                        widget.onThemeChanged?.call(value);
                      },
                      activeColor: AppColors.primary,
                    ),
                  ],
                ),
              ),

              // Divider
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Divider(thickness: 1),
              ),

              // Logout Button
              ListTile(
                leading: const Icon(Icons.logout_outlined, size: 20),
                title: const Text(
                  'Đăng xuất',
                  style: TextStyle(fontSize: 14),
                ),
                onTap: () {
                  Navigator.pop(context);
                  widget.onLogoutPressed?.call();
                },
                contentPadding: const EdgeInsets.symmetric(horizontal: 16),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildLanguageButton(String language) {
    final isSelected = _selectedLanguage == language;
    return GestureDetector(
      onTap: () {
        setState(() {
          _selectedLanguage = language;
        });
        widget.onLanguageChanged?.call(language);
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
        decoration: BoxDecoration(
          color: isSelected ? Colors.white : Colors.transparent,
          borderRadius: BorderRadius.circular(4),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.1),
                    blurRadius: 3,
                    offset: const Offset(0, 1),
                  ),
                ]
              : null,
        ),
        child: Text(
          language,
          style: TextStyle(
            fontSize: 14,
            color:
                isSelected ? const Color(0xFF155DFC) : const Color(0xFF4A5565),
            fontWeight: isSelected ? FontWeight.w500 : FontWeight.normal,
          ),
        ),
      ),
    );
  }
}
