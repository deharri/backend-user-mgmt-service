package com.deharri.ums.worker.dto.request;

import com.deharri.ums.worker.entity.Worker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateWorkerAccountDto {

    private Worker.WorkerType workerType;

    private List<String> skills;

    private String cnic;

    private String bio;

}
