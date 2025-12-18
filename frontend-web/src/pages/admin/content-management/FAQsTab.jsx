// import React, { useState, useEffect } from 'react';
// import { Button, Space, Spin, message } from 'antd';
// import {
//   PlusOutlined,
//   EditOutlined,
//   DeleteOutlined,
//   LoadingOutlined,
// } from '@ant-design/icons';
// import ConfirmModal from '../../../components/common/ConfirmModal';
// import FAQModal from '../../../components/admin/FAQModal';
// import {
//   getAllFAQs,
//   getFAQCategoryLabel,
//   getStatusLabel,
//   createFAQ,
//   updateFAQ,
//   deleteFAQ,
// } from '../../../services/contentService';

// const categoryStyles = {
//   'Tiết kiệm': { background: '#E0F2FE', color: '#0369A1' },
//   'Ngân sách': { background: '#FEF3C7', color: '#B45309' },
//   'Quản lý': { background: '#FEF3C7', color: '#B45309' },
//   'Đầu tư': { background: '#DCFCE7', color: '#15803D' },
//   'Chi tiêu': { background: '#FFE2E2', color: '#C10007' },
//   'Tổng quan': { background: '#F3E8FF', color: '#7C3AED' },
//   'Hướng dẫn': { background: '#E0E7FF', color: '#4338CA' },
//   'Bảo mật': { background: '#FCE7F3', color: '#BE185D' },
//   'Tính năng': { background: '#F3E8FF', color: '#7C3AED' },
//   'Khắc phục': { background: '#FEF3C7', color: '#B45309' },
// };

// const FAQsTab = () => {
//   // State for data
//   const [faqs, setFaqs] = useState([]);
  
//   // Loading states
//   const [loadingFaqs, setLoadingFaqs] = useState(false);

//   // FAQ modal states (thêm/sửa)
//   const [showFAQModal, setShowFAQModal] = useState(false);
//   const [editingFAQ, setEditingFAQ] = useState(null);
//   const [isSubmittingFAQ, setIsSubmittingFAQ] = useState(false);

//   // Delete FAQ states
//   const [deletingFAQ, setDeletingFAQ] = useState(null);
//   const [showConfirmDeleteFAQ, setShowConfirmDeleteFAQ] = useState(false);
//   const [isDeletingFAQ, setIsDeletingFAQ] = useState(false);

//   // Fetch FAQs
//   const fetchFaqs = async () => {
//     setLoadingFaqs(true);
//     try {
//       const data = await getAllFAQs();
//       setFaqs(data);
//     } catch (error) {
//       console.error('Error fetching FAQs:', error);
//       message.error('Không thể tải câu hỏi thường gặp');
//     } finally {
//       setLoadingFaqs(false);
//     }
//   };

//   // Fetch data on mount
//   useEffect(() => {
//     fetchFaqs();
//   }, []);

//   // Handle Add FAQ
//   const handleAddFAQClick = () => {
//     setEditingFAQ(null);
//     setShowFAQModal(true);
//   };

//   // Handle Edit FAQ
//   const handleEditFAQClick = (faq) => {
//     setEditingFAQ(faq);
//     setShowFAQModal(true);
//   };

//   // Handle Submit FAQ (Add/Edit)
//   const handleFAQSubmit = async (data) => {
//     setIsSubmittingFAQ(true);
//     try {
//       if (editingFAQ) {
//         await updateFAQ(editingFAQ.id, data);
//         message.success('Cập nhật FAQ thành công!');
//       } else {
//         // Tìm số lớn nhất từ faqCode hiện có (FAQ001, FAQ002,...)
//         let maxNumber = 0;
//         faqs.forEach((f) => {
//           const code = f.faqCode || '';
//           const match = code.match(/^FAQ(\d+)$/);
//           if (match) {
//             const num = parseInt(match[1], 10);
//             if (num > maxNumber) maxNumber = num;
//           }
//         });
//         const nextNumber = maxNumber + 1;
//         const faqData = {
//           ...data,
//           faqCode: `FAQ${String(nextNumber).padStart(3, '0')}`,
//         };
//         await createFAQ(faqData);
//         message.success('Thêm FAQ thành công!');
//       }
//       setShowFAQModal(false);
//       setEditingFAQ(null);
//       fetchFaqs();
//     } catch (error) {
//       console.error('Error submitting FAQ:', error);
//       message.error(
//         error.response?.data?.message || 
//         (editingFAQ ? 'Không thể cập nhật FAQ' : 'Không thể thêm FAQ')
//       );
//     } finally {
//       setIsSubmittingFAQ(false);
//     }
//   };

//   // Handle Close FAQ Modal
//   const handleCloseFAQModal = () => {
//     setShowFAQModal(false);
//     setEditingFAQ(null);
//   };

//   // Handle Delete FAQ Click
//   const handleDeleteFAQClick = (faq) => {
//     setDeletingFAQ(faq);
//     setShowConfirmDeleteFAQ(true);
//   };

//   // Handle Confirm Delete FAQ
//   const handleConfirmDeleteFAQ = async () => {
//     if (!deletingFAQ) return;
    
//     setIsDeletingFAQ(true);
//     try {
//       await deleteFAQ(deletingFAQ.id);
//       message.success('Xóa FAQ thành công!');
//       setShowConfirmDeleteFAQ(false);
//       setDeletingFAQ(null);
//       fetchFaqs();
//     } catch (error) {
//       console.error('Error deleting FAQ:', error);
//       message.error(error.response?.data?.message || 'Không thể xóa FAQ');
//     } finally {
//       setIsDeletingFAQ(false);
//     }
//   };

//   // Handle Cancel Delete FAQ
//   const handleCancelDeleteFAQ = () => {
//     setShowConfirmDeleteFAQ(false);
//     setDeletingFAQ(null);
//   };

//   return (
//     <>
//       <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
//         <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
//           <div>
//             <h3 style={{ fontSize: 16, fontWeight: 400, color: '#101828', margin: 0, fontFamily: 'Arimo, sans-serif' }}>Câu hỏi thường gặp</h3>
//             <p style={{ fontSize: 14, color: '#6A7282', margin: 0, fontFamily: 'Arimo, sans-serif' }}>Quản lý FAQ cho người dùng</p>
//           </div>
//           <Button type="primary" icon={<PlusOutlined />} onClick={handleAddFAQClick} style={{ background: '#155DFC', borderRadius: 8, height: 36, fontFamily: 'Arimo, sans-serif', fontSize: 14 }}>Thêm FAQ</Button>
//         </div>

//         <div style={{ background: '#FFFFFF', border: '1px solid rgba(0,0,0,0.1)', borderRadius: 14, overflow: 'hidden' }}>
//           {loadingFaqs ? (
//             <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: 40 }}>
//               <Spin indicator={<LoadingOutlined style={{ fontSize: 24 }} spin />} />
//             </div>
//           ) : (
//           <table style={{ width: '100%', borderCollapse: 'collapse' }}>
//             <thead>
//               <tr style={{ borderBottom: '1px solid rgba(0,0,0,0.1)' }}>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 70 }}>ID</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 250 }}>Câu hỏi</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>Trả lời</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 100 }}>Danh mục</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 80 }}>Hữu ích</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 84 }}>Trạng thái</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'right', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 120 }}>Thao tác</th>
//               </tr>
//             </thead>
//             <tbody>
//               {faqs.length === 0 ? (
//                 <tr>
//                   <td colSpan={7} style={{ padding: '40px', textAlign: 'center', color: '#6A7282', fontFamily: 'Arimo, sans-serif' }}>
//                     Chưa có câu hỏi nào
//                   </td>
//                 </tr>
//               ) : (
//               faqs.map((item, index) => {
//                 const categoryLabel = getFAQCategoryLabel(item.category);
//                 const statusLabel = getStatusLabel(item.status);
//                 return (
//                 <tr key={item.id} style={{ borderBottom: index < faqs.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none' }}>
//                   <td style={{ padding: '13px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>{item.faqCode || `FAQ${String(item.id).padStart(3, '0')}`}</td>
//                   <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.question}</td>
//                   <td style={{ padding: '13px 8px', fontSize: 14, color: '#4A5565', fontFamily: 'Arimo, sans-serif', maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.answer}</td>
//                   <td style={{ padding: '13px 8px' }}>
//                     <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: categoryStyles[categoryLabel]?.background || '#ECECF0', color: categoryStyles[categoryLabel]?.color || '#6A7282' }}>{categoryLabel}</span>
//                   </td>
//                   <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.helpfulCount || 0}</td>
//                   <td style={{ padding: '13px 8px' }}>
//                     <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: item.status === 'ACTIVE' ? '#DCFCE7' : '#FEF3C7', color: item.status === 'ACTIVE' ? '#008236' : '#B45309' }}>{statusLabel}</span>
//                   </td>
//                   <td style={{ padding: '13px 8px', textAlign: 'right' }}>
//                     <Space size={8}>
//                       <Button 
//                         type="text" 
//                         icon={<EditOutlined style={{ fontSize: 16, color: '#6B7280' }} />} 
//                         style={{ width: 36, height: 32, padding: 0 }} 
//                         onClick={() => handleEditFAQClick(item)}
//                         title="Chỉnh sửa"
//                       />
//                       <Button 
//                         type="text" 
//                         icon={<DeleteOutlined style={{ fontSize: 16, color: '#EF4444' }} />} 
//                         style={{ width: 36, height: 32, padding: 0 }} 
//                         onClick={() => handleDeleteFAQClick(item)}
//                         title="Xóa"
//                       />
//                     </Space>
//                   </td>
//                 </tr>
//                 );
//               })
//               )}
//             </tbody>
//           </table>
//           )}
//         </div>
//       </div>

//       {/* FAQ Add/Edit Modal */}
//       <FAQModal
//         open={showFAQModal}
//         onClose={handleCloseFAQModal}
//         onSubmit={handleFAQSubmit}
//         faq={editingFAQ}
//         loading={isSubmittingFAQ}
//       />

//       {/* FAQ Delete Confirmation Modal */}
//       <ConfirmModal
//         open={showConfirmDeleteFAQ}
//         onConfirm={handleConfirmDeleteFAQ}
//         onCancel={handleCancelDeleteFAQ}
//         title="Xác nhận xóa"
//         content={deletingFAQ ? `Bạn có chắc muốn xóa câu hỏi "${deletingFAQ.question}"?` : ''}
//         confirmText={isDeletingFAQ ? 'Đang xóa...' : 'Xóa'}
//         cancelText="Hủy"
//         danger={true}
//       />
//     </>
//   );
// };

// export default FAQsTab;
