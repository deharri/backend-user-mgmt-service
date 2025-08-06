package com.deharri.ums.kafka.log.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.logging.LogLevel;

import java.time.Instant;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class EndpointLog extends BaseLog{

    private String endpoint;

    private String method;

    public EndpointLog(String endpoint, String method, LogLevel logLevel) {
        this.endpoint = endpoint;
        this.method = method;
        this.logLevel = logLevel;
        this.timestamp = Instant.now();
    }

}
