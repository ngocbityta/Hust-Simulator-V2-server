import { NestFactory } from '@nestjs/core';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { AppModule } from './app.module';
import { Logger } from '@nestjs/common';

async function bootstrap() {
  const logger = new Logger('InterestMatcher');

  const zoneId = parseInt(process.env.ZONE_ID ?? '0', 10);
  const basePort = parseInt(process.env.IM_BASE_PORT ?? '4000', 10);
  const grpcPort = basePort + zoneId;

  const app = await NestFactory.createMicroservice<MicroserviceOptions>(
    AppModule,
    {
      transport: Transport.GRPC,
      options: {
        package: 'hustsimulator.interest_matcher',
        protoPath: join(__dirname, '../../proto/interest-matcher.proto'),
        url: `0.0.0.0:${grpcPort}`,
      },
    },
  );

  await app.listen();
  logger.log(
    `Interest Matcher Zone ${zoneId} started — gRPC listening on port ${grpcPort}`,
  );
}

bootstrap().catch((err) => {
  console.error('Error during bootstrap:', err);
  process.exit(1);
});
