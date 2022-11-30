package com.nike.springboottemplate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class ControllerTest {
    @Autowired
    private Controller controller;
    
    @Test
    void healthcheck() {
        ResponseEntity<Object> response = controller.healthcheck();
        assert response.equals(ResponseEntity.ok().build());
    }
}
