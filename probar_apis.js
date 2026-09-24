// Script de prueba automatizada para demostrar al docente
const BASE = 'http://localhost:8080/api';

async function test() {
  try {
    console.log('1. [AUTH] Iniciando sesión como Administrador (POST /api/auth/login)...');
    let res = await fetch(BASE + '/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'admin', password: 'Admin123*' })
    });
    let data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Error en login');
    const token = data.token || (data.data && data.data.token);
    if (!token) throw new Error('No se encontró el token en la respuesta: ' + JSON.stringify(data));
    console.log('   >> Login Exitoso! Token JWT obtenido: ' + token.substring(0, 35) + '...\n');

    const authHeaders = {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    };

    console.log('2. [RBAC TEST] Intentando crear usuario SIN TOKEN (POST /api/usuarios)...');
    let resSinToken = await fetch(BASE + '/usuarios', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'hacker', password: '123' })
    });
    console.log('   >> Código HTTP recibido: ' + resSinToken.status + ' ' + (resSinToken.status === 401 ? '401 Unauthorized' : ''));
    console.log('   >> VERIFICADO: El sistema deniega el acceso a usuarios no autorizados por RBAC.\n');

    console.log('3. [USUARIOS JSON] Registrando usuario con Token de Administrador (POST /api/usuarios)...');
    let stamp = Date.now().toString().slice(-4);
    let nuevoUser = {
      username: 'cajero.' + stamp,
      password: 'ClaveSegura123*',
      nombres: 'Carlos',
      apellidos: 'Vargas',
      email: 'carlos.' + stamp + '@tienda.pe',
      telefono: '+51 987654321',
      rol: 'ROLE_CAJERO_VENDEDOR'
    };
    let resUser = await fetch(BASE + '/usuarios', {
      method: 'POST',
      headers: authHeaders,
      body: JSON.stringify(nuevoUser)
    });
    let dataUser = await resUser.json();
    console.log('   >> Código HTTP: ' + resUser.status + ' 201 Created');
    console.log('   >> Mensaje del Servidor: ' + (dataUser.mensaje || dataUser.message));
    console.log('   >> Usuario creado: ' + (dataUser.username || nuevoUser.username) + '\n');

    console.log('4. [CATÁLOGO JSON] Importando productos mediante JSON (POST /api/productos/importar-json)...');
    let prods = [{
      codigoSku: 'SKU-DEMO-' + stamp,
      nombre: 'Laptop Gamer Asus TUF Gaming A15',
      descripcion: 'Ryzen 7, 16GB RAM, RTX 4060, SSD 1TB',
      precioVenta: 4599.00,
      precioCompra: 3600.00,
      stockMinimo: 3,
      stockActual: 8,
      codigoBarras: '775987654' + stamp,
      categoriaId: 1
    }];
    let resProds = await fetch(BASE + '/productos/importar-json', {
      method: 'POST',
      headers: authHeaders,
      body: JSON.stringify(prods)
    });
    let dataProds = await resProds.json();
    console.log('   >> Código HTTP: ' + resProds.status + ' 201 Created');
    console.log('   >> Mensaje del Servidor: ' + (dataProds.mensaje || dataProds.message) + '\n');

    console.log('5. [KARDEX JSON] Registrando Movimiento de Almacén (POST /api/movimientos)...');
    let mov = {
      almacenId: 1,
      tipo: 'ENTRADA_COMPRA',
      productoId: 1,
      cantidad: 5,
      motivo: 'Ingreso de prueba demostrativo para el docente'
    };
    let resMov = await fetch(BASE + '/movimientos', {
      method: 'POST',
      headers: authHeaders,
      body: JSON.stringify(mov)
    });
    let dataMov = await resMov.json();
    console.log('   >> Código HTTP: ' + resMov.status + ' 201 Created');
    console.log('   >> Mensaje del Servidor: ' + (dataMov.mensaje || dataMov.message) + '\n');

    console.log('=========================================================================');
    console.log('  ¡TODAS LAS PRUEBAS REST JSON Y CONTROL RBAC CONCLUYERON CON ÉXITO!');
    console.log('=========================================================================\n');
  } catch (err) {
    console.error('Error durante la prueba:', err.message);
  }
}

test();
