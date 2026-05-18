import { Controller, Logger } from '@nestjs/common';
import { GrpcMethod } from '@nestjs/microservices';
import { MatcherService } from './matcher.service';

interface PublishRequest {
  cellKey: string;
  payload: string;
  entityLongitude: number;
}

interface SubscribeRequest {
  dissNodeId: string;
  cellKeys: string[];
}

interface UnsubscribeRequest {
  dissNodeId: string;
  cellKeys: string[];
}

/**
 * MatcherController — gRPC handler for Interest Matcher SPS operations.
 *
 * Exposes the three core SPS operations defined in interest-matcher.proto:
 *   rpc Publish     → state-computation-service calls this
 *   rpc Subscribe   → state-dissemination-service calls this
 *   rpc Unsubscribe → state-dissemination-service calls this
 */
@Controller()
export class MatcherController {
  private readonly logger = new Logger(MatcherController.name);

  constructor(private readonly matcherService: MatcherService) {}

  @GrpcMethod('InterestMatcher', 'Publish')
  async publish(data: PublishRequest): Promise<{ success: boolean }> {
    try {
      await this.matcherService.publish(
        data.cellKey,
        data.payload,
        data.entityLongitude,
      );
      return { success: true };
    } catch (err) {
      this.logger.error(`Publish error for cell ${data.cellKey}`, err);
      return { success: false };
    }
  }

  @GrpcMethod('InterestMatcher', 'Subscribe')
  subscribe(data: SubscribeRequest): { success: boolean } {
    try {
      this.matcherService.subscribe(data.dissNodeId, data.cellKeys ?? []);
      return { success: true };
    } catch (err) {
      this.logger.error(`Subscribe error for node ${data.dissNodeId}`, err);
      return { success: false };
    }
  }

  @GrpcMethod('InterestMatcher', 'Unsubscribe')
  unsubscribe(data: UnsubscribeRequest): { success: boolean } {
    try {
      this.matcherService.unsubscribe(data.dissNodeId, data.cellKeys ?? []);
      return { success: true };
    } catch (err) {
      this.logger.error(`Unsubscribe error for node ${data.dissNodeId}`, err);
      return { success: false };
    }
  }
}
