import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface UserSession {
  id: number;
  username: string;
  email: string;
  nombres: string;
  apellidos: string;
  roles: string[];
  permisos: string[];
  token: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    token: string;
    type: string;
    id: number;
    username: string;
    email: string;
    nombres: string;
    apellidos: string;
    roles: string[];
    permisos: string[];
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<UserSession | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  public get currentUserValue(): UserSession | null {
    return this.currentUserSubject.value;
  }

  login(credentials: { username: string; password: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.success && response.data) {
          const user: UserSession = {
            id: response.data.id,
            username: response.data.username,
            email: response.data.email,
            nombres: response.data.nombres,
            apellidos: response.data.apellidos,
            roles: response.data.roles || [],
            permisos: response.data.permisos || [],
            token: response.data.token
          };
          localStorage.setItem('auth_user', JSON.stringify(user));
          localStorage.setItem('auth_token', response.data.token);
          this.currentUserSubject.next(user);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('auth_user');
    localStorage.removeItem('auth_token');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Verifica si el usuario posee un rol específico (ej: 'ADMINISTRADOR' o 'ROLE_ADMINISTRADOR')
   */
  hasRole(roleName: string): boolean {
    const user = this.currentUserValue;
    if (!user || !user.roles) return false;
    const formatted = roleName.startsWith('ROLE_') ? roleName : `ROLE_${roleName}`;
    return user.roles.some(r => r.toUpperCase() === formatted.toUpperCase());
  }

  /**
   * Verifica si el usuario posee un permiso atómico granular (ej: 'PRODUCTO_CREAR', 'INVENTARIO_AJUSTAR')
   * Los administradores poseen acceso universal automático.
   */
  hasPermission(permission: string): boolean {
    const user = this.currentUserValue;
    if (!user) return false;
    if (this.hasRole('ADMINISTRADOR')) return true; // Superusuario
    return user.permisos?.includes(permission.toUpperCase()) ?? false;
  }

  /**
   * Verifica si el usuario posee al menos uno de los permisos provistos
   */
  hasAnyPermission(permissions: string[]): boolean {
    const user = this.currentUserValue;
    if (!user) return false;
    if (this.hasRole('ADMINISTRADOR')) return true;
    return permissions.some(perm => user.permisos?.includes(perm.toUpperCase()));
  }

  private getUserFromStorage(): UserSession | null {
    const data = localStorage.getItem('auth_user');
    try {
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  }
}
