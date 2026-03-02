package com.hustsimulator.context.grpc;

import com.hustsimulator.context.grpc.proto.CommonProto;
import com.hustsimulator.context.grpc.proto.ContextEngineServiceGrpc;
import com.hustsimulator.context.grpc.proto.ContextProto;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC service implementation for ContextEngineService.
 * Exposes spatial/temporal context operations to the game-server.
 */
@GrpcService
public class ContextGrpcService extends ContextEngineServiceGrpc.ContextEngineServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ContextGrpcService.class);

    @Override
    public void checkPlayerZone(ContextProto.ZoneCheckRequest request,
                                 StreamObserver<ContextProto.ZoneCheckResponse> responseObserver) {
        log.info("checkPlayerZone called for player: {}", request.getPlayerId());

        // TODO: Implement spatial zone check using PostGIS
        ContextProto.ZoneCheckResponse response = ContextProto.ZoneCheckResponse.newBuilder()
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void reportSpatialTrigger(ContextProto.SpatialTriggerEvent request,
                                      StreamObserver<CommonProto.StatusResponse> responseObserver) {
        log.info("reportSpatialTrigger: player={} zone={} type={}",
                request.getPlayerId(), request.getZoneId(), request.getTriggerType());

        // TODO: Process spatial trigger event
        CommonProto.StatusResponse response = CommonProto.StatusResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Spatial trigger processed")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getActiveEvents(ContextProto.ActiveEventsRequest request,
                                 StreamObserver<ContextProto.ActiveEventsResponse> responseObserver) {
        log.info("getActiveEvents called for player: {}", request.getPlayerId());

        // TODO: Query active events for the player
        ContextProto.ActiveEventsResponse response = ContextProto.ActiveEventsResponse.newBuilder()
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void streamContextEvents(ContextProto.ActiveEventsRequest request,
                                     StreamObserver<ContextProto.ContextEvent> responseObserver) {
        log.info("streamContextEvents started for player: {}", request.getPlayerId());

        // TODO: Implement server-side streaming of context events
        responseObserver.onCompleted();
    }
}
