package com.oceandate.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sms.naver-cloud")
public class SmsProps {
    private String serviceId;
    private String accessKey;
    private String secretKey;
    private String fromPhoneNumber;
}
