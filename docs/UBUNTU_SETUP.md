# 🚀 Instalación Rápida en Ubuntu

## Opción 1: Script Automatizado (Recomendado)

Una vez que Ubuntu esté instalado, ejecuta:

```bash
# Conectar al VPS
ssh root@82.165.173.240

# Ejecutar script de instalación automatizado
curl -sSL https://raw.githubusercontent.com/JehielL/fudi-backend/master/scripts/install-vps.sh | bash
```

O si prefieres descargarlo primero:

```bash
wget https://raw.githubusercontent.com/JehielL/fudi-backend/master/scripts/install-vps.sh
chmod +x install-vps.sh
./install-vps.sh
```

El script instalará automáticamente:
- ✅ Docker y Docker Compose
- ✅ Nginx
- ✅ Certbot (SSL/HTTPS)
- ✅ Configurará el firewall
- ✅ Clonará el repositorio
- ✅ Creará los archivos de configuración

## Opción 2: Instalación Manual

### 1. Conectar y actualizar
```bash
ssh root@82.165.173.240

# Actualizar sistema
apt update && apt upgrade -y
```

### 2. Instalar dependencias
```bash
# Instalar herramientas básicas
apt install -y curl wget git nano vim ufw

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Instalar Docker Compose
apt install -y docker-compose

# Instalar Nginx
apt install -y nginx

# Instalar Certbot
apt install -y certbot python3-certbot-nginx
```

### 3. Habilitar servicios
```bash
systemctl enable docker
systemctl start docker
systemctl enable nginx
systemctl start nginx
```

### 4. Configurar firewall
```bash
ufw enable
ufw allow 22/tcp   # SSH
ufw allow 80/tcp   # HTTP
ufw allow 443/tcp  # HTTPS
ufw status
```

### 5. Clonar repositorio
```bash
cd /opt
git clone https://github.com/JehielL/fudi-backend.git
cd fudi-backend
```

### 6. Configurar variables de entorno
```bash
cp .env.production.example .env.production
nano .env.production
```

**Cambiar estos valores**:
```env
MYSQL_ROOT_PASSWORD=TuPasswordSegura123!
SPRING_DATASOURCE_PASSWORD=TuPasswordSegura123!
JWT_SECRET=GeneraUnoConOpenSSLRandBase64
```

Generar JWT_SECRET seguro:
```bash
openssl rand -base64 64
```

### 7. Configurar DNS
Desde tu proveedor de dominio (donde compraste fudi.es):
```
Tipo: A
Host: @
Valor: 82.165.173.240

Tipo: A
Host: www
Valor: 82.165.173.240
```

Espera 5-15 minutos para propagación DNS. Verificar con:
```bash
nslookup fudi.es
ping fudi.es
```

### 8. Configurar Nginx
```bash
nano /etc/nginx/sites-available/fudi.es
```

Copiar la configuración de Nginx desde [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md#3-configurar-nginx-como-reverse-proxy)

```bash
# Activar configuración
ln -s /etc/nginx/sites-available/fudi.es /etc/nginx/sites-enabled/
rm /etc/nginx/sites-enabled/default

# Verificar configuración
nginx -t

# Recargar Nginx
systemctl reload nginx
```

### 9. Obtener certificado SSL
```bash
certbot --nginx -d fudi.es -d www.fudi.es
```

Seguir las instrucciones:
- Proporcionar email
- Aceptar términos de servicio
- Elegir redirección a HTTPS (opción 2)

### 10. Levantar servicios con Docker
```bash
cd /opt/fudi-backend

# Construir y levantar
docker-compose -f docker-compose.production.yml up -d

# Ver logs
docker-compose -f docker-compose.production.yml logs -f
```

### 11. Verificar funcionamiento
```bash
# Ver contenedores
docker ps

# Probar API
curl http://localhost:8080/actuator/health
curl https://fudi.es/api/health

# Ver logs
docker logs fudi_backend -f
```

## ✅ Verificación Final

Si todo está bien, deberías poder acceder a:

- ✅ https://fudi.es/api/health
- ✅ https://fudi.es/swagger-ui/index.html
- ✅ https://fudi.es/v3/api-docs

## 🔧 Comandos Útiles

```bash
# Ver logs
docker-compose -f docker-compose.production.yml logs -f

# Reiniciar servicios
docker-compose -f docker-compose.production.yml restart

# Parar servicios
docker-compose -f docker-compose.production.yml down

# Ver estado
docker ps
docker stats

# Backup de base de datos
cd /opt/fudi-backend
chmod +x scripts/backup-mysql.sh
./scripts/backup-mysql.sh
```

## ⚠️ Troubleshooting

### DNS no resuelve
```bash
nslookup fudi.es
# Si no resuelve, esperar más tiempo o verificar configuración DNS
```

### Certbot falla
```bash
# Asegurarse de que DNS apunta correctamente
# Verificar que Nginx está corriendo
systemctl status nginx

# Verificar puertos abiertos
netstat -tulpn | grep :80
netstat -tulpn | grep :443
```

### Backend no inicia
```bash
# Ver logs
docker logs fudi_backend

# Verificar variables de entorno
cat /opt/fudi-backend/.env.production

# Reiniciar
docker-compose -f docker-compose.production.yml restart
```

### MySQL no conecta
```bash
# Ver logs de MySQL
docker logs fudi_mysql

# Entrar a MySQL
docker exec -it fudi_mysql mysql -u root -p

# Verificar que la base de datos existe
SHOW DATABASES;
USE fudidb;
SHOW TABLES;
```

---

**¡Una vez completados estos pasos, tu backend estará accesible en https://fudi.es/api!** 🎉
