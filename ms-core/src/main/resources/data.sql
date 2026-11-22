INSERT INTO lotes_publicados (
    id_proveedor, nombre_comercio, nombre_producto, descripcion, categoria,
    stock, unidades_vendidas, monto_liquido, comision, precio_oferta_final, precio_normal,
    fecha_caducidad, fin_oferta, fecha_publicacion, fecha_captura_foto, foto_base64,
    kg_co2_evitado_por_unidad, latitud, longitud, es_alcohol, activo)
VALUES
    (1, 'Minimarket Demo', 'Arroz 1 kg (pack x6)', 'Pack sellado', 'ABARROTES_ENVASADOS',
     8, 4, 3000, 300, 3300, 5200,
     DATEADD('DAY', 30, CURRENT_TIMESTAMP), DATEADD('HOUR', 6, CURRENT_TIMESTAMP),
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL,
     1.2, -33.0458, -71.6197, false, true),
    (1, 'Minimarket Demo', 'Vino tinto 750 ml', 'Reserva 2022', 'BEBIDAS_ALCOHOLICAS',
     5, 2, 4500, 450, 4950, 7990,
     DATEADD('DAY', 200, CURRENT_TIMESTAMP), DATEADD('HOUR', 12, CURRENT_TIMESTAMP),
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL,
     0.8, -33.0458, -71.6197, true, true),
    (2, 'Ferreteria El Puerto', 'Caja tornillos 200 u', 'Sobrestock', 'FERRETERIA',
     12, 3, 2000, 200, 2200, 3990,
     DATEADD('DAY', 365, CURRENT_TIMESTAMP), DATEADD('HOUR', 24, CURRENT_TIMESTAMP),
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL,
     0.5, -33.0420, -71.6260, false, true);
