package com.example.backend.controller;

import com.example.backend.dto.CreateUserRequestDTO;
import com.example.backend.dto.ProcessRequestDTO;
import com.example.backend.dto.UserRequestDTO;
import com.example.backend.model.UserRequest;
import com.example.backend.service.UserRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Requests", description = "API quản lý yêu cầu của người dùng (xuất dữ liệu, xóa tài khoản)")
public class UserRequestController {

    private final UserRequestService userRequestService;

    /**
     * Tạo yêu cầu mới (người dùng)
     */
    @PostMapping
    @Operation(summary = "Tạo yêu cầu mới", description = "Người dùng tạo yêu cầu xuất dữ liệu hoặc xóa tài khoản")
    public ResponseEntity<?> createRequest(
            @Valid @RequestBody CreateUserRequestDTO requestDTO,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            
            UserRequest.RequestType requestType = UserRequest.RequestType.valueOf(requestDTO.getRequestType());
            
            UserRequest request = userRequestService.createRequest(
                    username,
                    requestType,
                    requestDTO.getReason()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(UserRequestDTO.fromEntity(request));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Loại yêu cầu không hợp lệ: " + requestDTO.getRequestType());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating user request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách yêu cầu của người dùng hiện tại
     */
    @GetMapping("/my-requests")
    @Operation(summary = "Lấy yêu cầu của tôi", description = "Lấy tất cả yêu cầu của người dùng hiện tại")
    public ResponseEntity<?> getMyRequests(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<UserRequest> requests = userRequestService.getUserRequests(username);
            
            List<UserRequestDTO> dtos = requests.stream()
                    .map(UserRequestDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            log.error("Error getting user requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả yêu cầu (admin) với phân trang
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tất cả yêu cầu (Admin)", description = "Admin lấy tất cả yêu cầu với phân trang")
    public ResponseEntity<?> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<UserRequest> requestsPage = userRequestService.getAllRequests(pageable);
            
            Page<UserRequestDTO> dtoPage = requestsPage.map(UserRequestDTO::fromEntity);

            return ResponseEntity.ok(dtoPage);

        } catch (Exception e) {
            log.error("Error getting all requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy yêu cầu theo trạng thái (admin)
     */
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy yêu cầu theo trạng thái (Admin)", description = "Admin lấy yêu cầu theo trạng thái")
    public ResponseEntity<?> getRequestsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            UserRequest.RequestStatus requestStatus = UserRequest.RequestStatus.valueOf(status);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<UserRequest> requestsPage = userRequestService.getRequestsByStatus(requestStatus, pageable);
            
            Page<UserRequestDTO> dtoPage = requestsPage.map(UserRequestDTO::fromEntity);

            return ResponseEntity.ok(dtoPage);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Trạng thái không hợp lệ: " + status);
        } catch (Exception e) {
            log.error("Error getting requests by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy yêu cầu theo loại (admin)
     */
    @GetMapping("/admin/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy yêu cầu theo loại (Admin)", description = "Admin lấy yêu cầu theo loại")
    public ResponseEntity<?> getRequestsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            UserRequest.RequestType requestType = UserRequest.RequestType.valueOf(type);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<UserRequest> requestsPage = userRequestService.getRequestsByType(requestType, pageable);
            
            Page<UserRequestDTO> dtoPage = requestsPage.map(UserRequestDTO::fromEntity);

            return ResponseEntity.ok(dtoPage);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Loại yêu cầu không hợp lệ: " + type);
        } catch (Exception e) {
            log.error("Error getting requests by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Đếm số yêu cầu đang chờ duyệt (admin)
     */
    @GetMapping("/admin/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Đếm yêu cầu chờ duyệt (Admin)", description = "Đếm số yêu cầu đang chờ admin duyệt")
    public ResponseEntity<?> countPendingRequests() {
        try {
            long count = userRequestService.countPendingRequests();
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            log.error("Error counting pending requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đếm yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Duyệt yêu cầu (admin)
     */
    @PutMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt yêu cầu (Admin)", description = "Admin duyệt yêu cầu của người dùng")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long id,
            @RequestBody ProcessRequestDTO processDTO,
            Authentication authentication) {
        try {
            String adminUsername = authentication.getName();
            
            UserRequest request = userRequestService.approveRequest(
                    id,
                    adminUsername,
                    processDTO.getAdminNote()
            );

            return ResponseEntity.ok(UserRequestDTO.fromEntity(request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error approving request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi duyệt yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Từ chối yêu cầu (admin)
     */
    @PutMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Từ chối yêu cầu (Admin)", description = "Admin từ chối yêu cầu của người dùng")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestBody ProcessRequestDTO processDTO,
            Authentication authentication) {
        try {
            String adminUsername = authentication.getName();
            
            UserRequest request = userRequestService.rejectRequest(
                    id,
                    adminUsername,
                    processDTO.getAdminNote()
            );

            return ResponseEntity.ok(UserRequestDTO.fromEntity(request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error rejecting request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi từ chối yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê yêu cầu (admin)
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê yêu cầu (Admin)", description = "Lấy thống kê yêu cầu theo loại và trạng thái")
    public ResponseEntity<?> getRequestStatistics() {
        try {
            List<Object[]> stats = userRequestService.getRequestStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error getting request statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy thống kê: " + e.getMessage());
        }
    }
}
