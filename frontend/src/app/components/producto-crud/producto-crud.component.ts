import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../services/producto.service';
import { ReporteService } from '../../services/reporte.service';
import { Producto } from '../../models/producto.model';

import { HasPermissionDirective } from '../../directives/has-permission.directive';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-producto-crud',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective],
  templateUrl: './producto-crud.component.html',
  styleUrls: ['./producto-crud.component.css']
})
export class ProductoCrudComponent implements OnInit {

  productos: Producto[] = [];
  filtro: string = '';
  paginaActual: number = 0;
  totalPaginas: number = 0;
  totalElementos: number = 0;
  tamanoPagina: number = 8;
  cargando: boolean = false;
  mensajeAlerta: string = '';
  tipoAlerta: 'success' | 'error' | 'info' = 'info';

  // Control de formulario modal
  mostrarModal: boolean = false;
  esModoEdicion: boolean = false;
  productoForm: Producto = this.inicializarProducto();

  constructor(
    private productoService: ProductoService,
    private reporteService: ReporteService,
    public authService: AuthService
  ) { }

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(pagina: number = 0): void {
    this.cargando = true;
    this.paginaActual = pagina;
    this.productoService.listarPaginado(this.filtro, this.paginaActual, this.tamanoPagina).subscribe({
      next: (resp) => {
        this.productos = resp.data.content;
        this.totalPaginas = resp.data.totalPages;
        this.totalElementos = resp.data.totalElements;
        this.cargando = false;
      },
      error: (err) => {
        this.mostrarMensaje('Error al cargar la lista de productos: ' + (err.error?.message || err.message), 'error');
        this.cargando = false;
      }
    });
  }

  buscar(): void {
    this.cargarProductos(0);
  }

  limpiarBusqueda(): void {
    this.filtro = '';
    this.cargarProductos(0);
  }

  abrirModalNuevo(): void {
    this.esModoEdicion = false;
    this.productoForm = this.inicializarProducto();
    this.mostrarModal = true;
  }

  abrirModalEditar(prod: Producto): void {
    this.esModoEdicion = true;
    this.productoForm = { ...prod };
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
  }

  guardarProducto(): void {
    if (!this.productoForm.codigoSku || !this.productoForm.nombre || !this.productoForm.precioUnitario) {
      this.mostrarMensaje('Por favor, complete todos los campos obligatorios.', 'error');
      return;
    }

    if (this.esModoEdicion && this.productoForm.idProducto) {
      this.productoService.actualizar(this.productoForm.idProducto, this.productoForm).subscribe({
        next: (resp) => {
          this.mostrarMensaje('Producto actualizado exitosamente.', 'success');
          this.cerrarModal();
          this.cargarProductos(this.paginaActual);
        },
        error: (err) => {
          this.mostrarMensaje('Error al actualizar: ' + (err.error?.message || err.message), 'error');
        }
      });
    } else {
      this.productoService.crear(this.productoForm).subscribe({
        next: (resp) => {
          this.mostrarMensaje('Producto registrado exitosamente con inventario inicial.', 'success');
          this.cerrarModal();
          this.cargarProductos(0);
        },
        error: (err) => {
          this.mostrarMensaje('Error al registrar: ' + (err.error?.message || err.message), 'error');
        }
      });
    }
  }

  eliminarProducto(id: number, nombre: string): void {
    if (confirm(`¿Confirma que desea deshabilitar el producto "${nombre}"?`)) {
      this.productoService.eliminar(id).subscribe({
        next: () => {
          this.mostrarMensaje(`Producto "${nombre}" dado de baja correctamente.`, 'success');
          this.cargarProductos(this.paginaActual);
        },
        error: (err) => {
          this.mostrarMensaje('Error al eliminar: ' + (err.error?.message || err.message), 'error');
        }
      });
    }
  }

  descargarReporteJasper(): void {
    this.reporteService.abrirPdfEnPestana();
  }

  esStockCritico(prod: Producto): boolean {
    return (prod.stockActual !== undefined && prod.stockMinimo !== undefined) 
      && (prod.stockActual <= prod.stockMinimo);
  }

  mostrarMensaje(mensaje: string, tipo: 'success' | 'error' | 'info'): void {
    this.mensajeAlerta = mensaje;
    this.tipoAlerta = tipo;
    setTimeout(() => {
      this.mensajeAlerta = '';
    }, 5000);
  }

  private inicializarProducto(): Producto {
    return {
      codigoSku: '',
      nombre: '',
      descripcion: '',
      idCategoria: 1,
      idProveedor: 1,
      precioUnitario: 0.0,
      stockMinimo: 5,
      stockActual: 0
    };
  }
}
