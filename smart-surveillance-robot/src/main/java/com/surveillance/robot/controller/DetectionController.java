package com.surveillance.robot.controller;

import com.surveillance.robot.dto.DetectionResponse;
import com.surveillance.robot.dto.PageResponse;
import com.surveillance.robot.service.DetectionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/detections")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    /** All detections, newest first; optional ?robotCode= filter. */
    @GetMapping
    public PageResponse<DetectionResponse> list(
            @RequestParam(required = false) String robotCode,
            @PageableDefault(size = 20, sort = "detectedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return detectionService.list(robotCode, pageable);
    }
}
