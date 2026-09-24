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
6. [🎓 Balotario para Sustentación ante el Docente](#-6-balotario-para-sustentación-ante-el-docente-preguntas-y-respuestas-clave)

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
| 👑 **Administrador** | `admin` | **18 Privilegios Totales**: `PRODUCTO_CREAR`, `PRODUCTO_VER`, `PRODUCTO_EDITAR`, `PRODUCTO_ELIMINAR`, `STOCK_VER`, `INVENTARIO_ENTRADA`, `INVENTARIO_SALIDA`, `INVENTARIO_AJUSTAR`, `INVENTARIO_TRASLADAR`, `VENTA_REGISTRAR`, `VENTA_VER`, `VENTA_ANULAR`, `COMPRA_REGISTRAR`, `COMPRA_VER`, `COMPRA_APROBAR`, `REPORTE_DESCARGAR`, `USUARIO_ADMIN`, `AUDITORIA_VER` | **Control total del sistema**: Visualiza las 5 pestañas. Puede crear y dar de baja artículos, registrar cualquier movimiento, emitir y anular ventas, descargar PDFs y auditar la matriz RBAC. |
| 📋 **Supervisor de Almacén** | `supervisor` | `PRODUCTO_VER`, `PRODUCTO_EDITAR`, `STOCK_VER`, `INVENTARIO_ENTRADA`, `INVENTARIO_SALIDA`, `INVENTARIO_AJUSTAR`, `INVENTARIO_TRASLADAR`, `COMPRA_REGISTRAR`, `COMPRA_VER`, `COMPRA_APROBAR`, `REPORTE_DESCARGAR`, `AUDITORIA_VER` | **Gestión de almacén y reportes**: Visualiza Catálogo, Kardex y Reportes PDF. Puede hacer ajustes (+/-), traslados y descargar reportes PDF. No puede emitir ni anular ventas ni dar de baja productos. |
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

Aparecerá la **Pantalla de Logeo Dedicada**. Ingrese con cualquiera de las cuentas indicadas arriba (o haga clic en los botones de auto-llenado de credenciales) para experimentar la interfaz adaptada a ese rol.

---

## 🎓 6. Balotario para Sustentación ante el Docente (Preguntas y Respuestas Clave)

Si el docente o evaluador realiza preguntas técnicas sobre la implementación, aquí tienes las respuestas directas y fundamentadas en el código:

### 1. ¿Por qué las entidades están en el paquete `entity` y no en `model`?
> **Respuesta:** En la especificación **Jakarta Persistence (JPA)** y el estándar empresarial de Spring Boot, las clases anotadas con `@Entity` representan tablas mapeadas contra la base de datos relacional. El paquete `entity` expresa de forma canónica que estas clases pertenecen a la capa de persistencia ORM, reservándose `dto` para los objetos de transferencia de datos en los controladores.

### 2. ¿Cómo solucionaron el problema de concurrencia al vender o mover stock?
> **Respuesta:** Implementamos **Bloqueo Pesimista (Pessimistic Locking)** en `StockAlmacenRepository` mediante la anotación `@Lock(LockModeType.PESSIMISTIC_WRITE)`. Esto emite una cláusula SQL `SELECT ... FOR UPDATE` en MySQL, bloqueando la fila del producto para otros hilos concurrentes hasta que la transacción `@Transactional` actualiza el saldo y realiza el commit, garantizando que el stock jamás sea negativo.

### 3. ¿Cuál es la diferencia entre un Rol y un Permiso en su arquitectura RBAC?
> **Respuesta:** Un **Rol** (`ROLE_CAJERO_VENDEDOR`) es un perfil agrupador asignado a un usuario. Un **Permiso** (`VENTA_REGISTRAR`, `VENTA_ANULAR`) es un privilegio atómico que representa una acción específica. La relación es Muchos a Muchos (`roles_permisos`), lo que permite que la seguridad en los controladores REST esté protegida a nivel de privilegio con `@PreAuthorize("hasAuthority('VENTA_ANULAR')")` y no acoplada a un rol rígido.

### 4. ¿Por qué las contraseñas no se almacenan en texto plano en la base de datos?
> **Respuesta:** Se utiliza el algoritmo **BCrypt** (`BCryptPasswordEncoder`) con un factor de trabajo (*work factor*) de 10. BCrypt genera un hash irreversible con un *Salt* aleatorio integrado de 128 bits, protegiendo las credenciales frente a ataques de fuerza bruta o tablas *Rainbow*.

### 5. ¿Cómo funciona la autenticación con JWT en este proyecto?
> **Respuesta:** Es una arquitectura **Stateless** (sin estado en el servidor). Cuando el usuario envía sus credenciales a `/api/auth/login`, `AuthenticationManager` las valida. Luego, `JwtUtils` firma criptográficamente un token HMAC-SHA256 que incluye el username, roles y la lista de permisos del usuario. En cada petición subsecuente, `AuthTokenFilter` extrae el token de la cabecera `Authorization: Bearer <token>`, valida la firma y carga el `SecurityContext` en Spring Security.

### 6. ¿Qué ocurre si un usuario sin permisos (ej. Cajero) intenta invocar un endpoint protegido (ej. anular venta) por Postman o cURL?
> **Respuesta:** El filtro de Spring Security intercepta la petición antes de que llegue al controlador. Al evaluar `@PreAuthorize("hasAuthority('VENTA_ANULAR')")`, el manejador `AccessDeniedHandlerJwt` interrumpe la ejecución y retorna inmediatamente un código **HTTP 403 Forbidden** con un mensaje JSON explicativo: *"Acceso denegado: No cuenta con los privilegios requeridos"*.

### 7. ¿Por qué se utilizan DTOs en lugar de recibir directamente las entidades JPA en los controladores?
> **Respuesta:** Por tres razones de diseño empresarial:
> 1. **Seguridad (Mass Assignment Attack)**: Evita que clientes maliciosos modifiquen campos críticos (como `idUsuario` o `estado`).
> 2. **Desacoplamiento**: Si la base de datos cambia una columna, no rompemos el contrato de la API REST externa.
> 3. **Evitar bucles de serialización**: Previene excepciones `StackOverflowError` provocadas por relaciones bidireccionales circulares (`@OneToMany` / `@ManyToOne`) al convertir a JSON.

### 8. ¿Cómo genera JasperReports el reporte en PDF en tiempo de ejecución?
> **Respuesta:** En `JasperReportService`, se lee el archivo XML de plantilla `stock_critico.jrxml` mediante `ClassPathResource`. Se compila a un objeto `JasperReport` en memoria con `JasperCompileManager.compileReport()`. Luego, se inyectan los productos con stock crítico a través de `new JRBeanCollectionDataSource(datosStock)` y se renderiza con `JasperExportManager.exportReportToPdf()`, entregando el arreglo de bytes (`byte[]`) directamente al navegador como `application/pdf`.

### 9. ¿Qué garantiza la anotación `@Transactional` en el proceso de Venta?
> **Respuesta:** Garantiza las propiedades **ACID** (Atomicidad, Consistencia, Aislamiento y Durabilidad). Si se inserta la cabecera de la venta pero ocurre un error al descontar el inventario o al procesar un detalle, la anotación desencadena un *Rollback* automático que deshace todas las operaciones intermedias en MySQL, impidiendo inconsistencias contables o de stock.

### 10. ¿Cuántas tablas tiene la base de datos y cómo se relacionan con las entidades Java?
> **Respuesta:** La base de datos tiene **21 tablas en MySQL** y existen **19 clases `@Entity` en Java**. La diferencia de 2 tablas se debe a que las relaciones Muchos a Muchos (`usuarios_roles` y `roles_permisos`) son tablas intermedias relacionales puras, las cuales JPA mapea transparentemente mediante `@ManyToMany` con la anotación `@JoinTable`.
