package com.tienda.inventario.controller;

import com.tienda.inventario.dto.ApiResponse;
import com.tienda.inventario.dto.ProductoDTO;
import com.tienda.inventario.dto.StockCriticoDTO;
import com.tienda.inventario.service.IProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoRestController {

    private final IProductoService productoService;

    public ProductoRestController(IProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Listado paginado con soporte para filtrado por SKU o Nombre.
     * Requiere privilegio: PRODUCTO_VER o Rol ADMINISTRADOR
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTO_VER') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Page<ProductoDTO>>> listar(
            @RequestParam(required = false) String filtro,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        
        Page<ProductoDTO> pagina = productoService.buscarPorFiltro(filtro, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Listado de productos obtenido con éxito", pagina));
    }

    /**
     * Obtener producto por ID.
     * Requiere privilegio: PRODUCTO_VER o Rol ADMINISTRADOR
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTO_VER') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ProductoDTO>> obtenerPorId(@PathVariable Integer id) {
        ProductoDTO dto = productoService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Producto localizado con éxito", dto));
    }

    /**
     * Crear un nuevo producto e inicializar su inventario.
     * Requiere privilegio: PRODUCTO_CREAR o Rol ADMINISTRADOR
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCTO_CREAR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ProductoDTO>> crear(@Valid @RequestBody ProductoDTO dto) {
        ProductoDTO guardado = productoService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Producto creado exitosamente con inventario inicial", guardado));
    }

    /**
     * Carga masiva de productos mediante JSON (Array de productos o archivo .json).
     * Requiere privilegio: PRODUCTO_CREAR o Rol ADMINISTRADOR
     */
    @PostMapping("/importar-json")
    @PreAuthorize("hasAuthority('PRODUCTO_CREAR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<ProductoDTO>>> importarJson(@Valid @RequestBody List<ProductoDTO> productos) {
        List<ProductoDTO> importados = productoService.importarProductosJson(productos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Se importaron " + importados.size() + " productos exitosamente al catálogo desde JSON", importados));
    }

    /**
     * Actualizar datos comerciales de un producto existente.
     * Requiere privilegio: PRODUCTO_EDITAR o Rol ADMINISTRADOR
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTO_EDITAR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ProductoDTO>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoDTO dto) {
        ProductoDTO actualizado = productoService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado exitosamente", actualizado));
    }

    /**
     * Eliminación lógica (Soft Delete) del producto.
     * Requiere privilegio: PRODUCTO_ELIMINAR o Rol ADMINISTRADOR
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTO_ELIMINAR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        productoService.eliminarLogico(id);
        return ResponseEntity.ok(ApiResponse.ok("Producto dado de baja lógica exitosamente", null));
    }

    /**
     * Obtener productos en estado de stock crítico.
     * Requiere privilegio: STOCK_VER o Rol ADMINISTRADOR
     */
    @GetMapping("/stock-critico")
    @PreAuthorize("hasAuthority('STOCK_VER') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<StockCriticoDTO>>> listarStockCritico() {
        List<StockCriticoDTO> lista = productoService.obtenerProductosStockCritico();
        return ResponseEntity.ok(ApiResponse.ok("Productos en stock crítico obtenidos con éxito", lista));
    }
}
