# Stage 1: Build frontend
FROM node:20-alpine as build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build arguments cho environment variables
ARG VITE_API_BASE_URL
ARG VITE_AI_API_URL
ARG VITE_APP_NAME=FinPal Admin
ARG VITE_APP_VERSION=1.0.0

# Set environment variables cho build
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_AI_API_URL=$VITE_AI_API_URL
ENV VITE_APP_NAME=$VITE_APP_NAME
ENV VITE_APP_VERSION=$VITE_APP_VERSION

# Build production
RUN npm run build

# Stage 2: Serve với Nginx
FROM nginx:alpine

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built files từ stage 1
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
