-- ==========================================
-- 1. CATÁLOGOS DE UBICACIÓN
-- ==========================================

-- Estados
INSERT INTO Estados (id_estado, nombre) VALUES (1, 'jalisco');
INSERT INTO Estados (id_estado, nombre) VALUES (2, 'michoacán');
INSERT INTO Estados (id_estado, nombre) VALUES (3, 'sinaloa');
INSERT INTO Estados (id_estado, nombre) VALUES (4, 'sonora');
INSERT INTO Estados (id_estado, nombre) VALUES (5, 'veracruz');

-- Municipios
INSERT INTO Municipios (id_municipio, nombre) VALUES (1, 'zapopan');
INSERT INTO Municipios (id_municipio, nombre) VALUES (2, 'uruapan');
INSERT INTO Municipios (id_municipio, nombre) VALUES (3, 'culiacán');
INSERT INTO Municipios (id_municipio, nombre) VALUES (4, 'hermosillo');
INSERT INTO Municipios (id_municipio, nombre) VALUES (5, 'xalapa');

-- Localidades
INSERT INTO Localidades (id_localidad, nombre) VALUES (1, 'tesistan');
INSERT INTO Localidades (id_localidad, nombre) VALUES (2, 'san juan nuevo');
INSERT INTO Localidades (id_localidad, nombre) VALUES (3, 'el dorado');
INSERT INTO Localidades (id_localidad, nombre) VALUES (4, 'bahía kino');
INSERT INTO Localidades (id_localidad, nombre) VALUES (5, 'banderilla');

-- Predios
INSERT INTO Predios (id_predio, nombre) VALUES (1, 'el milagro');
INSERT INTO Predios (id_predio, nombre) VALUES (2, 'la esperanza');
INSERT INTO Predios (id_predio, nombre) VALUES (3, 'los pinos');
INSERT INTO Predios (id_predio, nombre) VALUES (4, 'buenavista');
INSERT INTO Predios (id_predio, nombre) VALUES (5, 'san José');


-- ==========================================
-- 2. UBICACIONES
-- (Dependen de Estados, Municipios, Localidades y Predios)
-- ==========================================
INSERT INTO Ubicacion (id_ubicacion, coordenadas, id_localidad, id_municipio, id_predio, id_estado) VALUES (1, ST_GeomFromText('POINT(-103.4800 20.7500)'), 1, 1, 1, 1);
INSERT INTO Ubicacion (id_ubicacion, coordenadas, id_localidad, id_municipio, id_predio, id_estado) VALUES (2, ST_GeomFromText('POINT(-102.0558 19.4138)'), 2, 2, 2, 2);
INSERT INTO Ubicacion (id_ubicacion, coordenadas, id_localidad, id_municipio, id_predio, id_estado) VALUES (3, ST_GeomFromText('POINT(-107.3941 24.8053)'), 3, 3, 3, 3);
INSERT INTO Ubicacion (id_ubicacion, coordenadas, id_localidad, id_municipio, id_predio, id_estado) VALUES (4, ST_GeomFromText('POINT(-110.9559 29.0729)'), 4, 4, 4, 4);
INSERT INTO Ubicacion (id_ubicacion, coordenadas, id_localidad, id_municipio, id_predio, id_estado) VALUES (5, ST_GeomFromText('POINT(-96.9101 19.5437)'), 5, 5, 5, 5);


-- ==========================================
-- 3. USUARIOS GLOBALES (Tabla `Usuario`)
-- ==========================================

-- 3.1 Administradores base usados para pruebas de validación
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (1, 'admin.operaciones@plaguie.test', 'gty6qkgWEWdpVYYRJRkHVfyZpPA3', 1, 'Ana Operaciones', 1);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (2, 'admin.campo@plaguie.test', 'GcmYUnDKyzNUf4lzJI3BTBBgrfY2', 1, 'David Campo', 1);

-- 3.2 Nuevos Administradores (id_rol = 1) -> Total de 5 administradores
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (3, 'admin.validacion@plaguie.test', 'pVtLnT4RIjfYL0LtuvaHIZnA6mx2', 1, 'Mariana Validación', 1);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (4, 'admin.inventario@plaguie.test', 'Wou1NFGdTaOFAf4ICaNrd6x2F9i2', 1, 'Roberto Inventario', 1);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (5, 'admin.reportes@plaguie.test', '2LTqkEM8P1bZCPbT1ySl5fWymCG3', 1, 'Lucía Reportes', 1);

-- 3.3 Nuevos Agricultores (id_rol = 2) -> Total de 5 agricultores
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (6, 'juan.mango@plaguie.test', 'i1TbB18EtlbX8lMT9hGECRXGEUF3', 1, 'Juan Hernández', 2);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (7, 'maria.soya@plaguie.test', 'c2hc3CW2VUT57pc13NpeJWqo24B2', 1, 'María González', 2);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (8, 'pedro.tomate@plaguie.test', 'sltP9g6CzWdPT3MGoztz314i8S42', 1, 'Pedro Ramírez', 2);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (9, 'lucia.citrico@plaguie.test', 'FJ9QfhyZG1cn4FRUoRccMNjSIFq1', 1, 'Lucía Torres', 2);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (10, 'carlos.campo@plaguie.test', 'J8X7wk5XJSXDwJsYIZ26KFtAGbv2', 1, 'Carlos Medina', 2);

-- 3.4 Nuevos Técnicos Vendedores (id_rol = 3) -> Total de 5 técnicos
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (11, 'roberto.tecnico@plaguie.test', 'WyhDU9gb1Mb4CbXxuQFWkDZWVwC3', 1, 'Roberto Salas', 3);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (12, 'ana.tecnica@plaguie.test', '3Q3KxUwqJkYmS1tMYdJRr3tvRB03', 1, 'Ana Beltrán', 3);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (13, 'luis.tecnico@plaguie.test', 'a5IqtyPATEgwRqxeUjvHsPsubSB3', 1, 'Luis Paredes', 3);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (14, 'sofia.tecnica@plaguie.test', 'eGmuWI2en2ZDI4iGNz0apt812Co2', 1, 'Sofía Duarte', 3);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (15, 'jorge.tecnico@plaguie.test', 'b4zklFnLY0YhVQsOF0Ht0LcLasw2', 1, 'Jorge Castillo', 3);


-- ==========================================
-- 4. MARKETPLACE - CATÁLOGOS
-- ==========================================

-- Colores
INSERT INTO Colores (id_color, name, hexa) VALUES (1, 'Rojo', '#FF0000');
INSERT INTO Colores (id_color, name, hexa) VALUES (2, 'Verde', '#008000');
INSERT INTO Colores (id_color, name, hexa) VALUES (3, 'Azul', '#0000FF');
INSERT INTO Colores (id_color, name, hexa) VALUES (4, 'Amarillo', '#FFFF00');
INSERT INTO Colores (id_color, name, hexa) VALUES (5, 'Naranja', '#FFA500');

-- Status
INSERT INTO Status (id_status, nombre) VALUES (1, 'Accepted');
INSERT INTO Status (id_status, nombre) VALUES (2, 'Revision');
INSERT INTO Status (id_status, nombre) VALUES (3, 'Rejected');

-- Acciones de Inventario (catalogo estatico: 1=add, 2=subtract)
INSERT INTO Acciones_Inventario (id_accion_inventario, accion) VALUES (1, 'add');
INSERT INTO Acciones_Inventario (id_accion_inventario, accion) VALUES (2, 'subtract');

-- Categorias (registradas por admins y tecnicos vendedores)
INSERT INTO Categorias (id_categoria, id_usuario, nombre, id_color, id_status) VALUES (1, 1,  'Fertilizantes',   1, 1);
INSERT INTO Categorias (id_categoria, id_usuario, nombre, id_color, id_status) VALUES (2, 2,  'Herbicidas',      2, 1);
INSERT INTO Categorias (id_categoria, id_usuario, nombre, id_color, id_status) VALUES (3, 11, 'Insecticidas',    3, 2);
INSERT INTO Categorias (id_categoria, id_usuario, nombre, id_color, id_status) VALUES (4, 12, 'Fungicidas',      4, 2);
INSERT INTO Categorias (id_categoria, id_usuario, nombre, id_color, id_status) VALUES (5, 3,  'Semillas',        5, 1);
INSERT INTO Categorias (id_categoria, id_usuario, nombre, id_color, id_status) VALUES (6, 13, 'Herramientas',    1, 3);
INSERT INTO Categorias (id_categoria, id_usuario, nombre, id_color, id_status) VALUES (7, 4,  'Equipos de Riego',2, 1);
INSERT INTO Categorias (id_categoria, id_usuario, nombre, id_color, id_status) VALUES (8, 14, 'Abonos',          3, 2);

-- ==========================================
-- 5. TABLAS DE ROLES ESPECÍFICOS
-- ==========================================

-- 4.1 Administradores (vinculados a los usuarios 1 al 5)
INSERT INTO Administrador (id_administrador, isActive, id_usuario) VALUES (1, 1, 1);
INSERT INTO Administrador (id_administrador, isActive, id_usuario) VALUES (2, 1, 2);
INSERT INTO Administrador (id_administrador, isActive, id_usuario) VALUES (3, 1, 3);
INSERT INTO Administrador (id_administrador, isActive, id_usuario) VALUES (4, 1, 4);
INSERT INTO Administrador (id_administrador, isActive, id_usuario) VALUES (5, 1, 5);

-- 4.2 Agricultores (vinculados a usuarios 6 al 10 y ubicaciones 1 al 5)
INSERT INTO Agricultor (id_agricultor, isActive, id_ubicacion, id_usuario) VALUES (1, 1, 1, 6);
INSERT INTO Agricultor (id_agricultor, isActive, id_ubicacion, id_usuario) VALUES (2, 1, 2, 7);
INSERT INTO Agricultor (id_agricultor, isActive, id_ubicacion, id_usuario) VALUES (3, 1, 3, 8);
INSERT INTO Agricultor (id_agricultor, isActive, id_ubicacion, id_usuario) VALUES (4, 1, 4, 9);
INSERT INTO Agricultor (id_agricultor, isActive, id_ubicacion, id_usuario) VALUES (5, 1, 5, 10);

-- 4.3 Técnicos Vendedores (vinculados a usuarios 11 al 15 y ubicaciones 1 al 5)
INSERT INTO Tecnico_Vendedor (id_tecnico_vendedor, isActive, id_ubicacion, id_usuario) VALUES (1, 1, 1, 11);
INSERT INTO Tecnico_Vendedor (id_tecnico_vendedor, isActive, id_ubicacion, id_usuario) VALUES (2, 1, 2, 12);
INSERT INTO Tecnico_Vendedor (id_tecnico_vendedor, isActive, id_ubicacion, id_usuario) VALUES (3, 1, 3, 13);
INSERT INTO Tecnico_Vendedor (id_tecnico_vendedor, isActive, id_ubicacion, id_usuario) VALUES (4, 1, 4, 14);
INSERT INTO Tecnico_Vendedor (id_tecnico_vendedor, isActive, id_ubicacion, id_usuario) VALUES (5, 1, 5, 15);

-- ==========================================
-- 5. VIGILANCIA FITOSANITARIA (MOCK DATA)
-- ==========================================

-- 5.1 Catalogos
INSERT INTO Sistemas_Monitoreos (id_sistema_monitoreo, nombre) VALUES (1, 'Trampeo semanal');
INSERT INTO Sistemas_Monitoreos (id_sistema_monitoreo, nombre) VALUES (2, 'Monitoreo visual');
INSERT INTO Sistemas_Monitoreos (id_sistema_monitoreo, nombre) VALUES (3, 'Muestreo por cuadrantes');

INSERT INTO Claves_Identificacion_plaga (id_cid, nombre) VALUES (1, 'CID-MOSCA-001');
INSERT INTO Claves_Identificacion_plaga (id_cid, nombre) VALUES (2, 'CID-HONGO-002');
INSERT INTO Claves_Identificacion_plaga (id_cid, nombre) VALUES (3, 'CID-ACARO-003');

INSERT INTO Plaga (id_plaga, nombre) VALUES (1, 'Mosca de la fruta');
INSERT INTO Plaga (id_plaga, nombre) VALUES (2, 'Roya asiatica');
INSERT INTO Plaga (id_plaga, nombre) VALUES (3, 'Araña roja');

INSERT INTO Hospedante (id_hospedante, nombre) VALUES (1, 'Mango');
INSERT INTO Hospedante (id_hospedante, nombre) VALUES (2, 'Soya');
INSERT INTO Hospedante (id_hospedante, nombre) VALUES (3, 'Tomate');

INSERT INTO Variedad (id_variedad, nombre) VALUES (1, 'Ataulfo');
INSERT INTO Variedad (id_variedad, nombre) VALUES (2, 'Huasteca 200');
INSERT INTO Variedad (id_variedad, nombre) VALUES (3, 'Saladette');

INSERT INTO Especie (id_especie, nombre) VALUES (1, 'Mangifera indica');
INSERT INTO Especie (id_especie, nombre) VALUES (2, 'Glycine max');
INSERT INTO Especie (id_especie, nombre) VALUES (3, 'Solanum lycopersicum');

-- 5.2 Registros de vigilancia fitosanitaria
INSERT INTO vigilancia_fitosanitaria (
	id_vigilancia_fitosanitaria,
	id_sistema_monitoreo,
	id_cid,
	lat,
	`long`,
	id_ubicacion,
	id_plaga,
	id_hospedante,
	id_variedad,
	id_especie,
	ahosp,
	id_status,
	id_validated_by,
	validated_at
) VALUES (1, 1, 1, 20.75000000, -103.48000000, 1, 1, 1, 1, 1, 12.50, 2, NULL, NULL);

INSERT INTO vigilancia_fitosanitaria (
	id_vigilancia_fitosanitaria,
	id_sistema_monitoreo,
	id_cid,
	lat,
	`long`,
	id_ubicacion,
	id_plaga,
	id_hospedante,
	id_variedad,
	id_especie,
	ahosp,
	id_status,
	id_validated_by,
	validated_at
) VALUES (2, 2, 2, 19.41380000, -102.05580000, 2, 2, 2, 2, 2, 8.25, 2, NULL, NULL);

INSERT INTO vigilancia_fitosanitaria (
	id_vigilancia_fitosanitaria,
	id_sistema_monitoreo,
	id_cid,
	lat,
	`long`,
	id_ubicacion,
	id_plaga,
	id_hospedante,
	id_variedad,
	id_especie,
	ahosp,
	id_status,
	id_validated_by,
	validated_at
) VALUES (3, 3, 3, 24.80530000, -107.39410000, 3, 3, 3, 3, 3, 15.00, 1, 1, '2026-05-10 10:30:00');

INSERT INTO vigilancia_fitosanitaria (
	id_vigilancia_fitosanitaria,
	id_sistema_monitoreo,
	id_cid,
	lat,
	`long`,
	id_ubicacion,
	id_plaga,
	id_hospedante,
	id_variedad,
	id_especie,
	ahosp,
	id_status,
	id_validated_by,
	validated_at
) VALUES (4, 2, 1, 29.07290000, -110.95590000, 4, 1, 1, 1, 1, 5.75, 1, 2, '2026-05-11 12:45:00');

INSERT INTO vigilancia_fitosanitaria (
	id_vigilancia_fitosanitaria,
	id_sistema_monitoreo,
	id_cid,
	lat,
	`long`,
	id_ubicacion,
	id_plaga,
	id_hospedante,
	id_variedad,
	id_especie,
	ahosp,
	id_status,
	id_validated_by,
	validated_at
) VALUES (5, 1, 2, 19.54370000, -96.91010000, 5, 2, 2, 2, 2, 18.40, 3, 3, '2026-05-12 09:20:00');

-- ==========================================
-- 6. ALERTAS Y RECOMENDACIONES PARA VALIDACIÓN
-- ==========================================

INSERT INTO alertas (
	id_alerta,
	titulo,
	descripcion,
	id_ubicacion,
	tipo_plaga,
	hectareas,
	severidad,
	id_reported_by,
	created_at,
	id_status,
	id_validated_by,
	validated_at
) VALUES (1, 'Brote de mosca de la fruta en mango', 'Capturas elevadas en trampas perimetrales del predio El Milagro.', 1, 'Mosca de la fruta', 12.50, 'critico', 6, '2026-05-10 08:15:00', 2, NULL, NULL);

INSERT INTO alertas (
	id_alerta,
	titulo,
	descripcion,
	id_ubicacion,
	tipo_plaga,
	hectareas,
	severidad,
	id_reported_by,
	created_at,
	id_status,
	id_validated_by,
	validated_at
) VALUES (2, 'Riesgo de roya por humedad persistente', 'Se observaron condiciones favorables para roya asiática en el lote de soya.', 2, 'Roya asiatica', 8.25, 'advertencia', 7, '2026-05-10 11:00:00', 2, NULL, NULL);

INSERT INTO alertas (
	id_alerta,
	titulo,
	descripcion,
	id_ubicacion,
	tipo_plaga,
	hectareas,
	severidad,
	id_reported_by,
	created_at,
	id_status,
	id_validated_by,
	validated_at
) VALUES (3, 'Araña roja confirmada en tomate', 'Daño visible en hojas medias, requiere seguimiento de control biológico.', 3, 'Araña roja', 15.00, 'informacion', 8, '2026-05-09 15:30:00', 1, 1, '2026-05-10 09:10:00');

INSERT INTO alertas (
	id_alerta,
	titulo,
	descripcion,
	id_ubicacion,
	tipo_plaga,
	hectareas,
	severidad,
	id_reported_by,
	created_at,
	id_status,
	id_validated_by,
	validated_at
) VALUES (4, 'Reporte duplicado de mosca de la fruta', 'El reporte repite la misma evidencia de la alerta 1 y debe corregirse.', 4, 'Mosca de la fruta', 5.75, 'advertencia', 9, '2026-05-09 17:45:00', 3, 2, '2026-05-11 13:20:00');

INSERT INTO recomendaciones (
	id_recomendacion,
	titulo,
	descripcion,
	tipo_plaga,
	productos_recomendados,
	id_reported_by,
	created_at,
	id_status,
	id_validated_by,
	validated_at
) VALUES (1, 'Control preventivo para mosca de la fruta', 'Instalar trampas McPhail y reforzar monitoreo semanal en bordes del cultivo.', 'Mosca de la fruta', 'Spinosad, trampas McPhail', 11, '2026-05-10 09:40:00', 2, NULL, NULL);

INSERT INTO recomendaciones (
	id_recomendacion,
	titulo,
	descripcion,
	tipo_plaga,
	productos_recomendados,
	id_reported_by,
	created_at,
	id_status,
	id_validated_by,
	validated_at
) VALUES (2, 'Manejo inicial de roya asiática', 'Aplicar fungicida sistémico si la humedad relativa se mantiene por encima del umbral.', 'Roya asiatica', 'Triazol, estrobilurina', 12, '2026-05-10 13:10:00', 2, NULL, NULL);

INSERT INTO recomendaciones (
	id_recomendacion,
	titulo,
	descripcion,
	tipo_plaga,
	productos_recomendados,
	id_reported_by,
	created_at,
	id_status,
	id_validated_by,
	validated_at
) VALUES (3, 'Liberación de control biológico', 'Liberar ácaros depredadores y reducir aplicaciones de amplio espectro.', 'Araña roja', 'Phytoseiulus persimilis, aceite agrícola', 13, '2026-05-09 10:25:00', 1, 1, '2026-05-10 16:05:00');

INSERT INTO recomendaciones (
	id_recomendacion,
	titulo,
	descripcion,
	tipo_plaga,
	productos_recomendados,
	id_reported_by,
	created_at,
	id_status,
	id_validated_by,
	validated_at
) VALUES (4, 'Dosis incompleta para roya', 'La recomendación no incluye intervalo de reentrada ni compatibilidad del producto.', 'Roya asiatica', 'Fungicida sin ficha técnica', 14, '2026-05-09 18:05:00', 3, 3, '2026-05-11 08:30:00');

-- 6. UNIDADES (registradas por admins y técnicos vendedores)
-- ==========================================

-- Registradas por administradores -> status Accepted (1)
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (1, 1, 'Kilogramo',  1);
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (2, 2, 'Litro',      1);
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (3, 3, 'Tonelada',   1);
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (4, 4, 'Gramo',      1);
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (5, 5, 'Mililitro',  1);

-- Registradas por técnicos vendedores -> status Revision (2)
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (6,  11, 'Caja',     2);
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (7,  12, 'Bolsa',    2);
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (8,  13, 'Paquete',  2);
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (9,  14, 'Frasco',   2);
INSERT INTO Unidades (id_unidad, id_usuario, nombre, id_status) VALUES (10, 15, 'Cubeta',   2);

-- ==========================================
-- 7. PROVEEDORES (registrados por técnicos vendedores)
-- ==========================================

INSERT INTO Proveedores (id_proveedor, id_usuario, nombre) VALUES (1, 11, 'AgroSuministros del Norte');
INSERT INTO Proveedores (id_proveedor, id_usuario, nombre) VALUES (2, 12, 'Distribuidora Campo Verde');
INSERT INTO Proveedores (id_proveedor, id_usuario, nombre) VALUES (3, 13, 'Insumos Agrícolas Sinaloa');
INSERT INTO Proveedores (id_proveedor, id_usuario, nombre) VALUES (4, 14, 'Comercializadora del Pacífico');
INSERT INTO Proveedores (id_proveedor, id_usuario, nombre) VALUES (5, 15, 'Semillas y Abonos del Golfo');

-- ==========================================
-- 8. PRODUCTOS
-- ==========================================
-- Productos 1-5: coinciden con los IDs del mock de la app mobile (SCRUM-207)
-- Agricultor 1 (id_usuario=6) tiene un pedido entregado con estos productos (ver sección 10/11)
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1, 1, 'Insecticida Ultra',   'PLG-M01', 3, 1,  80.0, 2, 'Insecticida de amplio espectro',      1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (2, 1, 'Fungicida Mancozeb',  'PLG-M02', 4, 1, 120.0, 1, 'Fungicida protector preventivo',      1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (3, 1, 'Fertilizante Bio',    'PLG-M03', 1, 1,  50.0, 1, 'Fertilizante organico enriquecido',   1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (4, 1, 'Plaguicida Pro',      'PLG-M04', 3, 1, 150.0, 2, 'Plaguicida de alto rendimiento',      1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (5, 1, 'Herbicida 2,4-D',     'PLG-M05', 2, 1,  90.0, 2, 'Herbicida sistemico selectivo',       1);
-- Productos 1001-1010: catálogo del marketplace web (2 por vendedor)
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1001, 1, 'Fertilizante NPK 20-20-20',  'PLG-001', 1, 1, 250.0, 1, 'Fertilizante balanceado para cultivos', 1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1002, 1, 'Herbicida Glifosato 36%',    'PLG-002', 2, 1, 180.0, 2, 'Control de malezas de hoja ancha',     1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1003, 2, 'Insecticida Clorpirifos 48E','PLG-003', 3, 2, 320.0, 2, 'Control de plagas del suelo',          1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1004, 2, 'Fungicida Mancozeb 80%',    'PLG-004', 4, 2, 145.0, 1, 'Proteccion contra hongos foliares',    1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1005, 3, 'Semilla Maiz Hibrido H-318','PLG-005', 5, 3, 890.0, 1, 'Semilla certificada alto rendimiento', 1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1006, 3, 'Semilla Sorgo Hibrido H-50','PLG-006', 5, 3, 650.0, 1, 'Semilla sorgo resistente a sequia',    1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1007, 4, 'Bioestimulante Auxinas',    'PLG-007', 1, 4,  95.0, 2, 'Promotor de enraizamiento',            1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1008, 4, 'Sulfato de Magnesio',       'PLG-008', 2, 4,  60.0, 1, 'Corrector de deficiencias de Mg',     1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1009, 5, 'Abono Organico Compostado', 'PLG-009', 8, 5,  45.0, 1, 'Mejora estructura del suelo',          1);
INSERT INTO Productos (sku_id_vendedor, id_vendedor, nombre, sku, id_categoria, id_proveedor, valor_unidad, id_unidad, descripcion, id_status) VALUES (1010, 5, 'Cal Agricola 90%',          'PLG-010', 2, 5,  30.0, 1, 'Corrector de pH acido',               1);

-- ==========================================
-- 8.B PRECIOS (historico por producto; el mas reciente = valor_unidad del producto)
-- ==========================================
-- Producto 1001 (Fertilizante NPK 20-20-20, valor_unidad=250)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (1,  1001, 220.00000, '2025-01-12 09:00:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (2,  1001, 240.00000, '2025-03-08 11:30:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (3,  1001, 250.00000, '2025-05-02 14:00:00');

-- Producto 1002 (Herbicida Glifosato 36%, valor_unidad=180)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (4,  1002, 160.00000, '2025-02-05 10:15:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (5,  1002, 180.00000, '2025-04-20 09:45:00');

-- Producto 1003 (Insecticida Clorpirifos 48E, valor_unidad=320)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (6,  1003, 300.00000, '2025-01-30 08:30:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (7,  1003, 320.00000, '2025-04-12 13:00:00');

-- Producto 1004 (Fungicida Mancozeb 80%, valor_unidad=145)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (8,  1004, 145.00000, '2025-03-15 12:00:00');

-- Producto 1005 (Semilla Maiz Hibrido H-318, valor_unidad=890)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (9,  1005, 850.00000, '2025-02-18 09:20:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (10, 1005, 890.00000, '2025-04-25 16:00:00');

-- Producto 1006 (Semilla Sorgo Hibrido H-50, valor_unidad=650)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (11, 1006, 620.00000, '2025-01-22 10:45:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (12, 1006, 650.00000, '2025-03-29 11:15:00');

-- Producto 1007 (Bioestimulante Auxinas, valor_unidad=95)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (13, 1007,  90.00000, '2025-02-10 08:00:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (14, 1007,  95.00000, '2025-04-08 09:30:00');

-- Producto 1008 (Sulfato de Magnesio, valor_unidad=60)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (15, 1008,  60.00000, '2025-03-03 10:00:00');

-- Producto 1009 (Abono Organico Compostado, valor_unidad=45)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (16, 1009,  40.00000, '2025-01-18 14:30:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (17, 1009,  45.00000, '2025-04-06 15:00:00');

-- Producto 1010 (Cal Agricola 90%, valor_unidad=30)
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (18, 1010,  28.00000, '2025-02-22 09:00:00');
INSERT INTO Precios (id_precio, sku_id_vendedor, precio, fecha_precio) VALUES (19, 1010,  30.00000, '2025-05-10 12:30:00');

-- ==========================================
-- 8.C INVENTARIO (historial por producto, cronologico por id_inventario ascendente)
-- id_accion_inventario: 1 = add, 2 = subtract
-- Cada producto mantiene running total >= 0 en cada paso. Stock final entre () al lado del producto.
-- ==========================================

-- Producto 1001 (final stock = 140)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (1,  1001, 100, 1); -- 100
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (2,  1001,  50, 1); -- 150
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (3,  1001,  30, 2); -- 120
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (4,  1001,  20, 1); -- 140

-- Producto 1002 (final stock = 75)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (5,  1002,  80, 1); -- 80
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (6,  1002,  20, 1); -- 100
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (7,  1002,  25, 2); --  75

-- Producto 1003 (final stock = 120)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (8,  1003, 100, 1); -- 100
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (9,  1003,  40, 2); --  60
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (10, 1003,  60, 1); -- 120

-- Producto 1004 (final stock = 100)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (11, 1004,  50, 1); --  50
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (12, 1004,  50, 1); -- 100

-- Producto 1005 (final stock = 150)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (13, 1005, 100, 1); -- 100
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (14, 1005,  20, 2); --  80
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (15, 1005,  30, 2); --  50
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (16, 1005, 100, 1); -- 150

-- Producto 1006 (final stock = 100)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (17, 1006,  60, 1); --  60
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (18, 1006,  40, 1); -- 100

-- Producto 1007 (final stock = 75)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (19, 1007,  90, 1); --  90
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (20, 1007,  15, 2); --  75

-- Producto 1008 (final stock = 100)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (21, 1008, 100, 1); -- 100

-- Producto 1009 (final stock = 50)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (22, 1009,  70, 1); --  70
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (23, 1009,  30, 1); -- 100
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (24, 1009,  50, 2); --  50

-- Producto 1010 (final stock = 120)
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (25, 1010, 100, 1); -- 100
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (26, 1010,  20, 2); --  80
INSERT INTO Inventario (id_inventario, sku_id_vendedor, cantidad, id_accion_inventario) VALUES (27, 1010,  40, 1); -- 120

-- ==========================================
-- 9. ESTADOS_PEDIDO
-- ==========================================
INSERT INTO Estados_Pedido (id_estado_pedido, estado) VALUES (1, 'Pendiente');
INSERT INTO Estados_Pedido (id_estado_pedido, estado) VALUES (2, 'Confirmado');
INSERT INTO Estados_Pedido (id_estado_pedido, estado) VALUES (3, 'En camino');
INSERT INTO Estados_Pedido (id_estado_pedido, estado) VALUES (4, 'Entregado');

-- ==========================================
-- 10. PEDIDOS (10 órdenes; id_agricultor 1-5 / id_vendedor 1-5)
-- ==========================================
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (1,  1, 1, '2025-01-10 10:00:00', 4,  860.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (2,  2, 1, '2025-01-15 11:30:00', 3,  610.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (3,  3, 2, '2025-02-01 09:00:00', 4,  610.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (4,  4, 2, '2025-02-14 14:00:00', 2,  785.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (5,  1, 3, '2025-03-05 08:45:00', 1, 1540.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (6,  5, 3, '2025-03-20 16:00:00', 2, 1780.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (7,  2, 4, '2025-04-02 12:00:00', 4,  405.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (8,  3, 4, '2025-04-10 09:00:00', 3,  370.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (9,  4, 5, '2025-04-18 10:30:00', 2,  315.00);
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (10, 5, 5, '2025-05-01 09:15:00', 1,  240.00);
-- Pedido 11: agricultor 1 (id_usuario=6), entregado, contiene productos 1-5 del mock mobile
INSERT INTO Pedido (id_pedido, id_agricultor, id_vendedor, fecha_pedido, id_estado_pedido, monto_total) VALUES (11, 1, 1, '2025-04-01 09:00:00', 4, 7500.00);

-- ==========================================
-- 11. DETALLE_PEDIDO (19 líneas; totales verificados)
-- ==========================================
-- Pedido 1: 2x1001(250) + 2x1002(180) = 860
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (1,  1, 1001, 2, 250.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (2,  1, 1002, 2, 180.0);
-- Pedido 2: 1x1001(250) + 2x1002(180) = 610
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (3,  2, 1001, 1, 250.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (4,  2, 1002, 2, 180.0);
-- Pedido 3: 1x1003(320) + 2x1004(145) = 610
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (5,  3, 1003, 1, 320.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (6,  3, 1004, 2, 145.0);
-- Pedido 4: 2x1003(320) + 1x1004(145) = 785
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (7,  4, 1003, 2, 320.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (8,  4, 1004, 1, 145.0);
-- Pedido 5: 1x1005(890) + 1x1006(650) = 1540
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (9,  5, 1005, 1, 890.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (10, 5, 1006, 1, 650.0);
-- Pedido 6: 2x1005(890) = 1780
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (11, 6, 1005, 2, 890.0);
-- Pedido 7: 3x1007(95) + 2x1008(60) = 405
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (12, 7, 1007, 3, 95.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (13, 7, 1008, 2, 60.0);
-- Pedido 8: 2x1007(95) + 3x1008(60) = 370
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (14, 8, 1007, 2, 95.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (15, 8, 1008, 3, 60.0);
-- Pedido 9: 5x1009(45) + 3x1010(30) = 315
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (16, 9, 1009, 5, 45.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (17, 9, 1010, 3, 30.0);
-- Pedido 10: 4x1009(45) + 2x1010(30) = 240
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (18, 10, 1009, 4, 45.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (19, 10, 1010, 2, 30.0);
-- Pedido 11: 15x1 + 10x2 + 60x3 + 5x4 + 15x5 = 1200+1200+3000+750+1350 = 7500
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (20, 11, 1, 15,  80.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (21, 11, 2, 10, 120.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (22, 11, 3, 60,  50.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (23, 11, 4,  5, 150.0);
INSERT INTO Detalle_Pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES (24, 11, 5, 15,  90.0);

-- ==========================================
-- 12. CATÁLOGOS DE PARCELA
-- ==========================================

-- Estados de parcela
INSERT INTO Estados_Parcelas (id_estado_parcela, nombre) VALUES (1, 'Activa');
INSERT INTO Estados_Parcelas (id_estado_parcela, nombre) VALUES (2, 'Inactiva');
INSERT INTO Estados_Parcelas (id_estado_parcela, nombre) VALUES (3, 'En preparación');

-- Tipos de cultivo
INSERT INTO Tipos_Cultivos (id_tipo_cultivo, nombre, fecha_siembra, fecha_cosecha) VALUES (1, 'Maíz',      '2025-05-01', '2025-10-15');
INSERT INTO Tipos_Cultivos (id_tipo_cultivo, nombre, fecha_siembra, fecha_cosecha) VALUES (2, 'Tomate',    '2025-03-01', '2025-07-30');
INSERT INTO Tipos_Cultivos (id_tipo_cultivo, nombre, fecha_siembra, fecha_cosecha) VALUES (3, 'Aguacate',  '2025-01-15', '2025-09-30');
INSERT INTO Tipos_Cultivos (id_tipo_cultivo, nombre, fecha_siembra, fecha_cosecha) VALUES (4, 'Caña',      '2025-06-01', '2026-03-01');
INSERT INTO Tipos_Cultivos (id_tipo_cultivo, nombre, fecha_siembra, fecha_cosecha) VALUES (5, 'Mango',     '2025-02-01', '2025-08-15');

-- Sistemas de riego
INSERT INTO Sistemas_Riego (id_sistema_riego, nombre) VALUES (1, 'Goteo');
INSERT INTO Sistemas_Riego (id_sistema_riego, nombre) VALUES (2, 'Aspersión');
INSERT INTO Sistemas_Riego (id_sistema_riego, nombre) VALUES (3, 'Gravedad');
INSERT INTO Sistemas_Riego (id_sistema_riego, nombre) VALUES (4, 'Microaspersión');
INSERT INTO Sistemas_Riego (id_sistema_riego, nombre) VALUES (5, 'Temporal');

-- ==========================================
-- 13. PARCELAS
-- (Dependen de Agricultor, Ubicacion, Estados_Parcelas, Tipos_Cultivos y Sistemas_Riego)
-- ==========================================
INSERT INTO Parcela (id_parcela, nombre_parcela, tamano_hectareas, fecha_siembra, fecha_cosecha, ph_suelo, id_agricultor, id_ubicacion, id_estado_parcela, id_tipo_cultivo, id_sistema_riego) VALUES (1, 'Parcela El Milagro',   12.50, '2025-05-01', '2025-10-15', 6.5, 1, 1, 1, 1, 1);
INSERT INTO Parcela (id_parcela, nombre_parcela, tamano_hectareas, fecha_siembra, fecha_cosecha, ph_suelo, id_agricultor, id_ubicacion, id_estado_parcela, id_tipo_cultivo, id_sistema_riego) VALUES (2, 'Parcela La Esperanza', 8.75,  '2025-03-01', '2025-07-30', 7.0, 2, 2, 1, 2, 2);
INSERT INTO Parcela (id_parcela, nombre_parcela, tamano_hectareas, fecha_siembra, fecha_cosecha, ph_suelo, id_agricultor, id_ubicacion, id_estado_parcela, id_tipo_cultivo, id_sistema_riego) VALUES (3, 'Parcela Los Pinos',    15.00, '2025-01-15', '2025-09-30', 5.8, 3, 3, 1, 3, 3);
INSERT INTO Parcela (id_parcela, nombre_parcela, tamano_hectareas, fecha_siembra, fecha_cosecha, ph_suelo, id_agricultor, id_ubicacion, id_estado_parcela, id_tipo_cultivo, id_sistema_riego) VALUES (4, 'Parcela Buenavista',   6.30,  '2025-06-01', '2026-03-01', 6.2, 4, 4, 2, 4, 4);
INSERT INTO Parcela (id_parcela, nombre_parcela, tamano_hectareas, fecha_siembra, fecha_cosecha, ph_suelo, id_agricultor, id_ubicacion, id_estado_parcela, id_tipo_cultivo, id_sistema_riego) VALUES (5, 'Parcela San José',     20.00, '2025-02-01', '2025-08-15', 6.8, 5, 5, 1, 5, 1);

-- ==========================================
-- 14. APLICACIONES DE INSUMOS
-- Agricultor 1 (id_usuario=6): stocks post-aplicación
--   Producto 1 (Insecticida Ultra):  entregados=15, aplicados=2.0  → disponible=13.0
--   Producto 2 (Fungicida Mancozeb): entregados=10, aplicados=1.5  → disponible=8.5
--   Producto 3 (Fertilizante Bio):   entregados=60, aplicados=10.0 → disponible=50.0
--   Producto 4 (Plaguicida Pro):     entregados=5,  aplicados=0    → disponible=5.0
--   Producto 5 (Herbicida 2,4-D):    entregados=15, aplicados=0    → disponible=15.0
-- ==========================================
INSERT INTO Aplicacion_Insumo (id_aplicacion, id_agricultor, sku_id_vendedor, cantidad, fecha, id_parcela) VALUES (1, 1, 1, 2.0,  '2025-05-20', 1);
INSERT INTO Aplicacion_Insumo (id_aplicacion, id_agricultor, sku_id_vendedor, cantidad, fecha, id_parcela) VALUES (2, 1, 2, 1.5,  '2025-05-15', 1);
INSERT INTO Aplicacion_Insumo (id_aplicacion, id_agricultor, sku_id_vendedor, cantidad, fecha, id_parcela) VALUES (3, 1, 3, 10.0, '2025-04-28', 1);
