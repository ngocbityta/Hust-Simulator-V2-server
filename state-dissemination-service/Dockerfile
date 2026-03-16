# Build stage
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY state-dissemination-service/package*.json ./state-dissemination-service/

# Install dependencies
RUN cd state-dissemination-service && npm install

# Copy source and proto
COPY state-dissemination-service ./state-dissemination-service/
COPY proto ./proto/

# Build
RUN cd state-dissemination-service && npm run build

# Production stage
FROM node:20-alpine

WORKDIR /app

# Copy built assets and production deps
COPY --from=build /app/state-dissemination-service/package*.json ./state-dissemination-service/
COPY --from=build /app/state-dissemination-service/node_modules ./state-dissemination-service/node_modules
COPY --from=build /app/state-dissemination-service/dist ./state-dissemination-service/dist
COPY --from=build /app/proto ./proto

WORKDIR /app/state-dissemination-service

ENV NODE_ENV=production

EXPOSE 3002
EXPOSE 50051

CMD ["node", "dist/main"]
