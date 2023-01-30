package com.nike.ncp.scheduler.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "scheduler executor api", tags = "scheduler executor")
@RestController
public class HeathCheckController {

    @ApiOperation("scheduler executor health check")
    @GetMapping("/healthcheck")
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
