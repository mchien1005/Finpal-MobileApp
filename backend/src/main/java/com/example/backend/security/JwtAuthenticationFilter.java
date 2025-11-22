package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Filter chính để parse và validate JWT token từ header `Authorization`.
     *
     * Luồng hoạt động:
     * 1. Lấy header `Authorization` và kiểm tra prefix `Bearer `
     * 2. Nếu không có header hoặc không đúng format -> tiếp tục filter chain (không
     * authenticate)
     * 3. Nếu có token -> extract username từ token bằng `JwtService`
     * 4. Tải `UserDetails` từ DB (sử dụng `UserDetailsService`)
     * 5. Kiểm tra token hợp lệ với `jwtService.isTokenValid`
     * 6. Nếu hợp lệ -> tạo `UsernamePasswordAuthenticationToken` và đặt vào
     * `SecurityContext`
     *
     * Ghi chú:
     * - Filter này chạy 1 lần cho mỗi request (extends OncePerRequestFilter)
     * - Nếu có bất kỳ lỗi nào trong quá trình validate token, filter sẽ log lỗi
     * nhưng vẫn tiếp tục chain
     */
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Kiểm tra header Authorization có tồn tại và bắt đầu bằng "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Không có token -> bỏ qua authentication ở đây
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy token (bỏ phần "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // Lấy username từ token
            username = jwtService.extractUsername(jwt);

            // Nếu username tồn tại và chưa có authentication trong context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Tải thông tin user (roles, authorities)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate token so với user details
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Tạo authentication token và gán vào SecurityContext để Spring Security nhận
                    // diện user
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Ghi log lỗi để debug (không ném exception để không block request flow)
            logger.error("Error processing JWT token: " + e.getMessage());
        }

        // Tiếp tục filter chain
        filterChain.doFilter(request, response);
    }
}
