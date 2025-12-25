import 'package:finpal_mobile/data/models/notification_settings.dart';
import 'package:finpal_mobile/data/services/api_service.dart';

class NotificationSettingsService {
  final ApiService _api = ApiService();

  Future<NotificationSettings> getSettings() async {
    final res = await _api.get('/notifications/settings');
    Map<String, dynamic> data = {};
    if (res.containsKey('data') && res['data'] is Map) {
      data = Map<String, dynamic>.from(res['data']);
    } else {
      data = Map<String, dynamic>.from(res);
    }
    return NotificationSettings.fromJson(data);
  }

  Future<NotificationSettings> updateSettings(NotificationSettings settings) async {
    final body = settings.toJson();
    final res = await _api.put('/notifications/settings', body);
    Map<String, dynamic> data = {};
    if (res.containsKey('data') && res['data'] is Map) {
      data = Map<String, dynamic>.from(res['data']);
    } else {
      data = Map<String, dynamic>.from(res);
    }
    return NotificationSettings.fromJson(data);
  }

  Future<void> resetSettings() async {
    await _api.post('/notifications/settings/reset', {});
  }
}
