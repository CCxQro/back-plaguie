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