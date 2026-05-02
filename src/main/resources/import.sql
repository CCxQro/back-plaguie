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

-- 3.1 Respetando los datos existentes (IDs 1 y 2, asumiendo id_rol 1 para ellos por el volcado)
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (1, 'ex1@gmail.com', 'gty6qkgWEWdpVYYRJRkHVfyZpPA3', 1, 'Ex1', 1);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (2, 'Dav@gmail.com', 'GcmYUnDKyzNUf4lzJI3BTBBgrfY2', 1, 'Davicho', 1);

-- 3.2 Nuevos Administradores (id_rol = 1) -> Total de 5 administradores
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (3, 'admin3@gmail.com', 'pVtLnT4RIjfYL0LtuvaHIZnA6mx2', 1, 'Admin3', 1);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (4, 'admin4@gmail.com', 'Wou1NFGdTaOFAf4ICaNrd6x2F9i2', 1, 'Admin4', 1);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (5, 'admin5@gmail.com', '2LTqkEM8P1bZCPbT1ySl5fWymCG3', 1, 'Admin5', 1);

-- 3.3 Nuevos Agricultores (id_rol = 2) -> Total de 5 agricultores
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (6, 'agri1@gmail.com', 'i1TbB18EtlbX8lMT9hGECRXGEUF3', 1, 'AJuan', 2);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (7, 'agri2@gmail.com', 'c2hc3CW2VUT57pc13NpeJWqo24B2', 1, 'AMaria', 2);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (8, 'agri3@gmail.com', 'sltP9g6CzWdPT3MGoztz314i8S42', 1, 'APedro', 2);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (9, 'agri4@gmail.com', 'FJ9QfhyZG1cn4FRUoRccMNjSIFq1', 1, 'ALucia', 2);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (10, 'agri5@gmail.com', 'J8X7wk5XJSXDwJsYIZ26KFtAGbv2', 1, 'ACarlos', 2);

-- 3.4 Nuevos Técnicos Vendedores (id_rol = 3) -> Total de 5 técnicos
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (11, 'tec1@gmail.com', 'WyhDU9gb1Mb4CbXxuQFWkDZWVwC3', 1, 'TRoberto', 3);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (12, 'tec2@gmail.com', '3Q3KxUwqJkYmS1tMYdJRr3tvRB03', 1, 'TAna', 3);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (13, 'tec3@gmail.com', 'a5IqtyPATEgwRqxeUjvHsPsubSB3', 1, 'TLuis', 3);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (14, 'tec4@gmail.com', 'eGmuWI2en2ZDI4iGNz0apt812Co2', 1, 'TSofia', 3);
INSERT INTO Usuario (id_usuario, email, uuid_firebase, isActive, nombre, id_rol) VALUES (15, 'tec5@gmail.com', 'b4zklFnLY0YhVQsOF0Ht0LcLasw2', 1, 'TJorge', 3);


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
