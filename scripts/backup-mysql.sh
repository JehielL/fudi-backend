#!/bin/bash
# ==========================================
# SCRIPT DE BACKUP AUTOMÁTICO DE MYSQL
# ==========================================
# Uso: ./backup-mysql.sh
# Crontab: 0 2 * * * /opt/fudi-backend/scripts/backup-mysql.sh

set -e

# Configuración
BACKUP_DIR="/opt/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
CONTAINER_NAME="fudi_mysql"
DATABASE_NAME="fudidb"
RETENTION_DAYS=7

# Crear directorio si no existe
mkdir -p $BACKUP_DIR

echo "===================================="
echo "Iniciando backup de MySQL - $DATE"
echo "===================================="

# Verificar que el contenedor está corriendo
if [ ! "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
    echo "ERROR: El contenedor $CONTAINER_NAME no está corriendo"
    exit 1
fi

# Realizar backup
echo "Haciendo dump de la base de datos..."
docker exec $CONTAINER_NAME mysqldump \
    -u root \
    -p"${MYSQL_ROOT_PASSWORD}" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    $DATABASE_NAME > $BACKUP_DIR/${DATABASE_NAME}_$DATE.sql

# Comprimir backup
echo "Comprimiendo backup..."
gzip $BACKUP_DIR/${DATABASE_NAME}_$DATE.sql

# Tamaño del backup
BACKUP_SIZE=$(du -h $BACKUP_DIR/${DATABASE_NAME}_$DATE.sql.gz | cut -f1)
echo "Backup completado: ${DATABASE_NAME}_$DATE.sql.gz ($BACKUP_SIZE)"

# Limpiar backups antiguos
echo "Limpiando backups con más de $RETENTION_DAYS días..."
find $BACKUP_DIR -name "${DATABASE_NAME}_*.sql.gz" -mtime +$RETENTION_DAYS -delete

# Listar backups disponibles
echo ""
echo "Backups disponibles:"
ls -lh $BACKUP_DIR/${DATABASE_NAME}_*.sql.gz | tail -n 5

echo "===================================="
echo "Backup completado exitosamente"
echo "===================================="
