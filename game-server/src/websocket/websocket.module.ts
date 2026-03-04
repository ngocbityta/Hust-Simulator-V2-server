import { Module } from '@nestjs/common';
import { GameGateway } from './game.gateway';
import { PlayerModule } from '../player/player.module';

@Module({
    imports: [PlayerModule],
    providers: [GameGateway],
    exports: [GameGateway],
})
export class WebsocketModule { }
