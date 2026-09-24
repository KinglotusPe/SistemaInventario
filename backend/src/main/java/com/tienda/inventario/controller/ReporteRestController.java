package com.tienda.inventario.controller;

import com.tienda.inventario.report.JasperReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteRestController {

    private final JasperReportService jasperReportService;

    public ReporteRestController(JasperReportService jasperReportService) {
        this.jasperReportService = jasperReportService;
    }

    /**
     * Genera y exporta el reporte de stock crítico en formato PDF.
     * Requiere privilegio: REPORTE_DESCARGAR o Rol ADMINISTRADOR.
     * GET /api/reportes/stock-critico/pdf
     */
    @GetMapping("/stock-critico/pdf")
    @PreAuthorize("hasAuthority('REPORTE_DESCARGAR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> descargarReporteStockCriticoPdf() {
        try {
            byte[] pdfBytes = jasperReportService.generarReporteStockCriticoPdf();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // "inline" permite que el navegador lo renderice en pestaña directamente
            headers.setContentDispositionFormData("inline", "reporte_stock_critico.pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
