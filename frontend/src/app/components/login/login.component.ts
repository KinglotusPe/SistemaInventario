import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo-circle">🏬</div>
          <h2>Acceso al Sistema Empresarial</h2>
          <p class="subtitle">Gestión de Inventario, Ventas y Compras con RBAC Granular</p>
        </div>

        <form (ngSubmit)="onLogin()" #loginForm="ngForm" class="login-form">
          <div *ngIf="errorMessage" class="alert alert-error">
            <span>⚠️ {{ errorMessage }}</span>
          </div>

          <div class="form-group">
            <label for="username">Usuario o Credencial</label>
            <input
              type="text"
              id="username"
              name="username"
              [(ngModel)]="username"
              required
              class="form-control"
              placeholder="Ingrese su usuario..."
            />
          </div>

          <div class="form-group">
            <label for="password">Contraseña</label>
            <input
              type="password"
              id="password"
              name="password"
              [(ngModel)]="password"
              required
              class="form-control"
              placeholder="••••••••"
            />
          </div>

          <button type="submit" class="btn-primary" [disabled]="loading || !username || !password">
            <span *ngIf="!loading">🔐 Iniciar Sesión</span>
            <span *ngIf="loading">Autenticando...</span>
          </button>
        </form>

        <div class="presets-section">
          <h4>🧪 Acceso Rápido por Rol (Demostración de Permisos):</h4>
          <div class="presets-grid">
            <button class="preset-btn admin" (click)="cargarCredenciales('admin', 'Admin123*')">
              <strong>👑 Administrador</strong>
              <small>Acceso total y gestión RBAC</small>
            </button>
            <button class="preset-btn supervisor" (click)="cargarCredenciales('supervisor', 'Admin123*')">
              <strong>📋 Supervisor Almacén</strong>
              <small>Ajustes, Catálogo y Traslados</small>
            </button>
            <button class="preset-btn almacenero" (click)="cargarCredenciales('almacenero', 'Admin123*')">
              <strong>📦 Operador Almacén</strong>
              <small>Solo ingresos/salidas físicas</small>
            </button>
            <button class="preset-btn cajero" (click)="cargarCredenciales('cajero', 'Admin123*')">
              <strong>💳 Cajero Vendedor</strong>
              <small>Ventas y emisión de tickets</small>
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 24px;
    }
    .login-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 36px;
      width: 100%;
      max-width: 520px;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
      border: 1px solid #e2e8f0;
    }
    .login-header {
      text-align: center;
      margin-bottom: 24px;
    }
    .logo-circle {
      font-size: 2.5rem;
      margin-bottom: 12px;
    }
    .login-header h2 {
      margin: 0;
      color: #0f172a;
      font-size: 1.45rem;
    }
    .subtitle {
      color: #64748b;
      font-size: 0.88rem;
      margin-top: 6px;
    }
    .alert-error {
      background-color: #fef2f2;
      border: 1px solid #fecaca;
      color: #b91c1c;
      padding: 10px 14px;
      border-radius: 8px;
      margin-bottom: 16px;
      font-size: 0.88rem;
    }
    .form-group {
      margin-bottom: 16px;
    }
    .form-group label {
      display: block;
      font-size: 0.85rem;
      font-weight: 600;
      color: #334155;
      margin-bottom: 6px;
    }
    .form-control {
      width: 100%;
      padding: 10px 14px;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.95rem;
      box-sizing: border-box;
      transition: border-color 0.2s;
    }
    .form-control:focus {
      outline: none;
      border-color: #2563eb;
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
    }
    .btn-primary {
      width: 100%;
      background-color: #2563eb;
      color: #ffffff;
      padding: 12px;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: background-color 0.2s;
    }
    .btn-primary:hover:not(:disabled) {
      background-color: #1d4ed8;
    }
    .btn-primary:disabled {
      background-color: #94a3b8;
      cursor: not-allowed;
    }
    .presets-section {
      margin-top: 28px;
      padding-top: 20px;
      border-top: 1px dashed #e2e8f0;
    }
    .presets-section h4 {
      font-size: 0.85rem;
      color: #475569;
      margin: 0 0 12px 0;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .presets-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px;
    }
    .preset-btn {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      padding: 10px 12px;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      background: #f8fafc;
      cursor: pointer;
      text-align: left;
      transition: all 0.2s;
    }
    .preset-btn:hover {
      background: #f1f5f9;
      border-color: #cbd5e1;
      transform: translateY(-1px);
    }
    .preset-btn strong {
      font-size: 0.85rem;
      color: #0f172a;
    }
    .preset-btn small {
      font-size: 0.72rem;
      color: #64748b;
      margin-top: 2px;
    }
    .preset-btn.admin { border-left: 4px solid #7c3aed; }
    .preset-btn.supervisor { border-left: 4px solid #0284c7; }
    .preset-btn.almacenero { border-left: 4px solid #ea580c; }
    .preset-btn.cajero { border-left: 4px solid #16a34a; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  loading = false;
  errorMessage = '';

  constructor(private authService: AuthService) {}

  cargarCredenciales(u: string, p: string): void {
    this.username = u;
    this.password = p;
    this.errorMessage = '';
  }

  onLogin(): void {
    if (!this.username || !this.password) return;
    this.loading = true;
    this.errorMessage = '';

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Error de credenciales o usuario inactivo.';
      }
    });
  }
}
