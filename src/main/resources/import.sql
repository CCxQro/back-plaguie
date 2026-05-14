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


