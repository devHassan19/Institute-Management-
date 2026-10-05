package com.example.institute.institute.service;

import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.model.AuditLog;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.AuditLogRepository;
import com.example.institute.institute.security.MyUserDetails;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AuditLogService {

    private AuditLogRepository auditLogRepository;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }

    public AuditLog createLog(Long userId, String action, String entityName,
                              Long entityId, String description) {
        System.out.println("Service Calling createLog ==> ");
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        User currentUser = getCurrentLogginUser();
        String role = currentUser.getRole();
        System.out.println("Service Calling listLogs ==> ");
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can Display The Logs");
        }
        return auditLogRepository.findAll();
    }
}
