package com.example.institute.institute.controller;

import com.example.institute.institute.model.AuditLog;
import com.example.institute.institute.repository.AuditLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class AuditLogController {

    private AuditLogRepository auditLogRepository;

    @GetMapping("/auditLog")
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}