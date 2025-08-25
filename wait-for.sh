#!/bin/sh
# Espera a que el host y puerto estén disponibles
host="$1"
port="$2"
shift 2

echo "Esperando a que $host:$port esté disponible..."

while ! nc -z "$host" "$port"; do
  sleep 1
done

echo "$host:$port está disponible. Ejecutando aplicación..."
exec "$@"
