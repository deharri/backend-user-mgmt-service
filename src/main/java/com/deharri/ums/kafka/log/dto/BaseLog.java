package com.deharri.ums.kafka.log.dto;

import lombok.Data;
import org.springframework.boot.logging.LogLevel;
import org.springframework.data.annotation.CreatedBy;

import java.time.Instant;
@Data
public class BaseLog {

    protected Instant timestamp;

    @CreatedBy
    protected String user;

    protected LogLevel logLevel;

    protected final String serviceName = "User Management Service";

}
