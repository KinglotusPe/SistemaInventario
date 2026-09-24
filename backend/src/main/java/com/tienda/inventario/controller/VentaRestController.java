package com.tienda.inventario.controller;

import com.tienda.inventario.dto.ApiResponse;
import com.tienda.inventario.dto.VentaRequestDTO;
import com.tienda.inventario.entity.Venta;
import com.tienda.inventario.service.IVentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaRestController {

    private final IVentaService ventaService;

    public VentaRestController(IVentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Registrar una nueva venta y emitir comprobante con descuento de stock atómico.
     * Requiere privilegio: VENTA_REGISTRAR o Rol ADMINISTRADOR.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('VENTA_REGISTRAR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Venta>> registrar(@Valid @RequestBody VentaRequestDTO request) {
        Venta venta = ventaService.registrarVenta(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Comprobante de venta emitido exitosamente", venta));
    }

    /**
     * Listado general de ventas y comprobantes.
     * Requiere privilegio: VENTA_VER o Rol ADMINISTRADOR.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('VENTA_VER') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<Venta>>> listar() {
        List<Venta> ventas = ventaService.listarVentas();
        return ResponseEntity.ok(ApiResponse.ok("Listado de ventas obtenido con éxito", ventas));
    }

    /**
     * Obtener comprobante con detalle de ítems por ID.
     * Requiere privilegio: VENTA_VER o Rol ADMINISTRADOR.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENTA_VER') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Venta>> obtenerPorId(@PathVariable Integer id) {
        Venta venta = ventaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Comprobante localizado con éxito", venta));
    }

    /**
     * Anulación de un comprobante de venta emitido con reincorporación física al stock.
     * Requiere privilegio: VENTA_ANULAR o Rol ADMINISTRADOR (Cajeros denegados).
     */
    @PutMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('VENTA_ANULAR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Venta>> anular(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = (body != null && body.containsKey("motivo")) ? body.get("motivo") : "Anulación solicitada";
        Venta anulada = ventaService.anularVenta(id, motivo);
        return ResponseEntity.ok(ApiResponse.ok("Comprobante de pago anulado exitosamente y stock reincorporado", anulada));
    }
}

