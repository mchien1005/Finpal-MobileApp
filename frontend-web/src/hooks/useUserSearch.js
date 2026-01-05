import { useState, useMemo } from 'react';

/**
 * Custom hook for user search and filtering
 * @param {Array} users - Array of user data
 * @returns {Object} - Search state and filtered users
 */
export const useUserSearch = (users) => {
  const [searchText, setSearchText] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('Tất cả');
  const [advancedFilters, setAdvancedFilters] = useState({
    minTransactions: '',
    registeredDate: null,
  });

  // Filter users based on search text, status, and advanced filters
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

    // Apply advanced filters
    if (advancedFilters.minTransactions) {
      result = result.filter(user => {
        const transactions = parseInt(user.transactions);
        return transactions >= parseInt(advancedFilters.minTransactions);
      });
    }

    if (advancedFilters.registeredDate) {
      result = result.filter(user => {
        // Simple date comparison - in real app would need proper date parsing
        return user.registeredDate === advancedFilters.registeredDate.format('DD/MM/YYYY');
      });
    }

    return result;
  }, [users, searchText, selectedStatus, advancedFilters]);

  return {
    searchText,
    setSearchText,
    selectedStatus,
    setSelectedStatus,
    advancedFilters,
    setAdvancedFilters,
    filteredUsers,
    totalFiltered: filteredUsers.length,
  };
};
