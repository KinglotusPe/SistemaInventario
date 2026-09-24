package com.tienda.inventario.controller;

import com.tienda.inventario.dto.ApiResponse;
import com.tienda.inventario.dto.MovimientoRequestDTO;
import com.tienda.inventario.entity.MovimientoInventario;
import com.tienda.inventario.service.IMovimientoInventarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoInventarioRestController {

    private final IMovimientoInventarioService movimientoService;

    public MovimientoInventarioRestController(IMovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    /**
     * Registro transaccional de un movimiento físico (ENTRADA, SALIDA, AJUSTE, TRASLADO).
     * Requiere privilegios de inventario o rol ADMINISTRADOR.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('INVENTARIO_ENTRADA', 'INVENTARIO_SALIDA', 'INVENTARIO_AJUSTAR', 'INVENTARIO_TRASLADAR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<MovimientoInventario>> registrar(@Valid @RequestBody MovimientoRequestDTO request) {
        MovimientoInventario movimiento = movimientoService.registrarMovimiento(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Movimiento de almacén registrado con éxito y saldo actualizado", movimiento));
    }

    /**
     * Historial paginado de movimientos de inventario (Kardex).
     * Requiere privilegio: STOCK_VER o Rol ADMINISTRADOR.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_VER') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Page<MovimientoInventario>>> listar(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MovimientoInventario> pagina = movimientoService.listarMovimientos(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Historial de movimientos obtenido con éxito", pagina));
    }

    /**
     * Obtener detalle completo de un movimiento por su ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK_VER') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<MovimientoInventario>> obtenerPorId(@PathVariable Integer id) {
        MovimientoInventario movimiento = movimientoService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Movimiento localizado con éxito", movimiento));
    }
}

