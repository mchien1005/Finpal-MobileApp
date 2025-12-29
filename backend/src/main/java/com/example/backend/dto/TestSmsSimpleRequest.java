package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request test SMS đơn giản
 * Chỉ cần nhập nội dung SMS, hệ thống tự động tìm parser phù hợp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để test SMS đơn giản")
public class TestSmsSimpleRequest {

    @Schema(
        description = "Nội dung tin nhắn SMS từ ngân hàng",
        example = "TK215xxx5259 tai BIDV +200,000VND vao 22:39 22/03/2023. So du:200,000VND. ND: TKThe :106869505742, tai Vietinbank. ngoc-CTLNHIDI000004146381974-11-CRE-002",
        required = true
    )
    private String smsContent;
}
