export interface Producto {
  idProducto?: number;
  codigoSku: string;
  nombre: string;
  descripcion?: string;
  idCategoria: number;
  nombreCategoria?: string;
  idProveedor: number;
  razonSocialProveedor?: string;
  precioUnitario: number;
  stockMinimo: number;
  stockActual?: number;
  estado?: boolean;
}

export interface StockCritico {
  idProducto: number;
  codigoSku: string;
  nombre: string;
  categoria: string;
  stockActual: number;
  stockMinimo: number;
  precioUnitario: number;
  deficit: number;
}
