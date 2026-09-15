-- Juego de datos existente: cliente 21, ventas 41 y 42.
-- Script de referencia; rechaza una segunda ejecución por el DNI.
DECLARE
  c NUMBER; vr NUMBER; va NUMBER; n NUMBER;
BEGIN
  SELECT COUNT(*) INTO n FROM clientes WHERE dni='90000003';
  IF n > 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'DNI de prueba ya existe');
  END IF;
  INSERT INTO clientes
    (dni,nombres,apellidos,email,telefono,direccion,
     estado,fecha_creacion)
  VALUES ('90000003','Cliente','Prueba Autonoma',
    'autonoma@example.invalid','900000003','Datos ficticios',
    TRUE,SYSTIMESTAMP) RETURNING id INTO c;
  INSERT INTO ventas (cliente_id,fecha_registro,estado,total)
  VALUES (c,TIMESTAMP '2026-08-10 10:00:00','REGISTRADA',37)
  RETURNING id INTO vr;
  INSERT INTO detalle_ventas
    (venta_id,producto_id,cantidad,precio,subtotal)
  VALUES (vr,28,2,18.50,37);
  INSERT INTO ventas (cliente_id,fecha_registro,estado,total)
  VALUES (c,TIMESTAMP '2026-08-20 10:00:00','ANULADA',22)
  RETURNING id INTO va;
  INSERT INTO detalle_ventas
    (venta_id,producto_id,cantidad,precio,subtotal)
  VALUES (va,29,1,22,22);
  UPDATE productos SET stock=stock-2
  WHERE id_producto=28 AND stock>=2;
  IF SQL%ROWCOUNT<>1 THEN
    RAISE_APPLICATION_ERROR(-20002,'Revisar stock producto 28');
  END IF;
  UPDATE productos SET stock=stock-1
  WHERE id_producto=29 AND stock>=1;
  IF SQL%ROWCOUNT<>1 THEN
    RAISE_APPLICATION_ERROR(-20003,'Revisar stock producto 29');
  END IF;
  COMMIT;
EXCEPTION WHEN OTHERS THEN ROLLBACK; RAISE;
END;
/