import 'package:flutter/material.dart';
import '../constants/app_colors.dart';

class CustomDrawer extends StatefulWidget {
  final VoidCallback? onSettingsPressed;
  final VoidCallback? onLogoutPressed;
  final Function(String)? onLanguageChanged;
  final Function(bool)? onThemeChanged;
  final String userName;
  final String userEmail;
  final String? avatarUrl;

  const CustomDrawer({
    super.key,
    this.onSettingsPressed,
    this.onLogoutPressed,
    this.onLanguageChanged,
    this.onThemeChanged,
    this.userName = 'Nguyễn Văn A',
    this.userEmail = 'demo@finpal.com',
    this.avatarUrl,
  });

  @override
  State<CustomDrawer> createState() => _CustomDrawerState();
}

class _CustomDrawerState extends State<CustomDrawer> {
  String _selectedLanguage = 'VI';
  bool _isDarkMode = false;

  String _getInitials(String name) {
    if (name.isEmpty) return 'U';
    final parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return '${parts[0][0]}${parts[parts.length - 1][0]}'.toUpperCase();
    }
    return name[0].toUpperCase();
  }

  @override
  Widget build(BuildContext context) {
    return Drawer(
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(16),
          bottomLeft: Radius.circular(16),
        ),
      ),
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: const BorderRadius.only(
            topLeft: Radius.circular(16),
            bottomLeft: Radius.circular(16),
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
                        fontFamily: 'Arimo',
                      ),
                    ),
                    IconButton(
                      icon: Icon(
                        Icons.close,
                        size: 16,
                        color: Colors.black.withOpacity(0.7),
                      ),
                      onPressed: () => Navigator.pop(context),
                      padding: EdgeInsets.zero,
                      constraints: const BoxConstraints(),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 31),

              // User Profile Card
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Container(
                  decoration: BoxDecoration(
                    color: const Color(0xFFEC4899).withOpacity(0.15),
                    border: Border.all(color: AppColors.primary, width: 1.191),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  padding: const EdgeInsets.symmetric(
                    horizontal: 17.189,
                    vertical: 12,
                  ),
                  child: Row(
                    children: [
                      // User Info
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              widget.userName,
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w400,
                                color: Color(0xFF101828),
                                fontFamily: 'Arimo',
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              widget.userEmail,
                              style: const TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w400,
                                color: Color(0xFF4A5565),
                                fontFamily: 'Arimo',
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(width: 12),
                      // Avatar
                      Container(
                        width: 48,
                        height: 48,
                        decoration: BoxDecoration(
                          color: const Color(0xFF51A2FF),
                          shape: BoxShape.circle,
                          image:
                              widget.avatarUrl != null &&
                                  widget.avatarUrl!.isNotEmpty
                              ? DecorationImage(
                                  image: NetworkImage(widget.avatarUrl!),
                                  fit: BoxFit.cover,
                                )
                              : null,
                        ),
                        child:
                            widget.avatarUrl == null ||
                                widget.avatarUrl!.isEmpty
                            ? Center(
                                child: Text(
                                  _getInitials(widget.userName),
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 20,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              )
                            : null,
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 29),

              // Settings Button
              _buildMenuItem(
                icon: Icons.settings_outlined,
                label: 'Cài đặt',
                onTap: widget.onSettingsPressed,
              ),

              const SizedBox(height: 16),

              // Language Selector
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 0,
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        const Icon(
                          Icons.language_outlined,
                          size: 20,
                          color: Colors.black,
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'Ngôn ngữ',
                          style: TextStyle(
                            fontSize: 14,
                            color: Colors.black,
                            fontFamily: 'Arimo',
                          ),
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

              const SizedBox(height: 12),

              // Dark Mode Toggle
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 0,
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        const Icon(
                          Icons.brightness_6_outlined,
                          size: 20,
                          color: Colors.black,
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'Giao diện tối',
                          style: TextStyle(
                            fontSize: 14,
                            color: Colors.black,
                            fontFamily: 'Arimo',
                          ),
                        ),
                      ],
                    ),
                    Transform.scale(
                      scale: 0.7,
                      child: Switch(
                        value: _isDarkMode,
                        onChanged: (value) {
                          setState(() {
                            _isDarkMode = value;
                          });
                          widget.onThemeChanged?.call(value);
                        },
                        activeColor: Colors.white,
                        activeTrackColor: AppColors.primary,
                        inactiveThumbColor: Colors.white,
                        inactiveTrackColor: const Color(0xFFCBCED4),
                        materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 12),

              // Divider
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: Container(height: 1.191, color: Colors.grey[200]),
              ),

              const SizedBox(height: 14),

              // Logout Button
              _buildMenuItem(
                icon: Icons.logout_outlined,
                label: 'Đăng xuất',
                onTap: () {
                  Navigator.pop(context);
                  widget.onLogoutPressed?.call();
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildMenuItem({
    required IconData icon,
    required String label,
    VoidCallback? onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Row(
          children: [
            Icon(icon, size: 20, color: Colors.black),
            const SizedBox(width: 12),
            Text(
              label,
              style: const TextStyle(
                fontSize: 14,
                color: Colors.black,
                fontFamily: 'Arimo',
              ),
            ),
          ],
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
            color: isSelected
                ? const Color(0xFF155DFC)
                : const Color(0xFF4A5565),
            fontWeight: isSelected ? FontWeight.w500 : FontWeight.normal,
            fontFamily: 'Arimo',
          ),
        ),
      ),
    );
  }
}
