import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductoCrudComponent } from './components/producto-crud/producto-crud.component';
import { MovimientoInventarioComponent } from './components/movimiento-inventario/movimiento-inventario.component';
import { LoginComponent } from './components/login/login.component';
import { VentasComponent } from './components/ventas/ventas.component';
import { AuthService } from './services/auth.service';
import { HasPermissionDirective } from './directives/has-permission.directive';
import { HasRoleDirective } from './directives/has-role.directive';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    ProductoCrudComponent,
    MovimientoInventarioComponent,
    LoginComponent,
    VentasComponent,
    HasPermissionDirective,
    HasRoleDirective
  ],
  template: `
    <div class="app-shell">
      <!-- Navbar con estado de autenticación y RBAC -->
      <nav class="navbar">
        <div class="nav-brand">
          <span class="brand-icon">🏬</span>
          <span class="brand-text">Sistema de Inventario & Ventas</span>
          <span class="badge-tech">Spring Boot + RBAC + Angular</span>
        </div>

        <!-- Pestañas de Navegación por Privilegios -->
        <div class="nav-tabs" *ngIf="authService.isAuthenticated()">
          <button 
            *hasPermission="'PRODUCTO_VER'"
            class="tab-btn" 
            [class.active]="tabActiva === 'productos'" 
            (click)="tabActiva = 'productos'">
            📦 Catálogo
          </button>
          
          <button 
            *hasPermission="'STOCK_VER'"
            class="tab-btn" 
            [class.active]="tabActiva === 'movimientos'" 
            (click)="tabActiva = 'movimientos'">
            🔄 Kardex & Movimientos
          </button>

          <button 
            *hasPermission="'VENTA_VER'"
            class="tab-btn" 
            [class.active]="tabActiva === 'ventas'" 
            (click)="tabActiva = 'ventas'">
            💰 Ventas
          </button>
        </div>

        <!-- Perfil y Cierre de Sesión -->
        <div class="user-session" *ngIf="authService.isAuthenticated(); else noAuth">
          <div class="user-info">
            <span class="user-name">{{ authService.currentUserValue?.nombres }}</span>
            <div class="user-roles">
              <span *ngFor="let rol of authService.currentUserValue?.roles" class="role-chip">
                {{ rol.replace('ROLE_', '') }}
              </span>
            </div>
          </div>
          <button class="btn-logout" (click)="authService.logout()" title="Cerrar sesión actual">
            🚪 Salir
          </button>
        </div>

        <ng-template #noAuth>
          <div class="auth-status">
            <span class="badge-guest">Modo No Autenticado</span>
          </div>
        </ng-template>
      </nav>

      <!-- Panel de Detalles RBAC (Desplegable informativo) -->
      <div class="rbac-bar" *ngIf="authService.isAuthenticated()">
        <div class="rbac-info">
          <span class="key">🔑 Privilegios activos en token JWT ({{ authService.currentUserValue?.permisos?.length }}):</span>
          <span class="perm-list">
            <span *ngFor="let p of authService.currentUserValue?.permisos" class="perm-tag">{{ p }}</span>
          </span>
        </div>
      </div>

      <!-- Contenido Principal Dinámico -->
      <main class="main-content">
        <div *ngIf="!authService.isAuthenticated()">
          <app-login></app-login>
        </div>

        <div *ngIf="authService.isAuthenticated()">
          <app-producto-crud *ngIf="tabActiva === 'productos'"></app-producto-crud>
          <app-movimiento-inventario *ngIf="tabActiva === 'movimientos'"></app-movimiento-inventario>
          <app-ventas *ngIf="tabActiva === 'ventas'"></app-ventas>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .app-shell {
      min-height: 100vh;
      background-color: #f1f5f9;
      font-family: 'Segoe UI', Roboto, sans-serif;
    }
    .navbar {
      background-color: #0f172a;
      color: #ffffff;
      padding: 0 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 64px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.15);
    }
    .nav-brand {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 1.1rem;
      font-weight: 700;
    }
    .brand-icon { font-size: 1.4rem; }
    .badge-tech {
      background-color: #1e293b;
      color: #38bdf8;
      font-size: 0.72rem;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 4px;
      border: 1px solid #334155;
    }
    .nav-tabs {
      display: flex;
      gap: 8px;
    }
    .tab-btn {
      background: transparent;
      border: none;
      color: #94a3b8;
      font-size: 0.9rem;
      font-weight: 600;
      padding: 8px 14px;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .tab-btn:hover {
      color: #ffffff;
      background-color: #1e293b;
    }
    .tab-btn.active {
      color: #ffffff;
      background-color: #2563eb;
    }
    .user-session {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .user-info {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }
    .user-name {
      font-size: 0.85rem;
      font-weight: 600;
      color: #f8fafc;
    }
    .role-chip {
      background-color: #059669;
      color: #ffffff;
      font-size: 0.68rem;
      font-weight: 700;
      padding: 1px 6px;
      border-radius: 4px;
      margin-left: 4px;
    }
    .btn-logout {
      background-color: #334155;
      color: #f1f5f9;
      border: 1px solid #475569;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 0.82rem;
      cursor: pointer;
      font-weight: 600;
      transition: background-color 0.2s;
    }
    .btn-logout:hover {
      background-color: #dc2626;
      border-color: #b91c1c;
    }
    .badge-guest {
      background-color: #334155;
      color: #cbd5e1;
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 0.78rem;
    }
    .rbac-bar {
      background-color: #1e293b;
      padding: 6px 24px;
      color: #cbd5e1;
      font-size: 0.76rem;
      border-bottom: 1px solid #334155;
    }
    .rbac-info {
      display: flex;
      align-items: center;
      gap: 8px;
      overflow-x: auto;
      white-space: nowrap;
    }
    .key {
      font-weight: 600;
      color: #38bdf8;
    }
    .perm-list {
      display: flex;
      gap: 6px;
    }
    .perm-tag {
      background-color: #0f172a;
      border: 1px solid #334155;
      padding: 1px 6px;
      border-radius: 4px;
      color: #a5f3fc;
      font-family: monospace;
      font-size: 0.7rem;
    }
    .main-content {
      padding: 20px 0;
    }
  `]
})
export class AppComponent {
  tabActiva: 'productos' | 'movimientos' | 'ventas' = 'productos';

  constructor(public authService: AuthService) {}
}
