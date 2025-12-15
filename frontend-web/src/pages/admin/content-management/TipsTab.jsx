// import React, { useState, useEffect } from 'react';
// import { Button, Space, Spin, message } from 'antd';
// import {
//   PlusOutlined,
//   EditOutlined,
//   DeleteOutlined,
//   LoadingOutlined,
// } from '@ant-design/icons';
// import ConfirmModal from '../../../components/common/ConfirmModal';
// import TipModal from '../../../components/admin/TipModal';
// import {
//   getAllTips,
//   getTipCategoryLabel,
//   getStatusLabel,
//   createTip,
//   updateTip,
//   deleteTip,
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

// const TipsTab = () => {
//   // State for data
//   const [tips, setTips] = useState([]);
  
//   // Loading states
//   const [loadingTips, setLoadingTips] = useState(false);

//   // Tip modal states (thêm/sửa)
//   const [showTipModal, setShowTipModal] = useState(false);
//   const [editingTip, setEditingTip] = useState(null);
//   const [isSubmittingTip, setIsSubmittingTip] = useState(false);

//   // Delete tip states
//   const [deletingTip, setDeletingTip] = useState(null);
//   const [showConfirmDeleteTip, setShowConfirmDeleteTip] = useState(false);
//   const [isDeletingTip, setIsDeletingTip] = useState(false);

//   // Fetch tips
//   const fetchTips = async () => {
//     setLoadingTips(true);
//     try {
//       const data = await getAllTips();
//       setTips(data);
//     } catch (error) {
//       console.error('Error fetching tips:', error);
//       message.error('Không thể tải mẹo và gợi ý');
//     } finally {
//       setLoadingTips(false);
//     }
//   };

//   // Fetch data on mount
//   useEffect(() => {
//     fetchTips();
//   }, []);

//   // Handle Add Tip
//   const handleAddTipClick = () => {
//     setEditingTip(null);
//     setShowTipModal(true);
//   };

//   // Handle Edit Tip
//   const handleEditTipClick = (tip) => {
//     setEditingTip(tip);
//     setShowTipModal(true);
//   };

//   // Handle Submit Tip (Add/Edit)
//   const handleTipSubmit = async (data) => {
//     setIsSubmittingTip(true);
//     try {
//       if (editingTip) {
//         await updateTip(editingTip.id, data);
//         message.success('Cập nhật tip thành công!');
//       } else {
//         // Tìm số lớn nhất từ tipCode hiện có (TIP001, TIP002,...)
//         let maxNumber = 0;
//         tips.forEach((t) => {
//           const code = t.tipCode || '';
//           const match = code.match(/^TIP(\d+)$/);
//           if (match) {
//             const num = parseInt(match[1], 10);
//             if (num > maxNumber) maxNumber = num;
//           }
//         });
//         const nextNumber = maxNumber + 1;
//         const tipData = {
//           ...data,
//           tipCode: `TIP${String(nextNumber).padStart(3, '0')}`,
//         };
//         await createTip(tipData);
//         message.success('Thêm tip thành công!');
//       }
//       setShowTipModal(false);
//       setEditingTip(null);
//       fetchTips();
//     } catch (error) {
//       console.error('Error submitting tip:', error);
//       message.error(
//         error.response?.data?.message || 
//         (editingTip ? 'Không thể cập nhật tip' : 'Không thể thêm tip')
//       );
//     } finally {
//       setIsSubmittingTip(false);
//     }
//   };

//   // Handle Close Tip Modal
//   const handleCloseTipModal = () => {
//     setShowTipModal(false);
//     setEditingTip(null);
//   };

//   // Handle Delete Tip Click
//   const handleDeleteTipClick = (tip) => {
//     setDeletingTip(tip);
//     setShowConfirmDeleteTip(true);
//   };

//   // Handle Confirm Delete Tip
//   const handleConfirmDeleteTip = async () => {
//     if (!deletingTip) return;
    
//     setIsDeletingTip(true);
//     try {
//       await deleteTip(deletingTip.id);
//       message.success('Xóa tip thành công!');
//       setShowConfirmDeleteTip(false);
//       setDeletingTip(null);
//       fetchTips();
//     } catch (error) {
//       console.error('Error deleting tip:', error);
//       message.error(error.response?.data?.message || 'Không thể xóa tip');
//     } finally {
//       setIsDeletingTip(false);
//     }
//   };

//   // Handle Cancel Delete Tip
//   const handleCancelDeleteTip = () => {
//     setShowConfirmDeleteTip(false);
//     setDeletingTip(null);
//   };

//   return (
//     <>
//       <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
//         <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
//           <div>
//             <h3 style={{ fontSize: 16, fontWeight: 400, color: '#101828', margin: 0, fontFamily: 'Arimo, sans-serif' }}>Mẹo và gợi ý</h3>
//             <p style={{ fontSize: 14, color: '#6A7282', margin: 0, fontFamily: 'Arimo, sans-serif' }}>Quản lý các tips tiết kiệm và gợi ý tài chính</p>
//           </div>
//           <Button type="primary" icon={<PlusOutlined />} onClick={handleAddTipClick} style={{ background: '#155DFC', borderRadius: 8, height: 36, fontFamily: 'Arimo, sans-serif', fontSize: 14 }}>Thêm mẹo</Button>
//         </div>

//         <div style={{ background: '#FFFFFF', border: '1px solid rgba(0,0,0,0.1)', borderRadius: 14, overflow: 'hidden' }}>
//           {loadingTips ? (
//             <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: 40 }}>
//               <Spin indicator={<LoadingOutlined style={{ fontSize: 24 }} spin />} />
//             </div>
//           ) : (
//           <table style={{ width: '100%', borderCollapse: 'collapse' }}>
//             <thead>
//               <tr style={{ borderBottom: '1px solid rgba(0,0,0,0.1)' }}>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 70 }}>ID</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 180 }}>Tiêu đề</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>Nội dung</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 100 }}>Danh mục</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 80 }}>Lượt xem</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 84 }}>Trạng thái</th>
//                 <th style={{ padding: '10px 8px', textAlign: 'right', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 120 }}>Thao tác</th>
//               </tr>
//             </thead>
//             <tbody>
//               {tips.length === 0 ? (
//                 <tr>
//                   <td colSpan={7} style={{ padding: '40px', textAlign: 'center', color: '#6A7282', fontFamily: 'Arimo, sans-serif' }}>
//                     Chưa có mẹo nào
//                   </td>
//                 </tr>
//               ) : (
//               tips.map((item, index) => {
//                 const categoryLabel = getTipCategoryLabel(item.category);
//                 const statusLabel = getStatusLabel(item.status);
//                 return (
//                 <tr key={item.id} style={{ borderBottom: index < tips.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none' }}>
//                   <td style={{ padding: '13px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>{item.tipCode || `TIP${String(item.id).padStart(3, '0')}`}</td>
//                   <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.title}</td>
//                   <td style={{ padding: '13px 8px', fontSize: 14, color: '#4A5565', fontFamily: 'Arimo, sans-serif' }}>{item.content}</td>
//                   <td style={{ padding: '13px 8px' }}>
//                     <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: categoryStyles[categoryLabel]?.background || '#ECECF0', color: categoryStyles[categoryLabel]?.color || '#6A7282' }}>{categoryLabel}</span>
//                   </td>
//                   <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.viewCount || 0}</td>
//                   <td style={{ padding: '13px 8px' }}>
//                     <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: item.status === 'ACTIVE' ? '#DCFCE7' : '#FEF3C7', color: item.status === 'ACTIVE' ? '#008236' : '#B45309' }}>{statusLabel}</span>
//                   </td>
//                   <td style={{ padding: '13px 8px', textAlign: 'right' }}>
//                     <Space size={8}>
//                       <Button 
//                         type="text" 
//                         icon={<EditOutlined style={{ fontSize: 16, color: '#6B7280' }} />} 
//                         style={{ width: 36, height: 32, padding: 0 }} 
//                         onClick={() => handleEditTipClick(item)}
//                         title="Chỉnh sửa"
//                       />
//                       <Button 
//                         type="text" 
//                         icon={<DeleteOutlined style={{ fontSize: 16, color: '#EF4444' }} />} 
//                         style={{ width: 36, height: 32, padding: 0 }} 
//                         onClick={() => handleDeleteTipClick(item)}
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

//       {/* Tip Add/Edit Modal */}
//       <TipModal
//         open={showTipModal}
//         onClose={handleCloseTipModal}
//         onSubmit={handleTipSubmit}
//         tip={editingTip}
//         loading={isSubmittingTip}
//       />

//       {/* Tip Delete Confirmation Modal */}
//       <ConfirmModal
//         open={showConfirmDeleteTip}
//         onConfirm={handleConfirmDeleteTip}
//         onCancel={handleCancelDeleteTip}
//         title="Xác nhận xóa"
//         content={deletingTip ? `Bạn có chắc muốn xóa mẹo "${deletingTip.title}"?` : ''}
//         confirmText={isDeletingTip ? 'Đang xóa...' : 'Xóa'}
//         cancelText="Hủy"
//         danger={true}
//       />
//     </>
//   );
// };

// export default TipsTab;
