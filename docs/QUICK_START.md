# 🚀 Inicio Rápido - Despliegue Fudi Backend

## Resumen de Pasos

### 1️⃣ Preparar VPS
```bash
# Conectar al VPS
ssh root@TU_IP_VPS

# Actualizar sistema
apt update && apt upgrade -y

# Instalar Docker, Docker Compose y Nginx
curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh
apt install docker-compose nginx certbot python3-certbot-nginx -y
```

### 2️⃣ Configurar DNS
Desde tu proveedor de dominio, configura:
```
Tipo A:   fudi.es     →  IP_DE_TU_VPS
Tipo A:   www.fudi.es →  IP_DE_TU_VPS
```

### 3️⃣ Clonar y Configurar Proyecto
```bash
cd /opt
git clone https://github.com/JehielL/fudi-backend.git
cd fudi-backend

# Copiar y editar variables de entorno
cp .env.production.example .env.production
nano .env.production
```

Cambiar en `.env.production`:
- `MYSQL_ROOT_PASSWORD`: una contraseña segura
- `SPRING_DATASOURCE_PASSWORD`: la misma contraseña
- `JWT_SECRET`: generar con `openssl rand -base64 64`

### 4️⃣ Configurar Nginx
```bash
# Copiar configuración de Nginx desde la guía
nano /etc/nginx/sites-available/fudi.es

# Activar configuración
ln -s /etc/nginx/sites-available/fudi.es /etc/nginx/sites-enabled/
rm /etc/nginx/sites-enabled/default
nginx -t
systemctl reload nginx
```

### 5️⃣ Obtener SSL
```bash
certbot --nginx -d fudi.es -d www.fudi.es
```

### 6️⃣ Configurar Firewall
```bash
ufw enable
ufw allow 22/tcp   # SSH
ufw allow 80/tcp   # HTTP
ufw allow 443/tcp  # HTTPS
```

### 7️⃣ Levantar Servicios
```bash
cd /opt/fudi-backend

# Construir y levantar con Docker Compose
docker-compose -f docker-compose.production.yml up -d

# Ver logs
docker-compose -f docker-compose.production.yml logs -f
```

### 8️⃣ Verificar Funcionamiento
```bash
# Health check
curl https://fudi.es/api/health

# Ver contenedores
docker ps
```

## ✅ URLs Disponibles
- **API**: https://fudi.es/api
- **Health**: https://fudi.es/api/health
- **Swagger**: https://fudi.es/swagger-ui/index.html
- **API Docs**: https://fudi.es/v3/api-docs

## 🔄 Comandos Útiles

### Ver logs
```bash
cd /opt/fudi-backend
docker-compose -f docker-compose.production.yml logs -f java_app
```

### Reiniciar servicios
```bash
docker-compose -f docker-compose.production.yml restart
```

### Actualizar código
```bash
cd /opt/fudi-backend
git pull
docker-compose -f docker-compose.production.yml build
docker-compose -f docker-compose.production.yml up -d
```

### Backup de base de datos
```bash
chmod +x scripts/backup-mysql.sh
./scripts/backup-mysql.sh
```

## 📊 Monitoreo
```bash
# Estado de servicios
docker ps

# Recursos
docker stats

# Logs de Nginx
tail -f /var/log/nginx/fudi.es.access.log
tail -f /var/log/nginx/fudi.es.error.log
```

## ⚠️ Problemas Comunes

**Error: Puerto 8080 en uso**
```bash
docker-compose -f docker-compose.production.yml down
docker-compose -f docker-compose.production.yml up -d
```

**Error: No conecta a MySQL**
```bash
# Verificar contenedor MySQL
docker logs fudi_mysql

# Entrar a MySQL
docker exec -it fudi_mysql mysql -u root -p
```

**Error: 502 Bad Gateway en Nginx**
```bash
# Verificar que el backend esté corriendo
docker logs fudi_backend

# Reiniciar servicios
docker-compose -f docker-compose.production.yml restart
```

---

Para más detalles, consulta [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
