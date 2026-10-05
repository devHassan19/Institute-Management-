package com.example.institute.institute.service;

import com.example.institute.institute.model.AuditLog;
import com.example.institute.institute.repository.AuditLogRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuditLogService {

    private AuditLogRepository auditLogRepository;
    public AuditLog createLog(Long userId, String action, String entityName,
                              Long entityId, String description) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());
        return auditLogRepository.save(log);
    }
}
