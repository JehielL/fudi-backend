-- Script para migrar datos de booking existentes
-- Ejecutar en MySQL antes de iniciar la aplicación

-- 1. Primero verificar qué valores hay actualmente
-- SELECT DISTINCT status FROM booking;

-- 2. Si el campo status era BOOLEAN (0/1) o INTEGER, actualizar a STRING:
-- Cambiar tipo de columna a VARCHAR si es necesario
ALTER TABLE booking MODIFY COLUMN status VARCHAR(20);

-- 3. Actualizar valores numéricos/booleanos a enum strings
UPDATE booking SET status = 'CONFIRMED' WHERE status = '1' OR status = 'true';
UPDATE booking SET status = 'PENDING' WHERE status = '0' OR status = 'false' OR status IS NULL OR status = '';

-- 4. Establecer valor por defecto
ALTER TABLE booking ALTER COLUMN status SET DEFAULT 'PENDING';

-- 5. Verificar resultados
-- SELECT id, status FROM booking;
