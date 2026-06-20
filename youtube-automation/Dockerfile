FROM node:20-alpine

# Set working directory for app assets
WORKDIR /app

# Install apk build tools required for native sqlite3 bindings compiles
RUN apk add --no-cache python3 make g++

# Copy package descriptors
COPY package*.json ./

# Clean reinstall dependencies
RUN npm ci --only=production

# Copy application files
COPY index.js ./

# SQLite persistence directory configuration volume
VOLUME ["/app/database"]

# Env setups
ENV NODE_ENV=production
ENV TARGET_NICHE="Tech Gossip"
ENV API_PROVIDER="Gemini"

# Launch main worker thread
CMD ["node", "index.js"]
