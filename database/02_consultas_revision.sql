-- Consultas para la revisión a fondo en MySQL Workbench
USE wishlist_db;

SELECT * FROM deseos ORDER BY id DESC;                                  -- contenido
SELECT usuario_id, COUNT(*) AS items FROM deseos GROUP BY usuario_id;    -- deseos por usuario
SELECT usuario_id, producto_id, COUNT(*) FROM deseos
 GROUP BY usuario_id, producto_id HAVING COUNT(*) > 1;                   -- duplicados (debe dar 0 filas)
SELECT * FROM deseos WHERE cantidad_deseada < 1;                         -- inválidos (debe dar 0 filas)
SHOW INDEX FROM deseos;                                                  -- verifica la restricción única
