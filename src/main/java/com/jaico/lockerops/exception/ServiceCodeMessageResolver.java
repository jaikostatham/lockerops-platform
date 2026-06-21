package com.jaico.lockerops.exception;

import org.springframework.stereotype.Component;

@Component
public class ServiceCodeMessageResolver {

    private final ServiceCodeProperties serviceCodeProperties;

    public ServiceCodeMessageResolver(ServiceCodeProperties serviceCodeProperties) {
        this.serviceCodeProperties = serviceCodeProperties;
    }

    public String resolve(int code, Object... args) {
        String messageTemplate = serviceCodeProperties
                .getCode()
                .getOrDefault(code, "Error de servicio no catalogado: " + code);

        if (args == null || args.length == 0) {
            return messageTemplate;
        }

        return messageTemplate.formatted(args);
    }
}
