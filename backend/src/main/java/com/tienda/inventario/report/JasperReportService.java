package com.tienda.inventario.report;

import com.tienda.inventario.dto.StockCriticoDTO;
import com.tienda.inventario.service.IProductoService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JasperReportService {

    private final IProductoService productoService;

    public JasperReportService(IProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Compila la plantilla JRXML, inyecta la lista de productos con stock crítico
     * y genera el arreglo de bytes del documento PDF.
     */
    public byte[] generarReporteStockCriticoPdf() throws Exception {
        // 1. Obtener la fuente de datos mediante la lógica de negocio
        List<StockCriticoDTO> datosStock = productoService.obtenerProductosStockCritico();

        // 2. Cargar el archivo de plantilla JRXML desde el classpath
        ClassPathResource resource = new ClassPathResource("reports/stock_critico.jrxml");
        InputStream inputStream = resource.getInputStream();

        // 3. Compilar el reporte JRXML a formato binario JasperReport
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        // 4. Configurar parámetros del reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("TITULO_EMPRESA", "TIENDA TECNOLÓGICA S.A.C.");
        parameters.put("RUC_EMPRESA", "20551234567");
        parameters.put("FECHA_EMISION", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        parameters.put("TOTAL_REGISTROS", datosStock.size());

        // 5. Enlazar la colección de DTOs como JRDataSource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datosStock);

        // 6. Llenar el reporte con parámetros y datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 7. Exportar a arreglo de bytes en formato PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
