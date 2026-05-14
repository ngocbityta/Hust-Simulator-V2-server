package com.hustsimulator.context.grpc;

import com.hustsimulator.context.grpc.proto.ContextEngineServiceGrpc;
import com.hustsimulator.context.grpc.proto.ContextProto;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC service implementation for ContextEngineService.
 * Exposes spatial/temporal context operations to the game-server.
 */
@GrpcService
@RequiredArgsConstructor
public class ContextGrpcService extends ContextEngineServiceGrpc.ContextEngineServiceImplBase {

    private final IContextFacade contextFacade;

    @Override
    public void checkPlayerZone(ContextProto.ZoneCheckRequest request,
            StreamObserver<ContextProto.ZoneCheckResponse> responseObserver) {
        responseObserver.onNext(contextFacade.checkPlayerZone(request));
        responseObserver.onCompleted();
    }

    @Override
    public void reportSpatialTrigger(ContextProto.SpatialTriggerEvent request,
            StreamObserver<com.google.protobuf.Empty> responseObserver) {
        responseObserver.onNext(contextFacade.reportSpatialTrigger(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updatePlayerState(ContextProto.UpdatePlayerStateRequest request,
            StreamObserver<com.google.protobuf.Empty> responseObserver) {
        responseObserver.onNext(contextFacade.updatePlayerState(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getActiveEvents(ContextProto.ActiveEventsRequest request,
            StreamObserver<ContextProto.ActiveEventsResponse> responseObserver) {
        responseObserver.onNext(contextFacade.getActiveEvents(request));
        responseObserver.onCompleted();
    }

    @Override
    public void streamContextEvents(ContextProto.ActiveEventsRequest request,
            StreamObserver<ContextProto.ContextEvent> responseObserver) {
        // Still a stub as streaming requires more complex logic (e.g. state tracking)
        responseObserver.onCompleted();
    }

    @Override
    public void getHistoricalDensity(ContextProto.GetHistoricalDensityRequest request,
            StreamObserver<ContextProto.GetHistoricalDensityResponse> responseObserver) {
        responseObserver.onNext(contextFacade.getHistoricalDensity(request));
        responseObserver.onCompleted();
    }
}
