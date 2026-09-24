# 🏬 Sistema Empresarial de Gestión de Inventario, Ventas y Compras (RBAC)

![Java](https://img.shields.io/badge/Java-21%20%2F%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.0-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![JasperReports](https://img.shields.io/badge/JasperReports-6.21.3-CB2026?style=for-the-badge&logo=adobeacrobatreader&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Tokens-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

Sistema web empresarial integral para la administración, control transaccional de existencias en almacenes, punto de venta (POS) con facturación electrónica y módulo de compras. Diseñado con una arquitectura desacoplada por capas bajo el estándar corporativo de **Spring Boot 3**, seguridad granular **RBAC con Spring Security 6**, control de concurrencia pesimista (**Pessimistic Locking**), motor de reportes en PDF con **JasperReports 6.21.3** y una interfaz interactiva con pantalla de logeo dedicada y visibilidad estricta según el rol del usuario.

---

## 📑 Tabla de Contenidos
1. [Stack Tecnológico (¿Qué herramientas usamos?)](#-1-stack-tecnológico-qué-herramientas-usamos)
2. [Arquitectura y Estructura del Código (¿Dónde está cada cosa?)](#-2-arquitectura-y-estructura-del-código-dónde-está-cada-cosa)
3. [Módulos y Reglas de Negocio (¿Qué hace el sistema?)](#-3-módulos-y-reglas-de-negocio-qué-hace-el-sistema)
4. [Control de Acceso RBAC y Cuentas de Acceso](#-4-control-de-acceso-rbac-y-cuentas-de-acceso)
5. [Guía de Instalación y Puesta en Marcha](#-5-guía-de-instalación-y-puesta-en-marcha)

---

## 🛠️ 1. Stack Tecnológico (¿Qué herramientas usamos?)

| Componente | Tecnología | Versión | Justificación Técnica |
| :--- | :--- | :--- | :--- |
| **Lenguaje Backend** | Java OpenJDK | 17 / 21 LTS | Tipado estricto, alto rendimiento transaccional y compatibilidad con Records y Virtual Threads. |
| **Framework Core** | Spring Boot | 3.2.5 | Inversión de Control (IoC), Inyección de Dependencias (DI) y servidor embebido Apache Tomcat 10. |
| **Seguridad** | Spring Security 6 + JJWT | 0.11.5 | Autenticación basada en tokens JWT (*Stateless*), encriptación BCrypt con salting y control granular con `@PreAuthorize`. |
| **Persistencia / ORM** | Spring Data JPA + Hibernate | 6.x | Mapeo Objeto-Relacional en el paquete estándar `entity`, consultas JPQL y bloqueo pesimista en base de datos. |
| **Base de Datos** | MySQL Server | 8.0+ | Motor InnoDB transaccional (ACID), 21 tablas normalizadas en 3FN, índices B-Tree y llaves foráneas. |
| **Reportes Oficiales** | JasperReports + OpenPDF | 6.21.3 | Motor de compilación dinámica en tiempo de ejecución a partir de plantillas XML (`.jrxml`) para exportar a PDF nativo. |
| **Frontend Web** | Single Page Application (SPA) | HTML5 / CSS3 / ES6 | Interfaz responsiva con diseño Dark Glassmorphism, interceptor de peticiones Fetch API con cabeceras Bearer JWT y control dinámico de visibilidad. |
| **Gestor de Construcción**| Apache Maven | 3.9+ | Gestión declarativa de dependencias y compilación automatizada (`pom.xml`). |

---

## 📂 2. Arquitectura y Estructura del Código (¿Dónde está cada cosa?)

El proyecto sigue estrictamente el patrón de **Arquitectura en Capas (Layered Architecture)** para desacoplar responsabilidades:

```text
d:/SistemaInventarioTienda/
├── backend/
│   ├── pom.xml                                      # Dependencias Maven (Spring Boot, Security, JPA, JasperReports)
│   └── src/main/
│       ├── java/com/tienda/inventario/
│       │   ├── controller/                          # CAPA CONTROLADORA (Endpoints REST API)
│       │   │   ├── AuthController.java              # Login y generación de tokens JWT (/api/auth/login)
│       │   │   ├── ProductoRestController.java      # CRUD de productos (/api/productos)
│       │   │   ├── MovimientoInventarioRestController.java # Operaciones de almacén (/api/movimientos)
│       │   │   ├── VentaRestController.java         # Facturación y anulación de ventas (/api/ventas)
│       │   │   ├── UsuarioRestController.java       # Auditoría de usuarios y roles (/api/usuarios)
│       │   │   └── ReporteRestController.java       # Exportación de reportes PDF (/api/reportes)
│       │   ├── dto/                                 # OBJETOS DE TRANSFERENCIA DE DATOS (Data Transfer Objects)
│       │   │   ├── ApiResponse.java                 # Formato unificado de respuesta JSON
│       │   │   ├── LoginRequest.java / JwtResponse.java # Carga útil de autenticación
│       │   │   ├── RegisterRequest.java             # Carga útil para registro de usuarios JSON
│       │   │   ├── ProductoDTO.java / StockCriticoDTO.java
│       │   │   ├── MovimientoRequestDTO.java / ItemMovimientoDTO.java
│       │   │   └── VentaRequestDTO.java / ItemVentaDTO.java
│       │   ├── entity/                              # CAPA DE ENTIDADES JPA (@Entity - 19 Clases)
│       │   │   ├── Usuario.java, Rol.java, Permiso.java # Tablas de seguridad RBAC
│       │   │   ├── Producto.java, Categoria.java, Marca.java, UnidadMedida.java # Catálogo
│       │   │   ├── Almacen.java, StockAlmacen.java  # Sedes y saldos físicos
│       │   │   ├── TipoMovimiento.java, MovimientoInventario.java, DetalleMovimiento.java # Kardex
│       │   │   ├── Proveedor.java, OrdenCompra.java, DetalleOrdenCompra.java # Abastecimiento
│       │   │   ├── Cliente.java, Venta.java, DetalleVenta.java # Ventas y facturación
│       │   │   └── AuditoriaSistema.java            # Registro de auditoría
│       │   ├── exception/                           # GESTIÓN GLOBAL DE EXCEPCIONES
│       │   │   ├── GlobalExceptionHandler.java      # Captura de 403 Forbidden, 401 Unauthorized, 404, 400
│       │   │   ├── ResourceNotFoundException.java   # Manejo de recursos inexistentes
│       │   │   └── StockInsuficienteException.java  # Regla de negocio cuando no hay saldo en almacén
│       │   ├── report/                              # SERVICIO DE JASPERREPORTS
│       │   │   └── JasperReportService.java         # Compilación de plantillas JRXML y generación de bytes PDF
│       │   ├── repository/                          # CAPA DE ACCESO A DATOS (Spring Data JPA)
│       │   │   ├── StockAlmacenRepository.java      # Consulta con @Lock(LockModeType.PESSIMISTIC_WRITE)
│       │   │   ├── ProductoRepository.java          # Búsqueda con filtro y detección de stock crítico
│       │   │   └── VentaRepository.java             # Generación de correlativos y consultas
│       │   ├── security/                            # CAPA DE SEGURIDAD (Spring Security 6)
│       │   │   ├── SecurityConfig.java              # Configuración de filtros, CORS y protección de endpoints
│       │   │   ├── jwt/                             # Generación y validación de tokens Bearer JWT
│       │   │   └── service/                         # CustomUserDetailsService & UserDetailsImpl
│       │   ├── service/                             # CAPA DE LÓGICA DE NEGOCIO (Interfaces)
│       │   │   ├── IProductoService.java, IMovimientoInventarioService.java, IVentaService.java
│       │   │   └── impl/                            # IMPLEMENTACIONES TRANSACCIONALES (@Transactional)
│       │   │       ├── ProductoServiceImpl.java
│       │   │       ├── MovimientoInventarioServiceImpl.java
│       │   │       └── VentaServiceImpl.java
│       │   └── SistemaInventarioApplication.java    # Clase principal ejecutable Spring Boot
│       └── resources/
│           ├── application.properties               # Parámetros MySQL, puertos y firma JWT
│           ├── reports/
│           │   └── stock_critico.jrxml              # Plantilla XML oficial del reporte JasperReports
│           └── static/
│               └── index.html                       # Frontend SPA con Login y Vistas por Rol
├── database/
│   └── schema_inventario.sql                        # Script SQL DDL (21 tablas) + Datos Semilla
├── frontend/                                        # Referencia de código Angular/TypeScript
└── README.md                                        # Documentación maestra del proyecto
```

---

## ⚙️ 3. Módulos y Reglas de Negocio (¿Qué hace el sistema?)

### 1. Control de Concurrencia y Stock (Pessimistic Locking)
* **Problema resuelto**: Evita la condición de carrera (*Race Condition*) en la que dos cajeros o almaceneros consultan al mismo tiempo un producto con solo 1 unidad disponible y ambos intentan registrar la salida, dejando el stock en negativo (-1).
* **Implementación técnica**: En `StockAlmacenRepository`, el método está anotado con `@Lock(LockModeType.PESSIMISTIC_WRITE)`, lo que se traduce en SQL a un bloqueo exclusivo `SELECT ... FOR UPDATE` a nivel de fila en la tabla `stock_almacen` de MySQL hasta que la transacción concluye.

### 2. Semáforo de Existencias y Stock Crítico
* **Fórmula**: Si `stockActual <= stockMinimo`, el sistema activa el semáforo en **🔴 CRÍTICO** y el artículo califica automáticamente para el reporte oficial de reposición urgente.
* Si `stockActual > stockMinimo`, se cataloga como **🟢 ÓPTIMO**.

### 3. Punto de Venta (POS) y Facturación Atómica
* Permite emitir **Boletas de Venta**, **Facturas Electrónicas** y **Notas de Venta**.
* Genera la numeración correlativa automática (Serie y Correlativo, ej. `B001-00000001`).
* Realiza el descuento físico del inventario dentro de la misma transacción `@Transactional`. Si alguna validación falla, se aplica *Rollback* automático.

### 4. Anulación de Comprobantes con Devolución de Stock
* Exclusivo del Administrador. Al anular un comprobante, el sistema cambia su estado a `ANULADA` y **reincorpora físicamente las unidades vendidas al almacén de despacho original**.

### 5. Motor de Reportes PDF con JasperReports 6.21.3
* Genera documentos ejecutivos en formato estándar PDF compilando directamente la plantilla `stock_critico.jrxml` sin requerir software externo en el cliente. Incluye membrete corporativo, fecha de emisión, cálculo de déficit de reposición y firma digital.

### 6. Gestión e Importación de Usuarios mediante JSON (Restringido al Administrador - RBAC)
* **Arquitectura de Seguridad Empresarial**: En un sistema de inventario y facturación empresarial **no se permite el autorregistro público**, ya que cualquier usuario anónimo podría asignarse privilegios de Administrador. Por ello, la creación de usuarios está estrictamente protegida y restringida a usuarios autenticados con rol `ROLE_ADMINISTRADOR` o permiso `USUARIO_ADMIN`.
* **Endpoints Protegidos**:
  * `POST /api/usuarios` (Registro individual de usuario mediante JSON).
  * `POST /api/usuarios/importar-json` (Importación masiva de usuarios en lote mediante Array JSON).
* **Cabeceras Requeridas**:
  * `Authorization: Bearer <TOKEN_JWT_DEL_ADMINISTRADOR>`
  * `Content-Type: application/json`
* **Carga Útil JSON Individual (Ejemplo)**:
  ```json
  {
    "username": "cajero.nuevo",
    "password": "Password123*",
    "nombres": "Ana",
    "apellidos": "Torres",
    "email": "ana.torres@tienda.pe",
    "telefono": "+51 987654321",
    "rol": "ROLE_CAJERO_VENDEDOR"
  }
  ```
* **Carga Útil Masiva (Batch JSON)**: Permite subir un archivo `.json` o un array JSON con múltiples usuarios para creación inmediata.
* **Mecanismos y Reglas de Negocio aplicadas**:
  1. **Validación de unicidad**: Verifica con `usuarioRepository.existsByUsername()` y `existsByEmail()`. Si ya existen, retorna HTTP `400 Bad Request` indicando el conflicto exacto.
  2. **Criptografía de Contraseñas**: La clave viaja en el JSON y es encriptada con algoritmo hash irreversible **BCrypt** antes de persistir en base de datos. La entidad `Usuario` tiene `@JsonIgnore` sobre el campo `password` para jamás exponer el hash en las respuestas JSON.
  3. **Asignación RBAC Dinámica**: Vincula automáticamente la entidad `Rol` seleccionada (`ROLE_ADMINISTRADOR`, `ROLE_SUPERVISOR_ALMACEN`, `ROLE_OPERADOR_ALMACEN` o `ROLE_CAJERO_VENDEDOR`) con sus respectivos permisos granulares en MySQL.
  4. **Panel Dedicado en Frontend**: El Administrador dispone de la pestaña **"👥 Gestión de Usuarios"** para listar cuentas, crear con modal de previsualización JSON interactiva y botón de subida de archivos `.json`.

### 7. Carga e Importación Masiva de Productos mediante JSON
* **Endpoint Protegido**: `POST /api/productos/importar-json`
* **Permiso requerido**: `@PreAuthorize("hasAuthority('PRODUCTO_CREAR') or hasRole('ADMINISTRADOR')")`
* **Carga Útil JSON (Array de Productos de Ejemplo)**:
  ```json
  [
    {
      "codigoSku": "SKU-MON-001",
      "nombre": "Monitor Gamer Asus ROG 27\" 165Hz",
      "descripcion": "Panel Fast IPS 1ms QHD HDR400",
      "precioVenta": 1399.00,
      "precioCompra": 950.00,
      "stockMinimo": 3,
      "stockActual": 12,
      "codigoBarras": "7759876543210",
      "categoriaId": 1
    }
  ]
  ```
* **Lógica de Negocio (Upsert Inteligente)**: Si el SKU ya existe en la base de datos, actualiza su nombre, precios y stock mínimo. Si no existe, crea el producto y genera automáticamente su registro de stock inicial en el almacén principal.
* **Interfaz de Usuario**: Botón **"📥 Importar Productos (JSON)"** en la vista de Catálogo con selector de archivos `.json` y editor de texto con botón de autollenado de prueba.

### 8. Registro y Operaciones de Almacén / Kardex mediante JSON
* **Endpoints Protegidos**:
  * `POST /api/movimientos` (Operación individual de Kardex mediante JSON).
  * `POST /api/movimientos/importar-json` (Lote de movimientos de almacén mediante JSON).
* **Permisos requeridos**: `INVENTARIO_ENTRADA`, `INVENTARIO_SALIDA`, `INVENTARIO_AJUSTAR`, `INVENTARIO_TRASLADAR` o `ADMINISTRADOR`.
* **Carga Útil JSON de Movimiento (Ejemplo Entrada por Compra)**:
  ```json
  {
    "almacenId": 1,
    "tipo": "ENTRADA_COMPRA",
    "productoId": 1,
    "cantidad": 10,
    "motivo": "Recepción de Orden de Compra OC-2026-001"
  }
  ```
* **Lógica de Negocio**: Ejecuta bloqueo pesimista (`SELECT ... FOR UPDATE`), verifica existencias para evitar inconsistencias de inventario, actualiza saldos en `stock_almacen` y genera el asiento contable en el Kardex físico.
* **Interfaz de Usuario**: Botón **"⚡ Movimiento con JSON"** en la vista de Kardex con selector de presets automáticos (Entrada por Compra, Salida por Merma, Ajuste por Inventario) y subida de archivos `.json`.

---

## 🛡️ 4. Control de Acceso RBAC y Cuentas de Acceso

El sistema implementa **RBAC Granular (Role-Based Access Control)** con separación estricta entre **Roles** y **Privilegios Atómicos**:

```
[Usuario] ──── (N:M) ────> [Rol] ──── (N:M) ────> [Permisos Granulares]
```

### Cuentas de Acceso de Prueba
> 🔑 **Contraseña universal para todas las cuentas:** `Admin123*`

| Rol | Usuario | Privilegios Granulares Asignados | ¿Qué puede ver y hacer en el sistema? |
| :--- | :--- | :--- | :--- |
| 👑 **Administrador** | `admin` | **18 Privilegios Totales**: `PRODUCTO_CREAR`, `PRODUCTO_VER`, `PRODUCTO_EDITAR`, `PRODUCTO_ELIMINAR`, `STOCK_VER`, `INVENTARIO_ENTRADA`, `INVENTARIO_SALIDA`, `INVENTARIO_AJUSTAR`, `INVENTARIO_TRASLADAR`, `VENTA_REGISTRAR`, `VENTA_VER`, `VENTA_ANULAR`, `COMPRA_REGISTRAR`, `COMPRA_VER`, `COMPRA_APROBAR`, `REPORTE_DESCARGAR`, `USUARIO_ADMIN`, `AUDITORIA_VER` | **Control total del sistema**: Acceso a las 5 pestañas principales más la pestaña exclusiva de **Gestión de Usuarios**. Puede crear y dar de baja usuarios, importar productos en lote vía JSON, registrar movimientos JSON, emitir y anular comprobantes, descargar reportes PDF y auditar permisos. |
| 📋 **Supervisor de Almacén** | `supervisor` | `PRODUCTO_VER`, `PRODUCTO_EDITAR`, `STOCK_VER`, `INVENTARIO_ENTRADA`, `INVENTARIO_SALIDA`, `INVENTARIO_AJUSTAR`, `INVENTARIO_TRASLADAR`, `COMPRA_REGISTRAR`, `COMPRA_VER`, `COMPRA_APROBAR`, `REPORTE_DESCARGAR`, `AUDITORIA_VER` | **Gestión de almacén y reportes**: Visualiza Catálogo, Kardex y Reportes PDF. Puede hacer ajustes (+/-), traslados y descargar reportes PDF. No puede emitir ni anular ventas, ni crear usuarios. |
| 📦 **Operador de Almacén** | `almacenero` | `PRODUCTO_VER`, `STOCK_VER`, `INVENTARIO_ENTRADA`, `INVENTARIO_SALIDA` | **Operaciones básicas de bodega**: Visualiza Kardex y Catálogo. Solo puede registrar entradas por compra y salidas por despacho. Opciones de Ajuste, Traslado, Ventas y Reportes ocultas. |
| 💳 **Cajero Vendedor** | `cajero` | `PRODUCTO_VER`, `STOCK_VER`, `VENTA_REGISTRAR`, `VENTA_VER` | **Punto de Venta**: Visualiza Ventas (POS) y Catálogo de precios. Emite boletas y facturas. Botón de anular venta oculto (denegado). No tiene acceso a Kardex ni Reportes. |

---

## 🚀 5. Guía de Instalación y Puesta en Marcha

### Requisitos Previos
* **Java Development Kit (JDK)**: Versión 17 o 21 instalada (`java -version`).
* **MySQL Server**: Versión 8.0 o superior corriendo en el puerto 3306.
* **Apache Maven**: 3.8+ (o el Maven incluido en `.tools/`).

### Paso 1: Configurar la Base de Datos
En MySQL Workbench o terminal MySQL, ejecute el script:
```sql
source database/schema_inventario.sql;
```
Este script creará la base de datos `db_tienda_inventario` con sus 21 tablas relacionales y cargará los usuarios iniciales, roles, permisos y catálogo de prueba.

### Paso 2: Ejecutar el Servidor Spring Boot
Abra una terminal en la carpeta `backend` y ejecute:
```bash
mvn clean spring-boot:run
```
El servidor compilará las 19 entidades, conectará con MySQL e iniciará Tomcat en el puerto **`8080`**.

### Paso 3: Abrir la Aplicación en el Navegador
Ingrese a:
👉 **http://localhost:8080/**

1. **Pantalla de Inicio de Sesión**:
   * Ingrese con cualquiera de las cuentas indicadas arriba (o haga clic en los chips rápidos: `👑 Admin`, `📋 Supervisor`, `📦 Almacenero`, `💳 Cajero`).
   * La pantalla explica que la creación de cuentas es una función protegida exclusiva del Administrador.
2. **Si inicia sesión como Administrador (`admin`)**:
   * Verá la pestaña **"👥 Gestión de Usuarios"**:
     * Directorio completo de usuarios activos con sus roles y correos.
     * Botón **"+ Nuevo Usuario (JSON)"** con previsualización reactiva de JSON y botón de prueba.
     * Botón **"📁 Subir Lote (.json)"** para importar múltiples usuarios a la vez.
   * En la pestaña **"📦 Catálogo"**:
     * Botón **"📥 Importar Productos (JSON)"** para cargar catálogos desde archivo `.json` o texto.
   * En la pestaña **"📋 Kardex y Movimientos"**:
     * Botón **"⚡ Movimiento con JSON"** para registrar entradas, salidas o ajustes enviando la carga JSON directamente con validación de concurrencia pesimista.

