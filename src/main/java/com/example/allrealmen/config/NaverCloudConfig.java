package com.example.allrealmen.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "naver-cloud.sms")
public class NaverCloudConfig {
    private String accessKey;
    private String secretKey;
    private String serviceId;
    private String senderPhone;
} 