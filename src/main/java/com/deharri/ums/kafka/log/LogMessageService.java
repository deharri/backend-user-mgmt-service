package com.deharri.ums.kafka.log;

import com.deharri.ums.kafka.log.dto.BaseLog;
import com.deharri.ums.kafka.log.dto.EndpointLog;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogMessageService {

    private final KafkaTemplate<String, BaseLog> kafkaTemplate;

    public void sendEndpointLogMessage(String endpoint, String method, LogLevel logLevel) {
        new Thread(() -> {
            EndpointLog endpointLog = new EndpointLog(endpoint, method, logLevel);
            try {
                kafkaTemplate.send("log-topic-endpoint", endpointLog);
            } catch (Exception e) {
                System.err.println("Kafka exception: " + e.getMessage());
            }
        }).start();
    }


}
