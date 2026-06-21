package com.jaico.lockerops.shared.exception;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "service")
public class ServiceCodeProperties {

    private Map<Integer, String> code = new HashMap<>();

    public Map<Integer, String> getCode() {
        return code;
    }

    public void setCode(Map<Integer, String> code) {
        this.code = code;
    }
}
