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

// Download exported data (try common path)
export const downloadRequestData = async (id) => {
  try {
    // try export path
    const res = await api.get(`/user-requests/${id}/export`, { responseType: 'blob' });
    return res;
  } catch (err) {
    console.error('Error downloading request data (export):', err);
    // fallback to download path
    try {
      const res2 = await api.get(`/user-requests/${id}/download`, { responseType: 'blob' });
      return res2;
    } catch (err2) {
      console.error('Error downloading request data (download):', err2);
      throw err2;
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

export default {
  getAdminRequests,
  approveRequest,
  rejectRequest,
  downloadRequestData,
  countPendingRequests,
};
