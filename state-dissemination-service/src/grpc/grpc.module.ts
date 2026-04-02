import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { GrpcComputationClient } from './computation.client';

@Module({
    imports: [
        ClientsModule.registerAsync([
            {
                name: 'COMPUTATION_SERVICE',
                imports: [ConfigModule],
                inject: [ConfigService],
                useFactory: (configService: ConfigService) => ({
                    transport: Transport.GRPC,
                    options: {
                        package: ['hustsimulator.computation'],
                        protoPath: [
                            join(__dirname, '../../../proto/computation.proto'),
                            join(__dirname, '../../../proto/common.proto'),
                        ],
                        url: `${configService.get<string>('COMPUTATION_SERVICE_HOST', 'localhost')}:${configService.get<number>('COMPUTATION_SERVICE_GRPC_PORT', 50053)}`,
                    },
                }),
            },
        ]),
    ],
    providers: [GrpcComputationClient],
    exports: [GrpcComputationClient],
})
export class GrpcModule { }
