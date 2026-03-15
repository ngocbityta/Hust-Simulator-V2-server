import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { GrpcContextClient } from './context.client';

@Module({
    imports: [
        // gRPC client to Context Service
        ClientsModule.registerAsync([
            {
                name: 'CONTEXT_SERVICE',
                imports: [ConfigModule],
                inject: [ConfigService],
                useFactory: (configService: ConfigService) => ({
                    transport: Transport.GRPC,
                    options: {
                        package: ['hustsimulator.context'],
                        protoPath: [
                            join(__dirname, '../../../proto/context.proto'),
                            join(__dirname, '../../../proto/common.proto'),
                        ],
                        url: `${configService.get<string>('CONTEXT_SERVICE_HOST', 'localhost')}:${configService.get<number>('CONTEXT_SERVICE_GRPC_PORT', 50052)}`,
                    },
                }),
            },
        ]),
    ],
    providers: [GrpcContextClient],
    exports: [GrpcContextClient],
})
export class GrpcModule { }
