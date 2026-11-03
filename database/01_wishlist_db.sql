-- Ejecutar en MySQL Workbench (File > Open SQL Script > Run).
-- Opcional: con spring.jpa.hibernate.ddl-auto=update Hibernate crea la tabla solo.
-- Útil si quieres DDL_AUTO=validate en producción.

CREATE DATABASE IF NOT EXISTS wishlist_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE wishlist_db;

CREATE TABLE IF NOT EXISTS deseos (
  id                   BIGINT       NOT NULL AUTO_INCREMENT,
  usuario_id           BIGINT       NOT NULL,
  producto_id          BIGINT       NOT NULL,
  cantidad_deseada     INT          NOT NULL DEFAULT 1,
  fecha_creacion       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  fecha_actualizacion  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  CONSTRAINT uk_deseos_usuario_producto UNIQUE (usuario_id, producto_id),
  CONSTRAINT ck_deseos_cantidad CHECK (cantidad_deseada >= 1)
) ENGINE=InnoDB;
