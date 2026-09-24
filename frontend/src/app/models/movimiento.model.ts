import { Producto } from './producto.model';

export type TipoMovimientoCodigo = 
  | 'ENTRADA_COMPRA' 
  | 'SALIDA_VENTA' 
  | 'AJUSTE_POSITIVO' 
  | 'AJUSTE_NEGATIVO' 
  | 'TRASLADO'
  | 'ENTRADA'
  | 'SALIDA'
  | 'AJUSTE';

export interface ItemMovimiento {
  idProducto: number;
  cantidad: number;
  costoUnitario?: number;
}

export interface MovimientoRequest {
  idAlmacenOrigen: number;
  idAlmacenDestino?: number;
  codigoTipo: string;
  motivo: string;
  idProducto?: number;
  cantidad?: number;
  items?: ItemMovimiento[];
}

export interface TipoMovimiento {
  idTipo: number;
  codigo: string;
  descripcion: string;
}

export interface Almacen {
  idAlmacen: number;
  codigo: string;
  nombre: string;
  ciudad: string;
}

export interface DetalleMovimiento {
  idDetalleMov: number;
  producto: Producto;
  cantidad: number;
  costoUnitario: number;
  subtotal: number;
}

export interface MovimientoInventario {
  idMovimiento: number;
  numeroMovimiento: string;
  tipoMovimiento: TipoMovimiento;
  almacenOrigen: Almacen;
  almacenDestino?: Almacen;
  motivo: string;
  fechaMovimiento: string;
  detalles: DetalleMovimiento[];
}
