package com.vhc.ec.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Đối tượng lưu trữ thông tin khởi tạo luồng dữ liệu trên hệ thống BPM
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@ToString
public class WorkflowDto implements Serializable {
    private final int contractId;
    private final int actionType;
    private final int approveType;
    private final int participantId;
    private final int recipientId;
}
