import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {

  private readonly apiUrl = 'http://localhost:8080/api/reportes';

  constructor(private http: HttpClient) { }

  descargarReporteStockCriticoPdf(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/stock-critico/pdf`, {
      responseType: 'blob'
    });
  }

  /**
   * Abre el PDF generado por JasperReports directamente en una nueva pestaña del navegador.
   */
  abrirPdfEnPestana(): void {
    this.descargarReporteStockCriticoPdf().subscribe({
      next: (blob: Blob) => {
        const fileURL = URL.createObjectURL(blob);
        window.open(fileURL, '_blank');
      },
      error: (err) => {
        console.error('Error al generar el reporte PDF:', err);
        alert('Ocurrió un error al generar el reporte de stock crítico.');
      }
    });
  }
}
