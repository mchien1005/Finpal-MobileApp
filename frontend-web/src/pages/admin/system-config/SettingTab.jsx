import React, { useEffect, useState } from 'react';
import adminSettingsService from '../../../services/adminSettings';
import SuccessModal from '../../../components/common/SuccessModal';

const AI_STRATEGIES = ['AI_FIRST', 'RULE_FIRST', 'HYBRID', 'RULE_ONLY'];
const RETRAIN_FREQS = ['DAILY', 'WEEKLY', 'MONTHLY'];

const SettingTab = () => {
  const [aiStrategy, setAiStrategy] = useState('RULE_FIRST');
  const [aiConfidence, setAiConfidence] = useState('0.75');
  const [aiRetrainFreq, setAiRetrainFreq] = useState('DAILY');
  const [notifEmail, setNotifEmail] = useState(false);
  const [notifPush, setNotifPush] = useState(false);
  const [maintenance, setMaintenance] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);

  const loadSettings = async () => {
    try {
      const list = await adminSettingsService.getSettings();
      if (!Array.isArray(list)) return;
      const getVal = (key, fallback) => {
        const item = list.find((s) => s.settingKey === key);
        return item ? item.settingValue : fallback;
      };
      setAiStrategy(getVal('ai.categorization.strategy', 'RULE_FIRST'));
      setAiConfidence(String(getVal('ai.confidence.threshold', '0.75')));
      setAiRetrainFreq(getVal('ai.retraining.frequency', 'DAILY'));
      setNotifEmail(String(getVal('notification.email.enabled', 'false')) === 'true');
      setNotifPush(String(getVal('notification.push.enabled', 'false')) === 'true');
      setMaintenance(String(getVal('system.maintenance.mode', 'false')) === 'true');
    } catch (e) {
      console.error('Load settings failed', e);
    }
  };

  useEffect(() => {
    loadSettings();
  }, []);

  const onSave = async () => {
    setLoading(true);
    try {
      const updates = [
        { settingKey: 'ai.categorization.strategy', settingValue: aiStrategy, key: 'ai.categorization.strategy', value: aiStrategy },
        { settingKey: 'ai.confidence.threshold', settingValue: aiConfidence, key: 'ai.confidence.threshold', value: aiConfidence },
        { settingKey: 'ai.retraining.frequency', settingValue: aiRetrainFreq, key: 'ai.retraining.frequency', value: aiRetrainFreq },
        { settingKey: 'notification.email.enabled', settingValue: notifEmail ? 'true' : 'false', key: 'notification.email.enabled', value: notifEmail ? 'true' : 'false' },
        { settingKey: 'notification.push.enabled', settingValue: notifPush ? 'true' : 'false', key: 'notification.push.enabled', value: notifPush ? 'true' : 'false' },
        { settingKey: 'system.maintenance.mode', settingValue: maintenance ? 'true' : 'false', key: 'system.maintenance.mode', value: maintenance ? 'true' : 'false' },
      ];
      await adminSettingsService.updateSettingsBatch(updates);
      setShowSuccess(true);
    } catch (e) {
      alert('Lỗi khi lưu: ' + (e.response?.data?.message || e.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20, color: '#0f172a' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
        <div>
          <div style={{ fontSize: 18, fontWeight: 700 }}>Cài đặt hệ thống Admin</div>
          <div style={{ fontSize: 13, color: '#475569' }}>Điều chỉnh AI, thông báo và bảo trì theo mẫu thiết kế</div>
        </div>
        <button
          onClick={onSave}
          disabled={loading}
          style={{
            padding: '10px 16px',
            background: loading ? '#94a3b8' : '#2563eb',
            color: '#fff',
            border: 'none',
            borderRadius: 10,
            cursor: loading ? 'not-allowed' : 'pointer',
            fontWeight: 600,
          }}
        >
          {loading ? 'Đang lưu...' : 'Lưu cấu hình'}
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 16 }}>
        <div style={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 16, boxShadow: '0 6px 18px rgba(15,23,42,0.04)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
            <div>
              <div style={{ fontWeight: 700 }}>AI Settings</div>
              <div style={{ fontSize: 12, color: '#64748b' }}>Chiến lược và ngưỡng tự động</div>
            </div>
            <span style={{ fontSize: 12, background: '#eef2ff', color: '#4338ca', padding: '4px 8px', borderRadius: 8 }}>AI</span>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <span style={{ fontWeight: 600 }}>Chiến lược phân loại</span>
              <select value={aiStrategy} onChange={(e) => setAiStrategy(e.target.value)} style={{ padding: '10px 12px', borderRadius: 10, border: '1px solid #e2e8f0', background: '#f8fafc' }}>
                {AI_STRATEGIES.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <span style={{ fontWeight: 600 }}>Ngưỡng độ tin cậy (0.0 - 1.0)</span>
              <input
                type="number"
                min="0"
                max="1"
                step="0.01"
                value={aiConfidence}
                onChange={(e) => setAiConfidence(e.target.value)}
                style={{ padding: '10px 12px', borderRadius: 10, border: '1px solid #e2e8f0', background: '#f8fafc' }}
              />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <span style={{ fontWeight: 600 }}>Tần suất huấn luyện lại</span>
              <select value={aiRetrainFreq} onChange={(e) => setAiRetrainFreq(e.target.value)} style={{ padding: '10px 12px', borderRadius: 10, border: '1px solid #e2e8f0', background: '#f8fafc' }}>
                {RETRAIN_FREQS.map((f) => (
                  <option key={f} value={f}>{f}</option>
                ))}
              </select>
            </label>
          </div>
        </div>

        <div style={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 16, boxShadow: '0 6px 18px rgba(15,23,42,0.04)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
            <div>
              <div style={{ fontWeight: 700 }}>Notification Settings</div>
              <div style={{ fontSize: 12, color: '#64748b' }}>Bật/tắt kênh thông báo hệ thống</div>
            </div>
            <span style={{ fontSize: 12, background: '#ecfeff', color: '#0ea5e9', padding: '4px 8px', borderRadius: 8 }}>NOTIF</span>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 12px', borderRadius: 10, border: '1px solid #e2e8f0', background: '#f8fafc' }}>
              <input type="checkbox" checked={notifEmail} onChange={(e) => setNotifEmail(e.target.checked)} />
              <span>Bật thông báo email</span>
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 12px', borderRadius: 10, border: '1px solid #e2e8f0', background: '#f8fafc' }}>
              <input type="checkbox" checked={notifPush} onChange={(e) => setNotifPush(e.target.checked)} />
              <span>Bật thông báo đẩy</span>
            </label>
          </div>
        </div>

        <div style={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 16, boxShadow: '0 6px 18px rgba(15,23,42,0.04)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
            <div>
              <div style={{ fontWeight: 700 }}>System</div>
              <div style={{ fontSize: 12, color: '#64748b' }}>Chế độ bảo trì toàn hệ thống</div>
            </div>
            <span style={{ fontSize: 12, background: maintenance ? '#fee2e2' : '#dcfce7', color: maintenance ? '#b91c1c' : '#166534', padding: '4px 8px', borderRadius: 8 }}>
              {maintenance ? 'Maintenance ON' : 'Normal'}
            </span>
          </div>
          <label style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 12px', borderRadius: 10, border: '1px solid #e2e8f0', background: '#f8fafc' }}>
            <input type="checkbox" checked={maintenance} onChange={(e) => setMaintenance(e.target.checked)} />
            <span>Bật chế độ bảo trì hệ thống</span>
          </label>
        </div>
      </div>

      <SuccessModal
        open={showSuccess}
        onClose={() => setShowSuccess(false)}
        message="Lưu cấu hình thành công!"
        buttonText="Đồng ý"
      />
    </div>
  );
};

export default SettingTab;
