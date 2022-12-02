package com.nike.springboottemplate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/springboot_demo")
public class Controller {
    @GetMapping("/healthcheck")
    public ResponseEntity<Object> healthcheck() {
        return ResponseEntity.ok().build();
    }
}
