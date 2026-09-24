import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }

    const expectedRoles: string[] = route.data['roles'] || [];
    const expectedPermissions: string[] = route.data['permissions'] || [];

    // 1. Validar por roles si están definidos
    if (expectedRoles.length > 0) {
      const hasAnyRole = expectedRoles.some(role => this.authService.hasRole(role));
      if (hasAnyRole) return true;
    }

    // 2. Validar por permisos granulares si están definidos
    if (expectedPermissions.length > 0) {
      const hasAnyPerm = this.authService.hasAnyPermission(expectedPermissions);
      if (hasAnyPerm) return true;
    }

    // 3. Si no cumple ni rol ni permiso: bloquear y alertar
    alert('Acceso Denegado (403): Su rol actual no tiene privilegios para acceder a esta sección.');
    this.router.navigate(['/']);
    return false;
  }
}
