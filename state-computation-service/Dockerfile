# Build stage
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY state-computation-service/package*.json ./state-computation-service/

# Install dependencies cleanly
RUN cd state-computation-service && npm ci

# Copy source and proto
COPY state-computation-service ./state-computation-service/
COPY proto ./proto/

# Build
RUN cd state-computation-service && npm run build

# Production stage
FROM node:20-alpine

WORKDIR /app

# Install production dependencies only
COPY state-computation-service/package*.json ./state-computation-service/
RUN cd state-computation-service && npm ci --omit=dev

# Copy built assets
COPY --from=build /app/state-computation-service/dist ./state-computation-service/dist
COPY --from=build /app/proto ./proto

WORKDIR /app/state-computation-service

ENV NODE_ENV=production

EXPOSE 3003
EXPOSE 50053

CMD ["sh", "-c", "node ${NODE_OPTIONS} dist/main"]
