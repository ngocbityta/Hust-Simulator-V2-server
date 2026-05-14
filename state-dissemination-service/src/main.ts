import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ConfigService } from '@nestjs/config';
import { Logger } from '@nestjs/common';
import { WsAdapter } from '@nestjs/platform-ws';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { WinstonModule } from 'nest-winston';
import * as winston from 'winston';

async function bootstrap() {
  // Create HTTP app with Winston Logger
  const app = await NestFactory.create(AppModule, {
    logger: WinstonModule.createLogger({
      transports: [
        new winston.transports.Console({
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

  // Use raw WebSocket adapter (ws library) instead of Socket.IO
  app.useWebSocketAdapter(new WsAdapter(app));

  // Enable CORS
  app.enableCors();

  // Swagger Documentation
  const config = new DocumentBuilder()
    .setTitle('HUST Simulator - State Dissemination Service')
    .setDescription(
      `
<h2 style="color: #FF5733;">🎮 HUST Simulator - WebSocket Client Integration Guide</h2>

Tài liệu này cung cấp **Contract Data** chính thức dành cho Client (Unity/React/Godot) để kết nối vào game server.

---

### 🌐 1. Cấu hình kết nối (Connection Details)
- **Chuẩn kết nối:** WebSocket Mộc (Native WS / Socket.IO)
- **URL Môi trường Dev:** \`ws://localhost:3002/ws\`
- **Định dạng đóng gói:** \`UTF-8 JSON String\`
- **Cấu trúc chung của mọi Message:**
  \`\`\`json
  {
    "event": "TÊN_SỰ_KIỆN",
    "data": { /* Dữ liệu chi tiết */ }
  }
  \`\`\`

---

### 📤 2. Các sự kiện Client GỬI LÊN (Emit)

#### 🔑 2.1 Định danh Session (Bắt buộc đầu tiên)
- **Sự kiện:** \`user:join\`
- **Mô tả:** Hệ thống cần Client gửi JWT Token (lấy từ lúc Login) để xác thực và map Connection TCP với User.
- **Dữ liệu gửi lên (Payload):**
  \`\`\`json
  {
    "event": "user:join",
    "data": {
      "token": "string - Bắt buộc (JWT token của user)"
    }
  }
  \`\`\`
- **Server Phản hồi:** \`user:joined\` (ACK).

#### 🏃 2.2 Đồng bộ Tọa độ Di chuyển (Phát liên tục)
- **Sự kiện:** \`user:move\`
- **Mô tả:** Được gọi liên tục (VD 10 lần/giây) khi Client duy chuyển. Sẽ Broadcast ra 50m xung quanh.
- **Dữ liệu gửi lên (Payload):**
  \`\`\`json
  {
    "event": "user:move",
    "data": {
      "position": { 
        "latitude": 21.00305,  // float
        "longitude": 105.84305 // float
      },
      "speed": 5.0,     // float - Vận tốc
      "heading": 45.5,  // float - Hướng xoay (Độ)
      "clientTimestamp": 1711223344000 // int (Optional)
    }
  }
  \`\`\`

#### ⚔️ 2.3 Đồng bộ Trạng thái Hoạt động (Bền vững)
- **Sự kiện:** \`user:state_change\`
- **Mô tả:** Được gọi khi Client CHUYỂN giữa các animation (từ Đứng yên -> Chạy, Đang đi -> Nhảy).
- **Dữ liệu gửi lên (Payload):**
  \`\`\`json
  {
    "event": "user:state_change",
    "data": {
      "activityState": "IDLE | WALKING | RUNNING | IN_BATTLE | DOING_TASK",
      "mapId": "string (Optional)",
      "position": { "latitude": 21.0, "longitude": 105.8 }
    }
  }
  \`\`\`

---

### 📥 3. Các sự kiện Server TRẢ VỀ (Listen/On)

#### 🌍 3.1 Cập nhật Tọa độ/Trạng thái của Người Khác (AOI)
- **Sự kiện Lắng nghe:** \`user:state_update\`
- **Mô tả:** Gateway sẽ gởi bản tin này khi có 1 người chơi trong tầm bán kính 50m xung quanh bạn di chuyển hoặc đổi State.
- **Dữ liệu nhận được:**
  \`\`\`json
  { 
    "event": "user:state_update", 
    "data": { 
      "userId": "player-456", 
      "type": "move",  // Thay đổi dựa vào 'move' hoặc 'state_change'
      "payload": {
          "position": { "latitude": 21.0, "longitude": 105.8 },
          "speed": 5.0,
          "heading": 45.5
      }, 
      "timestamp": 123456789 
    } 
  }
  \`\`\`
- **Frontend Action:** Lập trình viên Client khi nhận được bản dữ liệu này cần dò tìm đúng ID nhân vật trong Scene, và chạy script \`Lerp(currentTransform, payload.position)\` để nhân vật trượt trên màn hình cực kỳ mượt mà.

---

### 🔥 4. Heatmap - Bản đồ Nhiệt Mật độ Người dùng

#### 📤 4.1 Đăng ký nhận Heatmap
- **Sự kiện:** \`heatmap:subscribe\`
- **Mô tả:** Client gửi để bắt đầu nhận dữ liệu heatmap mỗi 5 giây.
- **Dữ liệu gửi lên:**
  \`\`\`json
  { "event": "heatmap:subscribe", "data": {} }
  \`\`\`
- **Server phản hồi:** \`heatmap:subscribed\`

#### 📤 4.2 Hủy đăng ký Heatmap
- **Sự kiện:** \`heatmap:unsubscribe\`
- **Dữ liệu gửi lên:**
  \`\`\`json
  { "event": "heatmap:unsubscribe", "data": {} }
  \`\`\`

#### 📥 4.3 Nhận dữ liệu Heatmap (Mỗi ~5 giây)
- **Sự kiện Lắng nghe:** \`heatmap:update\`
- **Dữ liệu nhận được:**
  \`\`\`json
  {
    "event": "heatmap:update",
    "data": {
      "timestamp": 1715400000000,
      "totalOnline": 42,
      "cells": [
        {
          "cellX": 244180,
          "cellY": 46623,
          "count": 5,
          "centerLat": 21.00315,
          "centerLng": 105.84275,
          "activities": { "ROAMING": 3, "IN_BUILDING": 1, "IN_EVENT": 1 }
        }
      ]
    }
  }
  \`\`\`
- **Frontend Action:** Render mỗi cell dưới dạng hình tròn/hình vuông bán kính 50m tại vị trí (centerLat, centerLng). Màu sắc/opacity tỉ lệ với count.

---
`,
    )
    .setVersion('1.0')
    .addTag('dissemination')
    .build();
  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api', app, document);

  // Start HTTP + WebSocket server
  const httpPort = configService.get<number>('GAME_SERVER_PORT', 3000);
  await app.listen(httpPort);
  logger.log(`HTTP server running on port ${httpPort}`);
  logger.log(`WebSocket server available on the same port`);
}

bootstrap().catch((err) => {
  console.error('Error during bootstrap:', err);
  process.exit(1);
});
