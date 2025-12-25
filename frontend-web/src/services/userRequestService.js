import api from './api';

// Admin: get all user requests
export const getAdminRequests = async ({ page = 0, size = 20, sortBy = 'createdAt', sortDirection = 'DESC' } = {}) => {
  try {
    const res = await api.get('/user-requests/admin/all', {
      params: { page, size, sortBy, sortDirection },
    });

    // Possible response shapes seen in Swagger / real API:
    // 1) res.data.data -> paged object or array
    // 2) res.data -> paged object (content + pageable) or array
    // 3) res.data.content -> actual items when paged

    const body = res.data && res.data.data ? res.data.data : res.data;

    // If the body is a paged object with `content`, normalize to items + meta
    if (body && typeof body === 'object' && Array.isArray(body.content)) {
      return {
        items: body.content,
        page: body.pageNumber ?? page,
        size: body.pageSize ?? size,
        totalElements: body.totalElements ?? null,
        totalPages: body.totalPages ?? null,
        raw: body,
      };
    }

    // If body itself is an array
    if (Array.isArray(body)) {
      return {
        items: body,
        page,
        size: body.length,
        totalElements: body.length,
        totalPages: 1,
        raw: body,
      };
    }

    // Fallback: return empty list
    return { items: [], page, size, totalElements: 0, totalPages: 0, raw: body };
  } catch (err) {
    console.error('Error fetching admin requests:', err);
    throw err;
  }
};

// Admin: approve request
export const approveRequest = async (id, { adminNote = '' } = {}) => {
  try {
    const res = await api.put(`/user-requests/admin/${id}/approve`, { adminNote });
    return res.data;
  } catch (err) {
    console.error('Error approving request:', err);
    throw err;
  }
};

// Admin: reject request
export const rejectRequest = async (id, { adminNote = '' } = {}) => {
  try {
    const res = await api.put(`/user-requests/admin/${id}/reject`, { adminNote });
    return res.data;
  } catch (err) {
    console.error('Error rejecting request:', err);
    throw err;
  }
};

// Admin: cancel deletion request (Swagger: PUT /user-requests/admin/{id}/cancel)
export const cancelDeletionRequest = async (id, { adminNote = '' } = {}) => {
  try {
    const res = await api.put(`/user-requests/admin/${id}/cancel`, { adminNote });
    return res.data;
  } catch (err) {
    const msg = err?.response?.data?.message || err?.response?.data || err?.message || 'Không thể hủy yêu cầu';
    console.error('Error canceling deletion request:', err);
    throw new Error(msg);
  }
};

// Download exported data for a request (Swagger: GET /user-requests/admin/{id}/download-export)
export const downloadRequestData = async (id) => {
  try {
    const res = await api.get(`/user-requests/admin/${id}/download-export`, { responseType: 'blob' });
    return res;
  } catch (err) {
    // try common alternates
    try {
      const res2 = await api.get(`/user-requests/${id}/export`, { responseType: 'blob' });
      return res2;
    } catch (e2) {
      const msg = err?.response?.data?.message || err?.response?.data || err?.message || 'Không thể tải tệp xuất dữ liệu';
      console.error('Error downloading request data:', err, e2);
      throw new Error(msg);
    }
  }
};

// Admin: count pending requests
export const countPendingRequests = async () => {
  try {
    const res = await api.get('/user-requests/admin/pending-count');
    // Swagger indicates generic object; try common shapes
    const body = res.data && res.data.data ? res.data.data : res.data;
    // If body is number or contains count field
    if (typeof body === 'number') return body;
    if (body && typeof body === 'object') {
      return body.count ?? body.pendingCount ?? body.total ?? body.value ?? 0;
    }
    return 0;
  } catch (err) {
    console.error('Error counting pending requests:', err);
    throw err;
  }
};

// Admin: get deletion history (accounts that have been deleted)
export const getDeletionHistory = async ({ page = 0, size = 20, sortBy = 'deletedAt', sortDirection = 'DESC' } = {}) => {
  try {
    // Try official admin path first
    const res = await api.get('/user-requests/admin/deletion-history', {
      params: { page, size, sortBy, sortDirection },
    });

    const body = res.data && res.data.data ? res.data.data : res.data;

    if (body && typeof body === 'object' && Array.isArray(body.content)) {
      return {
        items: body.content,
        page: body.pageNumber ?? page,
        size: body.pageSize ?? size,
        totalElements: body.totalElements ?? null,
        totalPages: body.totalPages ?? null,
        raw: body,
      };
    }

    if (Array.isArray(body)) {
      return {
        items: body,
        page,
        size: body.length,
        totalElements: body.length,
        totalPages: 1,
        raw: body,
      };
    }

    // Fallbacks for alternative paths
    try {
      const alt = await api.get('/user-requests/deletion-history', { params: { page, size, sortBy, sortDirection } });
      const b2 = alt.data && alt.data.data ? alt.data.data : alt.data;
      if (b2 && typeof b2 === 'object' && Array.isArray(b2.content)) {
        return {
          items: b2.content,
          page: b2.pageNumber ?? page,
          size: b2.pageSize ?? size,
          totalElements: b2.totalElements ?? null,
          totalPages: b2.totalPages ?? null,
          raw: b2,
        };
      }
      if (Array.isArray(b2)) {
        return { items: b2, page, size: b2.length, totalElements: b2.length, totalPages: 1, raw: b2 };
      }
    } catch (e2) {
      // ignore and return empty below
    }

    return { items: [], page, size, totalElements: 0, totalPages: 0, raw: body };
  } catch (err) {
    console.error('Error fetching deletion history:', err);
    throw err;
  }
};

export default {
  getAdminRequests,
  approveRequest,
  rejectRequest,
  cancelDeletionRequest,
  downloadRequestData,
  countPendingRequests,
  getDeletionHistory,
};
