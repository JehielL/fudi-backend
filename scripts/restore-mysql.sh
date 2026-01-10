#!/bin/bash
# ==========================================
# SCRIPT DE RESTAURACIÓN DE MYSQL
# ==========================================
# Uso: ./restore-mysql.sh backup_file.sql.gz

set -e

if [ -z "$1" ]; then
    echo "Uso: $0 <archivo_backup.sql.gz>"
    echo "Ejemplo: $0 /opt/backups/mysql/fudidb_20260110_020000.sql.gz"
    exit 1
fi

BACKUP_FILE=$1
CONTAINER_NAME="fudi_mysql"
DATABASE_NAME="fudidb"

echo "===================================="
echo "Iniciando restauración de MySQL"
echo "===================================="

# Verificar que el archivo existe
if [ ! -f "$BACKUP_FILE" ]; then
    echo "ERROR: El archivo $BACKUP_FILE no existe"
    exit 1
fi

# Verificar que el contenedor está corriendo
if [ ! "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
    echo "ERROR: El contenedor $CONTAINER_NAME no está corriendo"
    exit 1
fi

# Confirmar restauración
echo "ADVERTENCIA: Esto sobrescribirá la base de datos $DATABASE_NAME"
echo "Archivo: $BACKUP_FILE"
read -p "¿Continuar? (yes/no): " -r
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Restauración cancelada"
    exit 0
fi

# Descomprimir y restaurar
echo "Restaurando base de datos..."
if [[ $BACKUP_FILE == *.gz ]]; then
    gunzip -c $BACKUP_FILE | docker exec -i $CONTAINER_NAME mysql \
        -u root \
        -p"${MYSQL_ROOT_PASSWORD}" \
        $DATABASE_NAME
else
    docker exec -i $CONTAINER_NAME mysql \
        -u root \
        -p"${MYSQL_ROOT_PASSWORD}" \
        $DATABASE_NAME < $BACKUP_FILE
fi

echo "===================================="
echo "Restauración completada exitosamente"
echo "===================================="
