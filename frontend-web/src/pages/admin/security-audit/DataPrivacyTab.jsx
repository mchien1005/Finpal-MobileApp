import React, { useEffect, useState } from 'react';
import { Card, Row, Col, Table, Tag, Button, Typography, message, Spin } from 'antd';
import {
  SafetyOutlined,
  WarningOutlined,
  LineChartOutlined,
  LockOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import * as userRequestService from '../../../services/userRequestService';

const { Title, Text } = Typography;

const DataPrivacyTab = () => {
  const [gdprData, setGdprData] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [deletionLoading, setDeletionLoading] = useState(false);
  const [deletionData, setDeletionData] = useState([]);
  const [delPage, setDelPage] = useState(0);
  const [delSize, setDelSize] = useState(20);
  const [delTotalElements, setDelTotalElements] = useState(0);

  useEffect(() => {
    fetchRequests();
    fetchDeletionHistory();
  }, []);

  const fetchRequests = async ({ page: p = page, size: s = size } = {}) => {
    try {
      setLoading(true);
      const resp = await userRequestService.getAdminRequests({ page: p, size: s });
      const list = Array.isArray(resp?.items) ? resp.items : [];

      const rows = list.map((r) => {
        const key = r.id || r.requestId || Math.random().toString(36).slice(2);
        const rawStatus = (r.status || r.requestStatus || '').toString().toUpperCase();

        let normalizedStatus = 'Chờ xử lý';
        if (['COMPLETED', 'PROCESSED', 'DONE', 'FINISHED'].includes(rawStatus)) {
          normalizedStatus = 'Hoàn thành';
        } else if (['APPROVED'].includes(rawStatus)) {
          normalizedStatus = 'Đã duyệt';
        } else if (['REJECTED', 'REJECT', 'DECLINED'].includes(rawStatus)) {
          normalizedStatus = 'Từ chối';
        } else if (['PENDING', 'CREATED', 'WAITING', 'NEW', 'SUBMITTED'].includes(rawStatus)) {
          normalizedStatus = 'Chờ xử lý';
        } else if (r.processed === true) {
          normalizedStatus = 'Hoàn thành';
        } else if (r.status && typeof r.status === 'string') {
          // fallback: use original string (localized)
          normalizedStatus = r.status;
        }

        const statusColor = normalizedStatus === 'Hoàn thành'
          ? '#dcfce7'
          : (normalizedStatus === 'Chờ xử lý' ? '#fef3c7' : (normalizedStatus === 'Đã duyệt' ? '#bfdbfe' : (normalizedStatus === 'Từ chối' ? '#fee2e2' : '#d1d5db')));
        const statusTextColor = normalizedStatus === 'Hoàn thành'
          ? '#065f46'
          : (normalizedStatus === 'Chờ xử lý' ? '#92400e' : (normalizedStatus === 'Đã duyệt' ? '#1e3a8a' : (normalizedStatus === 'Từ chối' ? '#dc2626' : '#374151')));

        const rawType = (r.requestType || r.type || (r.request && r.request.type) || '').toString().toUpperCase();
        let requestTypeDisplay = r.requestType || r.type || (r.request && r.request.type) || 'Unknown';
        // color mapping for request types
        let requestTypeColor = '#eef2ff';
        let requestTypeTextColor = '#374151';
        if (rawType === 'DELETE_ACCOUNT' || rawType === 'DELETE' || rawType === 'REMOVE_ACCOUNT') {
          requestTypeColor = '#fee2e2'; // light red
          requestTypeTextColor = '#dc2626';
        } else if (rawType === 'EXPORT_DATA' || rawType === 'EXPORT' || rawType === 'DOWNLOAD') {
          requestTypeColor = '#dbeafe'; // light blue
          requestTypeTextColor = '#155dfc';
        }

        return {
          key,
          requestId: r.requestId || r.id,
          user: r.userEmail || r.requester || (r.user && (r.user.email || r.user.username)) || r.email || r.username,
          requestType: requestTypeDisplay,
          requestTypeColor,
          requestTypeTextColor,
          requestDate: r.requestDate || r.createdAt || r.createdAtDisplay || '',
          status: normalizedStatus,
          statusColor,
          statusTextColor,
          raw: r,
        };
      });

      setGdprData(rows);
      setPage(resp.page ?? p);
      setSize(resp.size ?? s);
      setTotalElements(resp.totalElements ?? (Array.isArray(list) ? list.length : 0));
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Không thể tải yêu cầu GDPR';
      message.error(msg);
      console.error('Fetch requests error:', err?.response?.data || err);
    } finally {
      setLoading(false);
    }
  };

  const fetchDeletionHistory = async ({ page: p = delPage, size: s = delSize } = {}) => {
    try {
      setDeletionLoading(true);
      const resp = await userRequestService.getDeletionHistory({ page: p, size: s });
      const list = Array.isArray(resp?.items) ? resp.items : [];

      const rows = list.map((r) => {
        const key = r.id || Math.random().toString(36).slice(2);
        const email = r.userEmail || r.email || (r.user && r.user.email) || r.username;
        const approvedBy = r.approvedByUsername || r.approvedBy || r.admin || r.adminUsername;
        return {
          key,
          id: r.id,
          user: email,
          approvedBy,
          approvedAt: r.approvedAt || r.processedAt || r.createdAt,
          deletedAt: r.deletedAt || r.scheduledDeletionAt,
          raw: r,
        };
      });

      setDeletionData(rows);
      setDelPage(resp.page ?? p);
      setDelSize(resp.size ?? s);
      setDelTotalElements(resp.totalElements ?? (Array.isArray(list) ? list.length : 0));
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Không thể tải lịch sử xóa tài khoản';
      message.error(msg);
      console.error('Fetch deletion history error:', err?.response?.data || err);
    } finally {
      setDeletionLoading(false);
    }
  };

  // Table columns
  const columns = [
    {
      title: 'Mã yêu cầu',
      dataIndex: 'requestId',
      key: 'requestId',
      width: 120,
      render: (text) => (
        <Text style={{ color: '#155dfc', fontWeight: 500 }}>{text}</Text>
      ),
    },
    {
      title: 'Người dùng',
      dataIndex: 'user',
      key: 'user',
      width: 200,
    },
    {
      title: 'Loại yêu cầu',
      dataIndex: 'requestType',
      key: 'requestType',
      width: 170,
      render: (text, record) => (
        <Tag
          color={record.requestTypeColor}
          style={{
            border: 'none',
            borderRadius: 8,
            color: record.requestTypeTextColor,
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Ngày yêu cầu',
      dataIndex: 'requestDate',
      key: 'requestDate',
      width: 140,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 130,
      render: (text, record) => (
        <Tag
          color={record.statusColor}
          style={{
            border: 'none',
            borderRadius: 8,
            color: record.statusTextColor,
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 210,
      align: 'right',
      render: (_, record) => {
        const id = record.raw?.id || record.requestId;
        const handleApprove = async () => {
          try {
            setLoading(true);
            const res = await userRequestService.approveRequest(id);

            // Try to detect returned status from API
            const returnedStatus = (res && (res.status || res.requestStatus || res.state || res.result || res.data?.status)) || null;

            if (returnedStatus) {
              const up = returnedStatus.toString().toUpperCase();
              if (['COMPLETED', 'PROCESSED', 'DONE', 'FINISHED'].includes(up)) {
                message.success('Yêu cầu đã hoàn thành');
              } else if (['APPROVED'].includes(up)) {
                message.success('Yêu cầu đã được duyệt');
              } else {
                message.success('Đã duyệt — đang xử lý');
              }
            } else {
              // Unknown response shape — assume approved and being processed
              message.success('Đã duyệt — đang xử lý');
            }

            // Refresh list to get current status
            await fetchRequests();

            // Poll server to observe state transitions (APPROVED -> COMPLETED)
            const poll = async () => {
              let attempts = 0;
              const maxAttempts = 10;
              const intervalMs = 2000;

              const timer = setInterval(async () => {
                attempts += 1;
                try {
                  const snapshot = await userRequestService.getAdminRequests({ page: 0, size: 100 });
                  const found = Array.isArray(snapshot.items) ? snapshot.items.find(it => (it.id || it.requestId) == id) : null;
                  const s = (found && (found.status || found.requestStatus || found.state || found.result)) || null;
                  const up = s ? s.toString().toUpperCase() : null;

                  if (up) {
                    if (['COMPLETED', 'PROCESSED', 'DONE', 'FINISHED'].includes(up)) {
                      message.success('Yêu cầu đã hoàn thành');
                      clearInterval(timer);
                      await fetchRequests();
                      return;
                    }
                    if (['APPROVED'].includes(up)) {
                      message.success('Đã duyệt');
                      clearInterval(timer);
                      await fetchRequests();
                      return;
                    }
                  }

                  if (attempts >= maxAttempts) {
                    clearInterval(timer);
                    message.info('Đang xử lý trên server — kiểm tra lại sau');
                    await fetchRequests();
                  }
                } catch (err) {
                  console.error('Polling error:', err);
                  if (attempts >= maxAttempts) {
                    clearInterval(timer);
                    message.error('Không thể kiểm tra trạng thái yêu cầu');
                  }
                }
              }, intervalMs);
            };

            poll();
          } catch (err) {
            message.error('Không thể xử lý yêu cầu');
            console.error(err);
          } finally {
            setLoading(false);
          }
        };

        const handleReject = async () => {
          try {
            setLoading(true);
            await userRequestService.rejectRequest(id);
            message.success('Đã từ chối yêu cầu');
            await fetchRequests();
          } catch (err) {
            message.error('Không thể từ chối yêu cầu');
            console.error(err);
          } finally {
            setLoading(false);
          }
        };

        const handleDownload = async () => {
          try {
            setLoading(true);
            const res = await userRequestService.downloadRequestData(id);
            const blob = new Blob([res.data]);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            // Try to derive filename from Content-Disposition header
            const cd = (res && (res.headers && (res.headers['content-disposition'] || res.headers['Content-Disposition']))) || '';
            let filename = `request-${id}.zip`;
            if (cd) {
              const match = /filename\*=?UTF-8''([^;\n]+)|filename=\"?([^\";\n]+)\"?/.exec(cd);
              if (match) filename = decodeURIComponent(match[1] || match[2]);
            }
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
            message.success('Bắt đầu tải xuống');
          } catch (err) {
            const serverMsg = err?.message || (err?.response && (err.response.data?.message || err.response.data)) || 'Không thể tải xuống';
            message.error(serverMsg);
            console.error('Download error:', err);
          } finally {
            setLoading(false);
          }
        };

        if (record.status === 'Chờ xử lý') {
          return (
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <Button
                type="primary"
                size="small"
                onClick={handleApprove}
                style={{
                  background: '#155dfc',
                  borderColor: '#155dfc',
                  borderRadius: 8,
                  height: 32,
                  paddingLeft: 12,
                  paddingRight: 12,
                }}
              >
                Xử lý
              </Button>
              <Button
                size="small"
                onClick={handleReject}
                style={{
                  color: '#e7000b',
                  borderRadius: 8,
                  height: 32,
                  paddingLeft: 12,
                  paddingRight: 12,
                  border: '1px solid rgba(0,0,0,0.1)',
                }}
              >
                Từ chối
              </Button>
            </div>
          );
        } else if (record.status === 'Hoàn thành') {
          return (
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                icon={<DownloadOutlined />}
                size="small"
                onClick={handleDownload}
                style={{
                  borderRadius: 8,
                  height: 32,
                  paddingLeft: 12,
                  paddingRight: 12,
                  border: '1px solid rgba(0,0,0,0.1)',
                }}
              >
                Tải xuống
              </Button>
            </div>
          );
        } else if (record.status === 'Đã duyệt' || record.status === 'Chờ xử lý') {
          // show Cancel button for approved/pending delete-account requests
          const rawType = (record.raw && (record.raw.requestType || record.raw.type || (record.raw.request && record.raw.request.type))) || '';
          const rawTypeUp = rawType.toString().toUpperCase();
          const isDelete = rawTypeUp.includes('DELETE') || rawTypeUp.includes('REMOVE') || rawTypeUp.includes('DELETE_ACCOUNT');
          if (isDelete) {
            const handleCancel = async () => {
              try {
                setLoading(true);
                await userRequestService.cancelDeletionRequest(id);
                message.success('Yêu cầu xoá đã được hủy');
                await fetchRequests();
              } catch (err) {
                const serverMsg = err?.message || (err?.response && (err.response.data?.message || err.response.data)) || 'Không thể hủy yêu cầu';
                message.error(serverMsg);
                console.error('Cancel deletion error:', err);
              } finally {
                setLoading(false);
              }
            };

            return (
              <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                <Button
                  size="small"
                  onClick={handleCancel}
                  style={{
                    color: '#e7000b',
                    borderRadius: 8,
                    height: 32,
                    paddingLeft: 12,
                    paddingRight: 12,
                    border: '1px solid rgba(0,0,0,0.1)',
                  }}
                >
                  Hủy
                </Button>
              </div>
            );
          }
        }
        return null;
      },
    },
  ];

  const deletionColumns = [
    {
      title: 'Mã yêu cầu',
      dataIndex: 'id',
      key: 'id',
      width: 110,
    },
    {
      title: 'Người dùng',
      dataIndex: 'user',
      key: 'user',
      width: 220,
    },
    {
      title: 'Đã duyệt bởi',
      dataIndex: 'approvedBy',
      key: 'approvedBy',
      width: 180,
    },
    {
      title: 'Ngày duyệt',
      dataIndex: 'approvedAt',
      key: 'approvedAt',
      width: 180,
    },
    {
      title: 'Ngày xóa',
      dataIndex: 'deletedAt',
      key: 'deletedAt',
      width: 180,
      render: (text) => (
        <Tag color="#dcfce7" style={{ border: 'none', borderRadius: 8, color: '#065f46' }}>
          {text || 'Đang chờ xóa'}
        </Tag>
      ),
    },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>

      {/* Section Header */}
      <div>
        <Title level={4} style={{ margin: 0, marginBottom: 4 }}>
          Quyền riêng tư Dữ liệu & GDPR
        </Title>
        <Text type="secondary">Xử lý yêu cầu về dữ liệu cá nhân</Text>
      </div>

      {/* GDPR Requests Table */}
      <Card
        style={{
          borderRadius: 14,
          border: '1px solid rgba(0,0,0,0.1)',
        }}
        bodyStyle={{ padding: 1 }}
      >
        <Spin spinning={loading}>
          <Table
            columns={columns}
            dataSource={gdprData}
            pagination={{
              current: page + 1,
              pageSize: size,
              total: totalElements,
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50', '100'],
            }}
            onChange={(pagination) => {
              const newPage = (pagination.current || 1) - 1;
              const newSize = pagination.pageSize || size;
              fetchRequests({ page: newPage, size: newSize });
            }}
            scroll={{ x: 1000 }}
          />
        </Spin>
      </Card>

      {/* GDPR Compliance Info */}
      <Card
        style={{
          borderRadius: 14,
          border: '1px solid #bedbff',
          background: '#eff6ff',
        }}
        bodyStyle={{ padding: 24 }}
      >
        <div style={{ display: 'flex', gap: 12 }}>
          <SafetyOutlined style={{ fontSize: 24, color: '#1447e6' }} />
          <div>
            <Title level={5} style={{ margin: 0, marginBottom: 8, color: '#1c398e' }}>
              Tuân thủ GDPR
            </Title>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <Text style={{ color: '#1447e6' }}>
                ✓ Dữ liệu được mã hóa AES-256
              </Text>
              <Text style={{ color: '#1447e6' }}>
                ✓ Users có quyền xem, export, xóa dữ liệu cá nhân
              </Text>
              <Text style={{ color: '#1447e6' }}>
                ✓ Dữ liệu không được chia sẻ với bên thứ ba
              </Text>
              <Text style={{ color: '#1447e6' }}>
                ✓ Audit logs đầy đủ cho mọi thao tác
              </Text>
            </div>
          </div>
        </div>
      </Card>

      {/* Deletion History Table */}
      <Card
        style={{
          borderRadius: 14,
          border: '1px solid rgba(0,0,0,0.1)',
        }}
        bodyStyle={{ padding: 1 }}
      >
        <div style={{ padding: 16 }}>
          <Title level={5} style={{ margin: 0, marginBottom: 8 }}>
            Lịch sử tài khoản đã xóa
          </Title>
          <Text type="secondary">Danh sách tài khoản đã xóa hoặc lên lịch xóa</Text>
        </div>
        <Spin spinning={deletionLoading}>
          <Table
            columns={deletionColumns}
            dataSource={deletionData}
            pagination={{
              current: delPage + 1,
              pageSize: delSize,
              total: delTotalElements,
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50', '100'],
            }}
            onChange={(pagination) => {
              const newPage = (pagination.current || 1) - 1;
              const newSize = pagination.pageSize || delSize;
              fetchDeletionHistory({ page: newPage, size: newSize });
            }}
            scroll={{ x: 900 }}
          />
        </Spin>
      </Card>
    </div>
  );
};

export default DataPrivacyTab;

