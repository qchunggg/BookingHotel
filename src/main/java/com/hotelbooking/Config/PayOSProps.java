package com.hotelbooking.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payos")
@Getter
@Setter
public class PayOSProps {
    private String clientId;
    private String apiKey;
    private String checksumKey;

    private String returnUrl;
    private String cancelUrl;

    /** Base URL REST của payOS (có thể đổi sang sandbox khác nếu họ thay đổi) */
    private String baseUrl = "https://api-merchant.payos.vn";
}
