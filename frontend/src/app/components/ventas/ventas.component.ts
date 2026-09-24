import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { HasPermissionDirective } from '../../directives/has-permission.directive';
import { AuthService } from '../../services/auth.service';

export interface VentaItem {
  idVenta: number;
  tipoComprobante: string;
  serie: string;
  numeroCorrelativo: string;
  cliente: { nombresORazonSocial: string; numeroDocumento: string };
  usuario: { username: string; nombres: string };
  almacen: { nombre: string };
  metodoPago: string;
  subtotal: number;
  igv: number;
  total: number;
  estado: string;
  fechaVenta: string;
}

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective],
  template: `
    <div class="ventas-view">
      <div class="view-header">
        <div>
          <h2>💰 Módulo de Facturación y Ventas</h2>
          <p class="subtitle">Emisión de comprobantes, control de cobros y caja comercial</p>
        </div>
        <div class="actions">
          <button 
            *hasPermission="'VENTA_REGISTRAR'" 
            class="btn-emitir" 
            (click)="abrirModalNuevaVenta()">
            ➕ Emitir Nueva Venta
          </button>
        </div>
      </div>

      <!-- Banner de Privilegios del Usuario Actual -->
      <div class="role-alert">
        <span class="shield">🛡️</span>
        <div class="role-text">
          <strong>Permisos de Venta para su sesión:</strong>
          <span>
            Puede emitir: <b>{{ authService.hasPermission('VENTA_REGISTRAR') ? 'SÍ' : 'NO' }}</b> | 
            Puede anular: <b [class.danger]="!authService.hasPermission('VENTA_ANULAR')">{{ authService.hasPermission('VENTA_ANULAR') ? 'AUTORIZADO' : 'DENEGADO (Solo Supervisor/Admin)' }}</b>
          </span>
        </div>
      </div>

      <!-- Listado de Ventas -->
      <div class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>Comprobante</th>
              <th>Cliente</th>
              <th>Cajero / Usuario</th>
              <th>Almacén Despacho</th>
              <th>Método Pago</th>
              <th>Total (PEN)</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let v of ventas" [class.anulada]="v.estado === 'ANULADA'">
              <td>
                <span class="badge-comprobante" [class.factura]="v.tipoComprobante === 'FACTURA'">
                  {{ v.tipoComprobante }}
                </span>
                <span class="correlativo">{{ v.serie }}-{{ v.numeroCorrelativo }}</span>
              </td>
              <td>{{ v.cliente?.nombresORazonSocial || 'Cliente General' }}</td>
              <td>{{ v.usuario?.username }}</td>
              <td>{{ v.almacen?.nombre }}</td>
              <td>{{ v.metodoPago }}</td>
              <td class="total-cell">S/ {{ v.total | number:'1.2-2' }}</td>
              <td>
                <span class="status-badge" [class.emitida]="v.estado === 'EMITIDA'" [class.anulada]="v.estado === 'ANULADA'">
                  {{ v.estado }}
                </span>
              </td>
              <td>
                <!-- Botón Protegido por Directiva Granular RBAC -->
                <button 
                  *hasPermission="'VENTA_ANULAR'" 
                  class="btn-anular" 
                  [disabled]="v.estado === 'ANULADA'"
                  (click)="anularVenta(v.idVenta)">
                  🚫 Anular
                </button>
                <span *ngIf="!authService.hasPermission('VENTA_ANULAR')" class="sin-permiso" title="Solo Supervisores y Administradores tienen permiso de anular ventas">
                  🔒 No autorizado
                </span>
              </td>
            </tr>
            <tr *ngIf="ventas.length === 0">
              <td colspan="8" class="text-center py-4">No hay comprobantes de venta emitidos aún.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .ventas-view {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 16px;
    }
    .view-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    .view-header h2 { margin: 0; color: #0f172a; font-size: 1.5rem; }
    .subtitle { color: #64748b; font-size: 0.9rem; margin-top: 4px; }
    .btn-emitir {
      background-color: #16a34a;
      color: #fff;
      border: none;
      padding: 10px 18px;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      box-shadow: 0 2px 4px rgba(22, 163, 74, 0.2);
    }
    .btn-emitir:hover { background-color: #15803d; }
    .role-alert {
      background-color: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: 8px;
      padding: 12px 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
    }
    .shield { font-size: 1.4rem; }
    .role-text { font-size: 0.88rem; color: #1e40af; display: flex; flex-direction: column; gap: 2px; }
    .role-text .danger { color: #dc2626; }
    .table-card {
      background: #ffffff;
      border-radius: 10px;
      border: 1px solid #e2e8f0;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .data-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 0.88rem;
    }
    .data-table th {
      background-color: #f8fafc;
      color: #475569;
      font-weight: 600;
      padding: 12px 16px;
      border-bottom: 1px solid #e2e8f0;
    }
    .data-table td {
      padding: 14px 16px;
      border-bottom: 1px solid #f1f5f9;
      color: #334155;
    }
    .data-table tr.anulada {
      opacity: 0.6;
      background-color: #f8fafc;
    }
    .badge-comprobante {
      font-size: 0.72rem;
      font-weight: 700;
      padding: 2px 6px;
      border-radius: 4px;
      background-color: #0284c7;
      color: #fff;
      margin-right: 6px;
    }
    .badge-comprobante.factura { background-color: #7c3aed; }
    .correlativo { font-weight: 600; font-family: monospace; }
    .total-cell { font-weight: 700; color: #0f172a; }
    .status-badge {
      font-size: 0.75rem;
      font-weight: 700;
      padding: 3px 8px;
      border-radius: 12px;
    }
    .status-badge.emitida { background-color: #dcfce7; color: #15803d; }
    .status-badge.anulada { background-color: #fee2e2; color: #b91c1c; }
    .btn-anular {
      background-color: #ef4444;
      color: #fff;
      border: none;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
    }
    .btn-anular:hover:not(:disabled) { background-color: #dc2626; }
    .btn-anular:disabled { background-color: #cbd5e1; cursor: not-allowed; }
    .sin-permiso {
      font-size: 0.78rem;
      color: #94a3b8;
      font-style: italic;
    }
    .text-center { text-align: center; }
    .py-4 { padding-top: 16px; padding-bottom: 16px; }
  `]
})
export class VentasComponent implements OnInit {
  ventas: VentaItem[] = [];
  private apiUrl = 'http://localhost:8080/api/ventas';

  constructor(
    public authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.cargarVentas();
  }

  cargarVentas(): void {
    this.http.get<{ success: boolean; data: VentaItem[] }>(this.apiUrl).subscribe({
      next: res => {
        if (res.success) {
          this.ventas = res.data;
        }
      },
      error: err => console.error('Error al cargar ventas:', err)
    });
  }

  abrirModalNuevaVenta(): void {
    const totalVenta = (Math.random() * 200 + 50).toFixed(2);
    if (confirm(`¿Desea simular una venta rápida por S/ ${totalVenta}?`)) {
      const payload = {
        tipoComprobante: 'BOLETA',
        idCliente: 1,
        idAlmacen: 1,
        metodoPago: 'EFECTIVO',
        detalles: [
          { idProducto: 1, cantidad: 1, descuento: 0 }
        ]
      };

      this.http.post<{ success: boolean; message: string }>(this.apiUrl, payload).subscribe({
        next: () => {
          alert('¡Venta emitida exitosamente!');
          this.cargarVentas();
        },
        error: err => {
          alert(err.error?.message || 'Error al emitir venta');
        }
      });
    }
  }

  anularVenta(idVenta: number): void {
    if (confirm('¿Está seguro de anular este comprobante de pago? El stock será devuelto al almacén.')) {
      this.http.put<{ success: boolean; message: string }>(`${this.apiUrl}/${idVenta}/anular`, {
        motivo: 'Anulación solicitada por cliente en mostrador'
      }).subscribe({
        next: () => {
          alert('Comprobante anulado y stock reincorporado al almacén.');
          this.cargarVentas();
        },
        error: err => {
          alert(err.error?.message || 'Error al anular venta');
        }
      });
    }
  }
}
