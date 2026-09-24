import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../services/producto.service';
import { MovimientoService } from '../../services/movimiento.service';
import { AuthService } from '../../services/auth.service';
import { Producto } from '../../models/producto.model';
import { MovimientoRequest, MovimientoInventario } from '../../models/movimiento.model';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

@Component({
  selector: 'app-movimiento-inventario',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective],
  templateUrl: './movimiento-inventario.component.html',
  styleUrls: ['./movimiento-inventario.component.css']
})
export class MovimientoInventarioComponent implements OnInit {

  productos: Producto[] = [];
  historialMovimientos: MovimientoInventario[] = [];

  // Formulario transaccional con soporte multialmacén
  movimientoForm: MovimientoRequest = {
    idAlmacenOrigen: 1,
    idAlmacenDestino: 2,
    idProducto: 0,
    codigoTipo: 'ENTRADA_COMPRA',
    cantidad: 1,
    motivo: ''
  };

  productoSeleccionado: Producto | null = null;
  cargando: boolean = false;
  guardando: boolean = false;
  mensajeAlerta: string = '';
  tipoAlerta: 'success' | 'error' | 'warning' = 'success';

  constructor(
    private productoService: ProductoService,
    private movimientoService: MovimientoService,
    public authService: AuthService
  ) { }

  ngOnInit(): void {
    this.cargarProductos();
    this.cargarHistorial();
  }

  cargarProductos(): void {
    this.productoService.listarPaginado('', 0, 100).subscribe({
      next: (resp) => {
        this.productos = resp.data.content;
        if (this.productos.length > 0) {
          this.movimientoForm.idProducto = this.productos[0].idProducto!;
          this.alCambiarProducto();
        }
      },
      error: (err) => {
        this.notificar('Error al cargar catálogo de productos: ' + (err.error?.message || err.message), 'error');
      }
    });
  }

  cargarHistorial(): void {
    this.cargando = true;
    this.movimientoService.listarHistorial(0, 15).subscribe({
      next: (resp) => {
        this.historialMovimientos = resp.data.content;
        this.cargando = false;
      },
      error: (err) => {
        this.cargando = false;
        console.error('Error al cargar historial kardex:', err);
      }
    });
  }

  alCambiarProducto(): void {
    const id = Number(this.movimientoForm.idProducto);
    this.productoSeleccionado = this.productos.find(p => p.idProducto === id) || null;
  }

  get puedeAjustar(): boolean {
    return this.authService.hasPermission('INVENTARIO_AJUSTAR');
  }

  get puedeTrasladar(): boolean {
    return this.authService.hasPermission('INVENTARIO_TRASLADAR');
  }

  get esStockInsuficiente(): boolean {
    if (this.movimientoForm.codigoTipo === 'SALIDA_VENTA' || this.movimientoForm.codigoTipo === 'TRASLADO' || this.movimientoForm.codigoTipo === 'AJUSTE_NEGATIVO') {
      const stockActual = this.productoSeleccionado?.stockActual ?? 0;
      return (this.movimientoForm.cantidad || 0) > stockActual;
    }
    return false;
  }

  get formularioInvalido(): boolean {
    if (!this.movimientoForm.idProducto || !this.movimientoForm.codigoTipo || !this.movimientoForm.motivo?.trim()) {
      return true;
    }
    if ((this.movimientoForm.cantidad || 0) <= 0) {
      return true;
    }
    if (this.esStockInsuficiente) {
      return true;
    }
    // Validar permisos según el tipo seleccionado
    if ((this.movimientoForm.codigoTipo === 'AJUSTE_POSITIVO' || this.movimientoForm.codigoTipo === 'AJUSTE_NEGATIVO') && !this.puedeAjustar) {
      return true;
    }
    if (this.movimientoForm.codigoTipo === 'TRASLADO' && !this.puedeTrasladar) {
      return true;
    }
    return false;
  }

  registrarMovimiento(): void {
    if (this.formularioInvalido) {
      this.notificar('Por favor verifique los datos o sus permisos para esta operación.', 'warning');
      return;
    }

    this.guardando = true;
    this.movimientoService.registrar(this.movimientoForm).subscribe({
      next: (resp) => {
        this.notificar(`Movimiento ${resp.data.numeroMovimiento} registrado exitosamente y stock sincronizado.`, 'success');
        this.movimientoForm.motivo = '';
        this.movimientoForm.cantidad = 1;
        this.guardando = false;

        this.cargarProductos();
        this.cargarHistorial();
      },
      error: (err) => {
        this.guardando = false;
        const msg = err.error?.message || 'Error en la transacción de almacén.';
        this.notificar(msg, 'error');
      }
    });
  }

  notificar(msg: string, tipo: 'success' | 'error' | 'warning'): void {
    this.mensajeAlerta = msg;
    this.tipoAlerta = tipo;
    setTimeout(() => this.mensajeAlerta = '', 6000);
  }
}
