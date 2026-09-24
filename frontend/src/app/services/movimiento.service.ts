import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MovimientoRequest, MovimientoInventario } from '../models/movimiento.model';
import { ApiResponse, PageResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class MovimientoService {

  private readonly apiUrl = 'http://localhost:8080/api/movimientos';

  constructor(private http: HttpClient) { }

  registrar(request: MovimientoRequest): Observable<ApiResponse<MovimientoInventario>> {
    return this.http.post<ApiResponse<MovimientoInventario>>(this.apiUrl, request);
  }

  listarHistorial(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<MovimientoInventario>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<MovimientoInventario>>>(this.apiUrl, { params });
  }

  listarPorProducto(idProducto: number, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<MovimientoInventario>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<MovimientoInventario>>>(`${this.apiUrl}/producto/${idProducto}`, { params });
  }
}
