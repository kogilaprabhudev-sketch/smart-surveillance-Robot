package com.surveillance.robot.controller;

import com.surveillance.robot.dto.AlertResponse;
import com.surveillance.robot.dto.PageResponse;
import com.surveillance.robot.service.AlertService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /** Optional ?acknowledged=true|false filter. */
    @GetMapping
    public PageResponse<AlertResponse> list(
            @RequestParam(required = false) Boolean acknowledged,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return alertService.list(acknowledged, pageable);
    }

    @PostMapping("/{id}/acknowledge")
    public AlertResponse acknowledge(@PathVariable Long id, Authentication auth) {
        return alertService.acknowledge(id, auth.getName());
    }
}
