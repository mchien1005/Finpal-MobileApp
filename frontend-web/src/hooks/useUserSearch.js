import { useState, useMemo } from 'react';

/**
 * Custom hook for user search and filtering
 * @param {Array} users - Array of user data
 * @returns {Object} - Search state and filtered users
 */
export const useUserSearch = (users) => {
  const [searchText, setSearchText] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('Tất cả');

  // Filter users based on search text and status
  const filteredUsers = useMemo(() => {
    let result = users;

    // Filter by search text (tìm kiếm theo tên, email, ID)
    if (searchText.trim()) {
      const searchLower = searchText.toLowerCase().trim();
      result = result.filter(user => {
        const matchUserId = user.userId?.toLowerCase().includes(searchLower);
        const matchName = user.user?.name?.toLowerCase().includes(searchLower);
        const matchEmail = user.contact?.email?.toLowerCase().includes(searchLower);
        const matchPhone = user.contact?.phone?.includes(searchText.trim());
        
        return matchUserId || matchName || matchEmail || matchPhone;
      });
    }

    // Filter by status
    if (selectedStatus !== 'Tất cả') {
      result = result.filter(user => user.status === selectedStatus);
    }

    return result;
  }, [users, searchText, selectedStatus]);

  return {
    searchText,
    setSearchText,
    selectedStatus,
    setSelectedStatus,
    filteredUsers,
    totalFiltered: filteredUsers.length,
  };
};
