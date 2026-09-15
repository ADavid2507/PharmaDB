SELECT c.id AS id_categoria, c.nombre AS categoria,
       SUM(d.cantidad) AS unidades,
       SUM(d.subtotal) AS monto
FROM detalle_ventas d
JOIN ventas v ON v.id = d.venta_id
JOIN productos p ON p.id_producto = d.producto_id
JOIN categorias c ON c.id = p.id_categoria
WHERE v.estado = 'REGISTRADA'
  AND v.fecha_registro >= DATE '2026-09-01'
  AND v.fecha_registro <  DATE '2026-09-12'
GROUP BY c.id, c.nombre
ORDER BY SUM(d.subtotal) DESC;

SELECT p.id_producto, p.nombre AS producto,
       c.nombre AS categoria,
       SUM(d.cantidad) AS unidades,
       SUM(d.subtotal) AS monto
FROM detalle_ventas d
JOIN ventas v ON v.id = d.venta_id
JOIN productos p ON p.id_producto = d.producto_id
JOIN categorias c ON c.id = p.id_categoria
WHERE v.estado = 'REGISTRADA'
  AND v.fecha_registro >= DATE '2026-09-01'
  AND v.fecha_registro <  DATE '2026-09-12'
GROUP BY p.id_producto, p.nombre, c.nombre
ORDER BY SUM(d.cantidad) DESC;