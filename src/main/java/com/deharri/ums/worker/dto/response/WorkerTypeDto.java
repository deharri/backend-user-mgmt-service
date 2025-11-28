package com.deharri.ums.worker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkerTypeDto {

    private String enumValue;

    private String displayName;

    private String description;

}
