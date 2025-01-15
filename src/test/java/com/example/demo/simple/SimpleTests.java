package com.example.demo.simple;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SimpleTests {

    @Test
    void contextLoads() {
        // 컨텍스트가 정상적으로 로드되는지 확인
        assertThat(true).isTrue();
    }
}
