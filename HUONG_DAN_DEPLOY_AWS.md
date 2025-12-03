# 🚀 Hướng Dẫn Deploy Backend Finpal Lên AWS EC2

## 📋 Mục Lục

1. [Chuẩn Bị AWS EC2](#1-chuẩn-bị-aws-ec2)
2. [Cài Đặt Docker Trên EC2](#2-cài-đặt-docker-trên-ec2)
3. [Upload Code Lên Server](#3-upload-code-lên-server)
4. [Cấu Hình & Deploy](#4-cấu-hình--deploy)
5. [Cấu Hình Domain & SSL](#5-cấu-hình-domain--ssl)
6. [Monitoring & Bảo Trì](#6-monitoring--bảo-trì)

---

## 1. Chuẩn Bị AWS EC2

### Bước 1.1: Tạo EC2 Instance

1. **Đăng nhập AWS Console** → EC2 Dashboard
2. **Launch Instance** với cấu hình:

   - **Name:** `finpal-backend`
   - **AMI:** Ubuntu Server 22.04 LTS
   - **Instance Type:**
     - Tối thiểu: `t2.medium` (2 vCPU, 4GB RAM)
     - Khuyến nghị: `t2.large` (2 vCPU, 8GB RAM)
   - **Storage:** 20GB gp3 (hoặc lớn hơn)

3. **Key Pair:**

   - Create new key pair: `finpal-key.pem`
   - Download và lưu an toàn

4. **Network Settings:**
   - ✅ Allow SSH (port 22)
   - ✅ Allow HTTP (port 80)
   - ✅ Allow HTTPS (port 443)
   - ✅ Custom TCP (port 8080) - Backend API
   - ✅ Custom TCP (port 8000) - AI API
   - ✅ Custom TCP (port 8081) - phpMyAdmin (tùy chọn, chỉ từ IP của bạn)

### Bước 1.2: Cấu Hình Security Group

Sau khi tạo instance, vào **Security Groups** và mở các port:

| Port | Protocol | Source    | Description                    |
| ---- | -------- | --------- | ------------------------------ |
| 22   | TCP      | My IP     | SSH                            |
| 80   | TCP      | 0.0.0.0/0 | HTTP                           |
| 443  | TCP      | 0.0.0.0/0 | HTTPS                          |
| 8080 | TCP      | 0.0.0.0/0 | Backend Java API               |
| 8000 | TCP      | 0.0.0.0/0 | Backend AI API                 |
| 8081 | TCP      | My IP     | phpMyAdmin (chỉ từ IP của bạn) |
| 3306 | TCP      | localhost | MySQL (chỉ internal)           |

### Bước 1.3: Elastic IP (Khuyến nghị)

1. **Allocate Elastic IP** (IP tĩnh)
2. **Associate** với EC2 instance
3. Lưu lại IP này để cấu hình domain

---

## 2. Cài Đặt Docker Trên EC2

### Bước 2.1: SSH Vào Server

**Windows (PowerShell):**

```powershell
# Set permission cho key file
icacls "finpal-key.pem" /inheritance:r
icacls "finpal-key.pem" /grant:r "$($env:USERNAME):(R)"

# SSH vào server
ssh -i finpal-key.pem ubuntu@<ELASTIC_IP>
```

**Linux/Mac:**

```bash
chmod 400 finpal-key.pem
ssh -i finpal-key.pem ubuntu@<ELASTIC_IP>
```

### Bước 2.2: Update System

```bash
sudo apt update
sudo apt upgrade -y
```

### Bước 2.3: Cài Docker

```bash
# Remove old versions
sudo apt remove docker docker-engine docker.io containerd runc -y

# Install dependencies
sudo apt install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker ubuntu
newgrp docker

# Verify installation
docker --version
docker compose version
```

### Bước 2.4: Cài Git

```bash
sudo apt install -y git
git --version
```

---

## 3. Upload Code Lên Server

### Cách 1: Sử Dụng Git (Khuyến nghị)

```bash
# Tạo thư mục project
cd ~
mkdir -p projects
cd projects

# Clone repository
git clone <YOUR_GITHUB_REPO_URL> Finpal-MobileApp
cd Finpal-MobileApp
```

**Lưu ý:** Nếu repo private, cần setup GitHub SSH key hoặc Personal Access Token.

### Cách 2: Upload Qua SCP

**Từ máy local (Windows PowerShell):**

```powershell
# Nén project (loại trừ node_modules, target, .git)
Compress-Archive -Path D:\Finpal-MobileApp\* -DestinationPath finpal-backend.zip

# Upload lên server
scp -i finpal-key.pem finpal-backend.zip ubuntu@<ELASTIC_IP>:~/
```

**Trên server:**

```bash
# Giải nén
sudo apt install -y unzip
mkdir -p ~/projects
unzip finpal-backend.zip -d ~/projects/Finpal-MobileApp
cd ~/projects/Finpal-MobileApp
```

### Cách 3: Sử Dụng GitHub Actions (CI/CD) - Nâng Cao

Tạo file `.github/workflows/deploy.yml` trong repo để tự động deploy khi push code.

---

## 4. Cấu Hình & Deploy

### Bước 4.1: Tạo File Environment (Production)

```bash
cd ~/projects/Finpal-MobileApp

# Tạo file .env cho production
nano .env.production
```

**Nội dung `.env.production`:**

```env
# Database
MYSQL_ROOT_PASSWORD=<STRONG_PASSWORD>
MYSQL_DATABASE=finpal_db
MYSQL_PORT=3306

# Backend Java
BACKEND_PORT=8080
JWT_SECRET=<STRONG_JWT_SECRET_64_CHARS>
JWT_EXPIRATION=86400000

# Backend AI
AI_BACKEND_PORT=8000
AI_BACKEND_URL=http://backend-ai:8000

# phpMyAdmin
PMA_PORT=8081
PMA_HOST=mysql

# Server
SERVER_IP=<YOUR_ELASTIC_IP>
DOMAIN=<YOUR_DOMAIN>  # Nếu có
```

**Generate JWT Secret mới:**

```bash
openssl rand -hex 32
```

### Bước 4.2: Cập Nhật docker-compose.yml

Tạo file `docker-compose.production.yml`:

```bash
nano docker-compose.production.yml
```

```yaml
version: "3.8"

services:
  mysql:
    image: mysql:8.0
    container_name: finpal-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --init-connect='SET NAMES utf8mb4'
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/database/finpal_schema.sql:/docker-entrypoint-initdb.d/1-schema.sql
      - ./backend/database/seed_data.sql:/docker-entrypoint-initdb.d/2-seed.sql
    networks:
      - finpal-network
    healthcheck:
      test:
        [
          "CMD",
          "mysqladmin",
          "ping",
          "-h",
          "localhost",
          "-u",
          "root",
          "-p${MYSQL_ROOT_PASSWORD}",
        ]
      timeout: 5s
      retries: 10

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: finpal-backend
    restart: always
    ports:
      - "${BACKEND_PORT}:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?characterEncoding=UTF-8&useUnicode=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION}
      AI_BACKEND_URL: ${AI_BACKEND_URL}
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - finpal-network
    healthcheck:
      test:
        [
          "CMD",
          "wget",
          "--quiet",
          "--tries=1",
          "--spider",
          "http://localhost:8080/actuator/health",
        ]
      interval: 30s
      timeout: 10s
      retries: 3

  backend-ai:
    build:
      context: ./backendAI
      dockerfile: Dockerfile
    container_name: finpal-backend-ai
    restart: always
    ports:
      - "${AI_BACKEND_PORT}:8000"
    volumes:
      - ai_models:/app/models
    networks:
      - finpal-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Không expose phpMyAdmin ra ngoài trong production
  phpmyadmin:
    image: phpmyadmin:latest
    container_name: finpal-phpmyadmin
    restart: always
    ports:
      - "127.0.0.1:${PMA_PORT}:80" # Chỉ localhost
    environment:
      PMA_HOST: ${PMA_HOST}
      PMA_PORT: 3306
      UPLOAD_LIMIT: 100M
    depends_on:
      - mysql
    networks:
      - finpal-network

volumes:
  mysql_data:
    driver: local
  ai_models:
    driver: local

networks:
  finpal-network:
    driver: bridge
```

### Bước 4.3: Build & Deploy

```bash
# Load environment variables
export $(cat .env.production | xargs)

# Build images
docker compose -f docker-compose.production.yml build

# Start services
docker compose -f docker-compose.production.yml up -d

# Check status
docker compose -f docker-compose.production.yml ps

# View logs
docker compose -f docker-compose.production.yml logs -f
```

### Bước 4.4: Kiểm Tra Services

```bash
# Check container health
docker ps

# Test Backend API
curl http://localhost:8080/actuator/health

# Test AI API
curl http://localhost:8000/health

# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456"}'
```

---

## 5. Cấu Hình Domain & SSL

### Bước 5.1: Cấu Hình Domain

**Tại nhà cung cấp domain (GoDaddy, Namecheap, etc.):**

Thêm DNS Records:

| Type  | Name | Value              | TTL |
| ----- | ---- | ------------------ | --- |
| A     | api  | `<ELASTIC_IP>`     | 600 |
| A     | ai   | `<ELASTIC_IP>`     | 600 |
| CNAME | www  | api.yourdomain.com | 600 |

Ví dụ:

- `api.finpal.com` → Backend Java
- `ai.finpal.com` → Backend AI

### Bước 5.2: Cài Nginx Reverse Proxy

```bash
sudo apt install -y nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

**Cấu hình Nginx:**

```bash
sudo nano /etc/nginx/sites-available/finpal-backend
```

```nginx
# Backend Java API
server {
    listen 80;
    server_name api.finpal.com;

    client_max_body_size 50M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}

# Backend AI API
server {
    listen 80;
    server_name ai.finpal.com;

    location / {
        proxy_pass http://localhost:8000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}
```

**Enable site:**

```bash
sudo ln -s /etc/nginx/sites-available/finpal-backend /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Bước 5.3: Cài SSL với Let's Encrypt

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Generate SSL certificates
sudo certbot --nginx -d api.finpal.com -d ai.finpal.com

# Auto-renewal setup
sudo systemctl status certbot.timer
```

**Certbot sẽ tự động:**

- Generate SSL certificates
- Cấu hình Nginx redirect HTTP → HTTPS
- Setup auto-renewal

**Test SSL:**

```bash
curl https://api.finpal.com/actuator/health
```

---

## 6. Monitoring & Bảo Trì

### Bước 6.1: Setup Firewall (UFW)

```bash
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw enable
sudo ufw status
```

### Bước 6.2: Monitoring Logs

```bash
# Docker logs
docker compose -f docker-compose.production.yml logs -f

# Nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# System logs
sudo journalctl -u docker -f
```

### Bước 6.3: Backup Database

**Tạo script backup:**

```bash
mkdir -p ~/backups
nano ~/backup-db.sh
```

```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=~/backups
MYSQL_ROOT_PASSWORD="<YOUR_PASSWORD>"

docker exec finpal-mysql mysqldump -uroot -p$MYSQL_ROOT_PASSWORD finpal_db > $BACKUP_DIR/finpal_db_$DATE.sql

# Keep only last 7 days
find $BACKUP_DIR -name "finpal_db_*.sql" -mtime +7 -delete

echo "Backup completed: finpal_db_$DATE.sql"
```

**Setup cron job (backup hàng ngày lúc 2 AM):**

```bash
chmod +x ~/backup-db.sh
crontab -e
```

Thêm dòng:

```
0 2 * * * /home/ubuntu/backup-db.sh >> /home/ubuntu/backups/backup.log 2>&1
```

### Bước 6.4: Update & Restart

**Khi có code mới:**

```bash
cd ~/projects/Finpal-MobileApp

# Pull latest code
git pull origin main

# Rebuild & restart
docker compose -f docker-compose.production.yml build
docker compose -f docker-compose.production.yml up -d

# View logs
docker compose -f docker-compose.production.yml logs -f
```

### Bước 6.5: Docker Cleanup

```bash
# Remove unused images
docker image prune -a -f

# Remove unused volumes
docker volume prune -f

# Full cleanup
docker system prune -a -f
```

---

## 📊 Giám Sát Tài Nguyên

### Check Disk Space

```bash
df -h
```

### Check Memory

```bash
free -h
```

### Check Docker Stats

```bash
docker stats
```

### Top Processes

```bash
htop  # Cài: sudo apt install htop
```

---

## 🔒 Bảo Mật

### 1. Change SSH Port (Tùy chọn)

```bash
sudo nano /etc/ssh/sshd_config
# Change: Port 2222
sudo systemctl restart sshd
```

### 2. Disable Root Login

```bash
sudo nano /etc/ssh/sshd_config
# Set: PermitRootLogin no
sudo systemctl restart sshd
```

### 3. Install Fail2Ban

```bash
sudo apt install -y fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

### 4. Update Regularly

```bash
sudo apt update
sudo apt upgrade -y
sudo reboot  # Nếu cần
```

---

## 🚀 URLs Sau Khi Deploy

| Service         | URL                                    | Description                |
| --------------- | -------------------------------------- | -------------------------- |
| **Backend API** | https://api.finpal.com                 | REST API                   |
| **Swagger UI**  | https://api.finpal.com/swagger-ui.html | API Documentation          |
| **AI API**      | https://ai.finpal.com                  | AI Categorization          |
| **AI Docs**     | https://ai.finpal.com/docs             | FastAPI Docs               |
| **phpMyAdmin**  | http://`<ELASTIC_IP>`:8081             | DB Management (local only) |

---

## ⚠️ Xử Lý Lỗi

### 1. Container Failed to Start

```bash
# Check logs
docker compose -f docker-compose.production.yml logs backend

# Restart service
docker compose -f docker-compose.production.yml restart backend
```

### 2. Out of Memory

```bash
# Check memory
free -h

# Increase swap
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### 3. Disk Full

```bash
# Clean Docker
docker system prune -a -f

# Clean old logs
sudo journalctl --vacuum-time=7d
```

### 4. SSL Certificate Issues

```bash
# Renew manually
sudo certbot renew

# Check auto-renewal
sudo certbot renew --dry-run
```

---

## 📝 Checklist Deploy

- [ ] EC2 instance created với adequate resources
- [ ] Security groups configured
- [ ] Elastic IP allocated
- [ ] Docker & Docker Compose installed
- [ ] Code uploaded/cloned
- [ ] `.env.production` configured
- [ ] Strong passwords set (MySQL, JWT)
- [ ] Services built và started
- [ ] Health checks passing
- [ ] Domain DNS configured
- [ ] Nginx reverse proxy setup
- [ ] SSL certificates installed
- [ ] Firewall (UFW) enabled
- [ ] Backup script created
- [ ] Monitoring setup
- [ ] Test all APIs
- [ ] Documentation updated

---

## 💰 Chi Phí Ước Tính (AWS)

**EC2 t2.medium (24/7):**

- ~$30-40/month

**EC2 t2.large (24/7):**

- ~$60-80/month

**Elastic IP:**

- Free khi attached to running instance

**Storage (20GB):**

- ~$2/month

**Data Transfer:**

- 1GB free/month
- $0.09/GB sau đó

**Total:** ~$35-85/month tùy instance type

**Tiết kiệm chi phí:**

- Dùng Reserved Instances (giảm 30-70%)
- Lightsail: $10-40/month (easier setup)
- AWS Free Tier: 750h t2.micro miễn phí (12 tháng đầu)

---

## 🎯 Next Steps

1. **Setup CI/CD với GitHub Actions**
2. **Monitoring với CloudWatch/Prometheus**
3. **Load Balancer cho high availability**
4. **Database replication/backup strategy**
5. **CDN cho static assets**
6. **Rate limiting & API throttling**

---

## 📞 Hỗ Trợ

**Các lệnh hữu ích:**

```bash
# Service status
docker compose -f docker-compose.production.yml ps

# Restart all
docker compose -f docker-compose.production.yml restart

# Stop all
docker compose -f docker-compose.production.yml down

# View specific logs
docker logs finpal-backend -f
docker logs finpal-mysql -f

# SSH tunnel for phpMyAdmin (từ local)
ssh -i finpal-key.pem -L 8081:localhost:8081 ubuntu@<ELASTIC_IP>
# Access: http://localhost:8081
```

---

## 🎉 Hoàn Thành!

Backend của bạn đã sẵn sàng production trên AWS!

**Happy Deploying! 🚀**
