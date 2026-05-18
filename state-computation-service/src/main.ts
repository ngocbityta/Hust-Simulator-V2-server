import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { ConfigService } from '@nestjs/config';
import { Logger } from '@nestjs/common';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { WinstonModule } from 'nest-winston';
import * as winston from 'winston';

async function bootstrap() {
  // Create HTTP app with Winston Logger
  const app = await NestFactory.create(AppModule, {
    logger: WinstonModule.createLogger({
      transports: [
        new winston.transports.Console({
          level: process.env.LOG_LEVEL || 'info',
          format: winston.format.combine(
            winston.format.timestamp(),
            winston.format.json(),
          ),
        }),
      ],
    }),
  });
  const logger = new Logger('Bootstrap');
  const configService = app.get(ConfigService);

  // Swagger Documentation
  const config = new DocumentBuilder()
    .setTitle('HUST Simulator - State Computation Service')
    .setDescription('Calculates game states and updates Redis')
    .setVersion('1.0')
    .addTag('computation')
    .build();
  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api', app, document);

  // Connect gRPC microservice (exposes ComputationService and UserStateService)
  const grpcPort = configService.get<number>(
    'COMPUTATION_SERVICE_GRPC_PORT',
    50053,
  );
  app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.GRPC,
    options: {
      package: ['hustsimulator.computation', 'hustsimulator.player'],
      protoPath: [
        join(__dirname, '../../proto/computation.proto'),
        join(__dirname, '../../proto/player.proto'),
        join(__dirname, '../../proto/common.proto'),
      ],
      url: `0.0.0.0:${grpcPort}`,
    },
  });

  // Start all microservices
  await app.startAllMicroservices();
  logger.log(`gRPC server running on port ${grpcPort}`);

  // Start HTTP server
  const httpPort = configService.get<number>('COMPUTATION_SERVICE_PORT', 3003);
  await app.listen(httpPort);
  logger.log(`HTTP server running on port ${httpPort}`);
}

bootstrap().catch((err) => {
  console.error('Error during bootstrap:', err);
  process.exit(1);
});
