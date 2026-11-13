-- S14, paso 2. SOLO en el esquema de desarrollo/pruebas.
-- Adaptado a las entidades actuales; no requiere columnas de auditoria V4.
-- Ejecutar UNA SOLA VEZ. Las ventas sinteticas alteran los reportes y no tienen detalle.
-- Comprobar antes el esquema, los tipos de columnas y que existen categorias/clientes.
select user as esquema_actual from dual;
select table_name, column_name, data_type from user_tab_columns
where table_name in ('PRODUCTOS', 'VENTAS', 'CATEGORIAS', 'CLIENTES')
order by table_name, column_id;
select count(*) as categorias from categorias;
select count(*) as clientes from clientes;

-- Ejecutar este bloque solo tras comprobar los resultados anteriores.
-- TRUE corresponde a Oracle con BOOLEAN SQL. Si ESTADO es NUMBER usar 1.
insert into productos (nombre, precio, stock, estado, id_categoria, fecha_creacion)
select 'Producto de prueba ' || lpad(level, 5, '0'),
       round(dbms_random.value(1, 200), 2),
       trunc(dbms_random.value(0, 500)), true,
       (select min(id) from categorias), systimestamp
from dual connect by level <= 5000;

insert into ventas (fecha_registro, total, estado, cliente_id)
select systimestamp - numtodsinterval(dbms_random.value(0, 365), 'DAY'),
       round(dbms_random.value(5, 500), 2), 'REGISTRADA',
       (select min(id) from clientes)
from dual connect by level <= 20000;
commit;

select count(*) as productos from productos;
select count(*) as ventas from ventas;
