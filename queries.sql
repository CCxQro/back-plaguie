-- =====================================================================
-- PriceRepositoryImpl
-- =====================================================================

-- save(Price)
INSERT INTO Precios (sku_id_vendedor, precio, fecha_precio)
VALUES (?, ?, ?);

-- findLatestBySkuSellerId(Long skuSellerId)
SELECT *
FROM Precios
WHERE sku_id_vendedor = ?
ORDER BY fecha_precio DESC
LIMIT 1;

-- findAllBySkuSellerId(Long skuSellerId)
SELECT *
FROM Precios
WHERE sku_id_vendedor = ?
ORDER BY fecha_precio DESC;


-- =====================================================================
-- InventoryActionRepositoryImpl
-- =====================================================================

-- findByInventoryActionId(Long)
SELECT *
FROM Acciones_Inventario
WHERE id_accion_inventario = ?;

-- findAllActions()
SELECT *
FROM Acciones_Inventario;


-- =====================================================================
-- InventoryRepositoryImpl
-- =====================================================================

-- save(Inventory)
INSERT INTO Inventario (sku_id_vendedor, cantidad, id_accion_inventario)
VALUES (?, ?, ?);

-- updateCantidad(Long inventoryId, Integer cantidad)
UPDATE Inventario
SET cantidad = ?
WHERE id_inventario = ?;

-- delete(Long inventoryId)
DELETE FROM Inventario
WHERE id_inventario = ?;

-- findByInventoryId(Long)
SELECT *
FROM Inventario
WHERE id_inventario = ?;

-- findAllBySkuSellerId(Long skuSellerId) -- newest first by id (audit order)
SELECT *
FROM Inventario
WHERE sku_id_vendedor = ?
ORDER BY id_inventario DESC;

-- sumByActionAndSkuSellerId(Long actionId, Long skuSellerId)
SELECT COALESCE(SUM(cantidad), 0)
FROM Inventario
WHERE id_accion_inventario = ?
  AND sku_id_vendedor = ?;

-- currentStock(Long skuSellerId)  -- two queries, subtracted in Java
SELECT COALESCE(SUM(cantidad), 0) AS added
FROM Inventario
WHERE id_accion_inventario = 1
  AND sku_id_vendedor = ?;

SELECT COALESCE(SUM(cantidad), 0) AS subtracted
FROM Inventario
WHERE id_accion_inventario = 2
  AND sku_id_vendedor = ?;


-- =====================================================================
-- ProductRepositoryImpl
-- =====================================================================

-- countAllProducts()
SELECT COUNT(*) FROM Productos;

-- countProductsByStockAbove(int threshold)
SELECT COUNT(*)
FROM Productos p
WHERE (
        COALESCE((SELECT SUM(i.cantidad) FROM Inventario i
                  WHERE i.sku_id_vendedor = p.sku_id_vendedor AND i.id_accion_inventario = 1), 0)
      - COALESCE((SELECT SUM(i.cantidad) FROM Inventario i
                  WHERE i.sku_id_vendedor = p.sku_id_vendedor AND i.id_accion_inventario = 2), 0)
      ) > ?;

-- countProductsByStockBetween(int minInclusive, int maxInclusive)
SELECT COUNT(*)
FROM Productos p
WHERE (
        COALESCE((SELECT SUM(i.cantidad) FROM Inventario i
                  WHERE i.sku_id_vendedor = p.sku_id_vendedor AND i.id_accion_inventario = 1), 0)
      - COALESCE((SELECT SUM(i.cantidad) FROM Inventario i
                  WHERE i.sku_id_vendedor = p.sku_id_vendedor AND i.id_accion_inventario = 2), 0)
      ) BETWEEN ? AND ?;

-- countProductsByStockBelow(int threshold)
SELECT COUNT(*)
FROM Productos p
WHERE (
        COALESCE((SELECT SUM(i.cantidad) FROM Inventario i
                  WHERE i.sku_id_vendedor = p.sku_id_vendedor AND i.id_accion_inventario = 1), 0)
      - COALESCE((SELECT SUM(i.cantidad) FROM Inventario i
                  WHERE i.sku_id_vendedor = p.sku_id_vendedor AND i.id_accion_inventario = 2), 0)
      ) < ?;

-- setActiveBySellerId(Long sellerId, boolean isActive)
UPDATE Productos
SET activo = ?
WHERE id_vendedor = ?;


-- =====================================================================
-- ParcelaRepositoryImpl
-- =====================================================================

-- findAllParcelas() / findParcelaById(Long) / findByFarmerId(Long)
SELECT p.*, f.*, u.*, l.*, ep.*, tc.*, sr.*
FROM Parcela p
LEFT JOIN Agricultor f ON p.id_agricultor = f.id_agricultor
LEFT JOIN Usuario u ON f.id_usuario = u.id_usuario
LEFT JOIN Ubicacion l ON p.id_ubicacion = l.id_ubicacion
LEFT JOIN Estado_Parcela ep ON p.id_estado_parcela = ep.id_estado_parcela
LEFT JOIN Tipo_Cultivo tc ON p.id_tipo_cultivo = tc.id_tipo_cultivo
LEFT JOIN Sistema_Riego sr ON p.id_sistema_riego = sr.id_sistema_riego
-- WHERE p.id_parcela = ?         (findParcelaById)
-- WHERE p.id_agricultor = ?      (findByFarmerId)
;

-- save(Parcela)
INSERT INTO Parcela (nombre_parcela, tamano_hectareas, fecha_siembra, fecha_cosecha,
                     ph_suelo, id_agricultor, id_ubicacion, id_estado_parcela,
                     id_tipo_cultivo, id_sistema_riego, activo)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- update(Parcela)
UPDATE Parcela
SET nombre_parcela     = ?,
    tamano_hectareas   = ?,
    fecha_siembra      = ?,
    fecha_cosecha      = ?,
    ph_suelo           = ?,
    id_agricultor      = ?,
    id_ubicacion       = ?,
    id_estado_parcela  = ?,
    id_tipo_cultivo    = ?,
    id_sistema_riego   = ?,
    activo             = ?
WHERE id_parcela = ?;

-- delete(Long parcelaId)
DELETE FROM Parcela WHERE id_parcela = ?;

-- setActiveByFarmerId(Long farmerId, boolean isActive)
UPDATE Parcela
SET activo = ?
WHERE id_agricultor = ?;


-- =====================================================================
-- LocationRepositoryImpl
-- =====================================================================

-- update(Location) -- mutate existing Ubicacion in place; user/farmer keeps the same id_ubicacion
UPDATE Ubicacion
SET coordenadas = ?,
    id_estado = ?,
    id_municipio = ?,
    id_localidad = ?,
    id_predio = ?
WHERE id_ubicacion = ?;