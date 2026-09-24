import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto, StockCritico } from '../models/producto.model';
import { ApiResponse, PageResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {

  private readonly apiUrl = 'http://localhost:8080/api/productos';

  constructor(private http: HttpClient) { }

  listarPaginado(filtro: string = '', page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Producto>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filtro.trim()) {
      params = params.set('filtro', filtro.trim());
    }

    return this.http.get<ApiResponse<PageResponse<Producto>>>(this.apiUrl, { params });
  }

  obtenerPorId(id: number): Observable<ApiResponse<Producto>> {
    return this.http.get<ApiResponse<Producto>>(`${this.apiUrl}/${id}`);
  }

  crear(producto: Producto): Observable<ApiResponse<Producto>> {
    return this.http.post<ApiResponse<Producto>>(this.apiUrl, producto);
  }

  actualizar(id: number, producto: Producto): Observable<ApiResponse<Producto>> {
    return this.http.put<ApiResponse<Producto>>(`${this.apiUrl}/${id}`, producto);
  }

  eliminar(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  obtenerStockCritico(): Observable<ApiResponse<StockCritico[]>> {
    return this.http.get<ApiResponse<StockCritico[]>>(`${this.apiUrl}/stock-critico`);
  }
}
