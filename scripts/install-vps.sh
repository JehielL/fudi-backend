#!/bin/bash
# ==========================================
# SCRIPT DE INSTALACIÓN AUTOMATIZADA - FUDI BACKEND
# ==========================================
# Compatible con Ubuntu/Debian y CentOS/RHEL
# Uso: curl -sSL https://raw.githubusercontent.com/JehielL/fudi-backend/master/scripts/install-vps.sh | bash

set -e

echo "=========================================="
echo "  Instalación Fudi Backend - VPS Setup"
echo "=========================================="
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar que se ejecuta como root
if [[ $EUID -ne 0 ]]; then
   echo -e "${RED}Error: Este script debe ejecutarse como root${NC}"
   echo "Uso: sudo bash install-vps.sh"
   exit 1
fi

# Detectar sistema operativo
echo -e "${YELLOW}Detectando sistema operativo...${NC}"
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
    VERSION=$VERSION_ID
    echo -e "${GREEN}Sistema detectado: $PRETTY_NAME${NC}"
else
    echo -e "${RED}No se pudo detectar el sistema operativo${NC}"
    exit 1
fi

# Función para instalar en Ubuntu/Debian
install_ubuntu() {
    echo -e "${YELLOW}Instalando para Ubuntu/Debian...${NC}"
    
    # Actualizar sistema
    echo "Actualizando sistema..."
    apt update && apt upgrade -y
    
    # Instalar dependencias básicas
    echo "Instalando dependencias básicas..."
    apt install -y curl wget git nano vim ufw
    
    # Instalar Docker
    echo "Instalando Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh
    
    # Instalar Docker Compose
    echo "Instalando Docker Compose..."
    apt install -y docker-compose
    
    # Instalar Nginx
    echo "Instalando Nginx..."
    apt install -y nginx
    
    # Instalar Certbot
    echo "Instalando Certbot..."
    apt install -y certbot python3-certbot-nginx
    
    echo -e "${GREEN}Instalación completada para Ubuntu/Debian${NC}"
}

# Función para instalar en CentOS/RHEL
install_centos() {
    echo -e "${YELLOW}Instalando para CentOS/RHEL...${NC}"
    
    # Actualizar sistema
    echo "Actualizando sistema..."
    yum update -y
    
    # Instalar dependencias básicas
    echo "Instalando dependencias básicas..."
    yum install -y curl wget git nano vim firewalld
    
    # Instalar Docker
    echo "Instalando Docker..."
    yum install -y yum-utils
    yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    yum install -y docker-ce docker-ce-cli containerd.io
    systemctl start docker
    systemctl enable docker
    
    # Instalar Docker Compose
    echo "Instalando Docker Compose..."
    curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    
    # Instalar Nginx
    echo "Instalando Nginx..."
    yum install -y epel-release
    yum install -y nginx
    
    # Instalar Certbot
    echo "Instalando Certbot..."
    yum install -y certbot python3-certbot-nginx
    
    echo -e "${GREEN}Instalación completada para CentOS/RHEL${NC}"
}

# Instalar según el sistema operativo
case $OS in
    ubuntu|debian)
        install_ubuntu
        ;;
    centos|rhel|rocky|almalinux)
        install_centos
        ;;
    *)
        echo -e "${RED}Sistema operativo no soportado: $OS${NC}"
        exit 1
        ;;
esac

# Habilitar y configurar servicios
echo -e "${YELLOW}Configurando servicios...${NC}"

# Docker
systemctl enable docker
systemctl start docker

# Nginx
systemctl enable nginx
systemctl start nginx

# Verificar instalaciones
echo ""
echo -e "${YELLOW}Verificando instalaciones...${NC}"
docker --version
docker-compose --version
nginx -v

# Configurar firewall
echo ""
echo -e "${YELLOW}Configurando firewall...${NC}"

if command -v ufw &> /dev/null; then
    # Ubuntu/Debian (UFW)
    echo "Configurando UFW..."
    ufw --force enable
    ufw allow 22/tcp comment 'SSH'
    ufw allow 80/tcp comment 'HTTP'
    ufw allow 443/tcp comment 'HTTPS'
    ufw status
elif command -v firewall-cmd &> /dev/null; then
    # CentOS/RHEL (firewalld)
    echo "Configurando firewalld..."
    systemctl enable firewalld
    systemctl start firewalld
    firewall-cmd --permanent --add-service=ssh
    firewall-cmd --permanent --add-service=http
    firewall-cmd --permanent --add-service=https
    firewall-cmd --reload
    firewall-cmd --list-all
fi

# Clonar repositorio
echo ""
echo -e "${YELLOW}Clonando repositorio fudi-backend...${NC}"
cd /opt
if [ -d "fudi-backend" ]; then
    echo "El directorio fudi-backend ya existe. Actualizando..."
    cd fudi-backend
    git pull
else
    git clone https://github.com/JehielL/fudi-backend.git
    cd fudi-backend
fi

# Crear archivo de configuración
echo ""
echo -e "${YELLOW}Creando archivo de configuración...${NC}"
if [ ! -f ".env.production" ]; then
    cp .env.production.example .env.production
    echo -e "${GREEN}Archivo .env.production creado${NC}"
    echo -e "${RED}IMPORTANTE: Edita /opt/fudi-backend/.env.production con tus credenciales${NC}"
else
    echo -e "${YELLOW}.env.production ya existe, no se sobrescribe${NC}"
fi

# Dar permisos a scripts
chmod +x scripts/*.sh 2>/dev/null || true

echo ""
echo -e "${GREEN}=========================================="
echo "  ✓ Instalación completada exitosamente"
echo "==========================================${NC}"
echo ""
echo -e "${YELLOW}Próximos pasos:${NC}"
echo ""
echo "1. Editar configuración:"
echo "   nano /opt/fudi-backend/.env.production"
echo ""
echo "2. Configurar Nginx para fudi.es:"
echo "   nano /etc/nginx/sites-available/fudi.es"
echo "   (Ver docs/DEPLOYMENT_GUIDE.md para la configuración)"
echo ""
echo "3. Obtener certificado SSL:"
echo "   certbot --nginx -d fudi.es -d www.fudi.es"
echo ""
echo "4. Levantar servicios:"
echo "   cd /opt/fudi-backend"
echo "   docker-compose -f docker-compose.production.yml up -d"
echo ""
echo -e "${YELLOW}Para más información, consulta: /opt/fudi-backend/docs/DEPLOYMENT_GUIDE.md${NC}"
