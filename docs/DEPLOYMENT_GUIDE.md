# 🚀 Guía de Despliegue - Fudi Backend en VPS

## 📋 Requisitos Previos
- VPS M con Ubuntu 20.04/22.04
- Dominio: fudi.es configurado con DNS apuntando a la IP del VPS
- Acceso SSH al VPS

## 🔧 Instalación en VPS

### 1. Conectarse al VPS
```bash
ssh root@tu-ip-vps
```

### 2. Actualizar Sistema
```bash
apt update && apt upgrade -y
```

### 3. Instalar Docker y Docker Compose
```bash
# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Instalar Docker Compose
apt install docker-compose -y

# Verificar instalación
docker --version
docker-compose --version
```

### 4. Instalar Nginx
```bash
apt install nginx -y
systemctl enable nginx
systemctl start nginx
```

### 5. Instalar Certbot (SSL/HTTPS)
```bash
apt install certbot python3-certbot-nginx -y
```

## 📦 Configuración del Proyecto

### 1. Clonar Repositorio
```bash
cd /opt
git clone https://github.com/JehielL/fudi-backend.git
cd fudi-backend
```

### 2. Configurar Variables de Entorno
```bash
# Crear archivo .env para producción
nano .env.production
```

Contenido del archivo `.env.production`:
```env
# Base de datos
MYSQL_ROOT_PASSWORD=TuPasswordSegura123!
MYSQL_DATABASE=fudidb
SPRING_DATASOURCE_URL=jdbc:mysql://java_db:3306/fudidb
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=TuPasswordSegura123!

# Spring/JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

# JWT
JWT_SECRET=UnSecretoMuyLargoYSeguroParaProduccionQueDebeCambiar2026!

# Email (opcional, configurar si quieres emails)
EMAIL_ENABLED=false
EMAIL_FROM=noreply@fudi.es
FRONTEND_URL=https://fudi.es

# Uploads
FILE_UPLOAD_DIR=/app/uploads
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=10MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=30MB
```

### 3. Configurar Nginx como Reverse Proxy
```bash
nano /etc/nginx/sites-available/fudi.es
```

Contenido del archivo:
```nginx
# Redirect HTTP to HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name fudi.es www.fudi.es;
    
    # Certbot challenge
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }
    
    # Redirect to HTTPS
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS Server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name fudi.es www.fudi.es;

    # SSL certificates (se configurarán con certbot)
    ssl_certificate /etc/letsencrypt/live/fudi.es/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/fudi.es/privkey.pem;
    
    # SSL settings
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Logs
    access_log /var/log/nginx/fudi.es.access.log;
    error_log /var/log/nginx/fudi.es.error.log;

    # Max body size for uploads
    client_max_body_size 30M;

    # Proxy to Spring Boot
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_http_version 1.1;
        
        # Headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check endpoint
    location /api/health {
        proxy_pass http://localhost:8080/actuator/health;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
    }

    # Swagger UI
    location /swagger-ui/ {
        proxy_pass http://localhost:8080/swagger-ui/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # API Docs
    location /v3/api-docs {
        proxy_pass http://localhost:8080/v3/api-docs;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
    }
}
```

### 4. Activar Configuración de Nginx
```bash
# Crear enlace simbólico
ln -s /etc/nginx/sites-available/fudi.es /etc/nginx/sites-enabled/

# Eliminar configuración por defecto
rm /etc/nginx/sites-enabled/default

# Verificar configuración
nginx -t

# Recargar Nginx
systemctl reload nginx
```

### 5. Obtener Certificado SSL
```bash
# Obtener certificado para fudi.es
certbot --nginx -d fudi.es -d www.fudi.es

# Seguir instrucciones (proporcionar email, aceptar términos)
# Certbot configurará automáticamente el SSL en Nginx

# Verificar renovación automática
certbot renew --dry-run
```

### 6. Configurar Firewall
```bash
# Habilitar UFW
ufw enable

# Permitir SSH (¡IMPORTANTE! No te bloquees)
ufw allow 22/tcp

# Permitir HTTP y HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# Permitir MySQL solo desde localhost (seguridad)
ufw allow from 127.0.0.1 to any port 3306

# Ver reglas
ufw status
```

## 🐳 Desplegar con Docker

### 1. Usar Docker Compose para Producción
Ya tienes un `docker-compose.yml`, pero vamos a crear uno optimizado para producción:

```bash
nano docker-compose.production.yml
```

### 2. Construir y Levantar Servicios
```bash
# Construir imágenes
docker-compose -f docker-compose.production.yml build

# Levantar servicios en background
docker-compose -f docker-compose.production.yml up -d

# Ver logs
docker-compose -f docker-compose.production.yml logs -f

# Ver estado
docker-compose -f docker-compose.production.yml ps
```

### 3. Verificar que Todo Funciona
```bash
# Verificar contenedores
docker ps

# Probar endpoint de salud
curl http://localhost:8080/actuator/health

# Ver logs del backend
docker logs java_app -f
```

## 🔒 Seguridad Adicional

### 1. Configurar Fail2Ban (Protección SSH)
```bash
apt install fail2ban -y
systemctl enable fail2ban
systemctl start fail2ban
```

### 2. Deshabilitar Login Root SSH (después de crear usuario)
```bash
# Crear usuario no-root
adduser fudi
usermod -aG sudo fudi
usermod -aG docker fudi

# Editar SSH config
nano /etc/ssh/sshd_config

# Cambiar:
# PermitRootLogin no
# PasswordAuthentication no (si usas SSH keys)

# Reiniciar SSH
systemctl restart sshd
```

## 📊 Monitoreo y Mantenimiento

### Ver Logs
```bash
# Logs de Nginx
tail -f /var/log/nginx/fudi.es.access.log
tail -f /var/log/nginx/fudi.es.error.log

# Logs de Docker
docker-compose -f docker-compose.production.yml logs -f java_app
docker-compose -f docker-compose.production.yml logs -f java_db
```

### Backups de Base de Datos
```bash
# Crear script de backup
nano /opt/backup-mysql.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/opt/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

docker exec java_db mysqldump -u root -p'TuPasswordSegura123!' fudidb > $BACKUP_DIR/fudidb_$DATE.sql

# Mantener solo últimos 7 días
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

```bash
# Dar permisos
chmod +x /opt/backup-mysql.sh

# Agregar a crontab (backup diario a las 2am)
crontab -e
# Agregar línea:
# 0 2 * * * /opt/backup-mysql.sh
```

### Comandos Útiles
```bash
# Reiniciar servicios
docker-compose -f docker-compose.production.yml restart

# Actualizar código
cd /opt/fudi-backend
git pull
docker-compose -f docker-compose.production.yml build
docker-compose -f docker-compose.production.yml up -d

# Ver uso de recursos
docker stats

# Limpiar Docker
docker system prune -a
```

## 🌐 URLs de Acceso

Después del despliegue:
- **API Backend**: https://fudi.es/api
- **Health Check**: https://fudi.es/api/health
- **Swagger UI**: https://fudi.es/swagger-ui/index.html
- **API Docs**: https://fudi.es/v3/api-docs

## ⚠️ Troubleshooting

### Puerto 8080 en uso
```bash
lsof -i :8080
kill -9 <PID>
```

### Nginx no inicia
```bash
nginx -t
systemctl status nginx
journalctl -xe
```

### Contenedor no levanta
```bash
docker logs java_app
docker logs java_db
```

### Base de datos no conecta
```bash
docker exec -it java_db mysql -u root -p
SHOW DATABASES;
USE fudidb;
SHOW TABLES;
```

## 📞 Soporte
Si tienes problemas, revisa los logs primero:
1. Logs de Nginx
2. Logs de Docker
3. Logs de la aplicación

---

**¡Tu backend estará accesible en https://fudi.es/api!** 🎉
