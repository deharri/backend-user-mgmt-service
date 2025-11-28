package com.deharri.ums.worker.mapper;

import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.entity.Worker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring"
)
public abstract class WorkerMapper {

    @Autowired
    protected WorkerMapperHelper workerMapperHelper;

    @Mapping(target = "coreUser", expression = "java(workerMapperHelper.getCoreUser())")
    @Mapping(target = "cnicVerification.cnic", source = "cnic")
    public abstract Worker createWorkerAccountDtoToWorker(CreateWorkerAccountDto createWorkerAccountDto);

}