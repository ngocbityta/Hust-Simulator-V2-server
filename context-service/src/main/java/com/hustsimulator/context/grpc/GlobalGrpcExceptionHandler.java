package com.hustsimulator.context.grpc;

import io.grpc.Status;
import io.grpc.StatusException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
@Slf4j
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusException handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid argument for gRPC call: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusException handleGenericException(Exception e) {
        log.error("Unhandled exception in gRPC interceptor: {}", e.getMessage(), e);
        return Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asException();
    }
}
