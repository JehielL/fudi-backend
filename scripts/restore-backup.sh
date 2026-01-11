#!/bin/bash
# Script de restauración de backup de MySQL
# Uso: ./restore-backup.sh <archivo-backup.sql>

if [ -z "$1" ]; then
    echo "❌ Error: Debes especificar el archivo de backup"
    echo "Uso: $0 /root/backups/pre-deploy-20260111_140000.sql"
    echo ""
    echo "Backups disponibles:"
    ls -lh /root/backups/*.sql 2>/dev/null || echo "No hay backups"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "❌ Error: El archivo $BACKUP_FILE no existe"
    exit 1
fi

echo "⚠️  ATENCIÓN: Esto sobrescribirá la base de datos actual"
echo "Archivo: $BACKUP_FILE"
read -p "¿Continuar? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Cancelado"
    exit 0
fi

echo "🔄 Restaurando backup..."
docker exec -i fudi_mysql mysql -u root -pFudiDB2026! BiteBooking < "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Backup restaurado exitosamente"
else
    echo "❌ Error al restaurar backup"
    exit 1
fi
