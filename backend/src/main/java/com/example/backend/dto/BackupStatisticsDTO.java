package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupStatisticsDTO {

    private long totalBackups;
    private long successfulBackups;
    private long failedBackups;
    private BackupHistoryDTO latestBackup;
    private String totalStorageUsed; // "125.5 MB"
    private long totalStorageBytes;
}
