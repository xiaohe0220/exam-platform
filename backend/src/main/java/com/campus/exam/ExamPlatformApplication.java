package com.campus.exam;

import com.campus.exam.config.AgentProperties;
import com.campus.exam.config.AuthProperties;
import com.campus.exam.config.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CorsProperties.class, AgentProperties.class, AuthProperties.class})
public class ExamPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamPlatformApplication.class, args);
    }
}
