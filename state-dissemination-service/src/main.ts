import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { ConfigService } from '@nestjs/config';
import { Logger } from '@nestjs/common';
import { WsAdapter } from '@nestjs/platform-ws';

async function bootstrap() {
  const logger = new Logger('Bootstrap');

  // Create HTTP app
  const app = await NestFactory.create(AppModule);
  const configService = app.get(ConfigService);

  // Use raw WebSocket adapter (ws library) instead of Socket.IO
  app.useWebSocketAdapter(new WsAdapter(app));

  // Enable CORS
  app.enableCors();

  // Connect gRPC microservice (exposes PlayerStateService)
  const grpcPort = configService.get<number>('GAME_SERVER_GRPC_PORT', 50051);
  app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.GRPC,
    options: {
      package: ['hustsimulator.player'],
      protoPath: [
        join(__dirname, '../../proto/player.proto'),
        join(__dirname, '../../proto/common.proto'),
      ],
      url: `0.0.0.0:${grpcPort}`,
    },
  });

  // Start all microservices
  await app.startAllMicroservices();
  logger.log(`gRPC server running on port ${grpcPort}`);

  // Start HTTP + WebSocket server
  const httpPort = configService.get<number>('GAME_SERVER_PORT', 3000);
  await app.listen(httpPort);
  logger.log(`HTTP server running on port ${httpPort}`);
  logger.log(`WebSocket server available on the same port`);
}

bootstrap();
