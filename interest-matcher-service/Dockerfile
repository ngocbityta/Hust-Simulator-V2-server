FROM node:20-alpine AS builder
WORKDIR /app

# Copy package files and install deps
COPY interest-matcher-service/package.json ./
RUN npm install --frozen-lockfile

# Copy source and proto
COPY interest-matcher-service/ ./
COPY proto/ ../proto/

RUN npm run build

# ─── Production stage ───────────────────────────────────────────
FROM node:20-alpine
WORKDIR /app

COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/../proto ../proto

CMD ["node", "dist/main"]
