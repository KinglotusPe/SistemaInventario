import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();

    // Adjuntar token Bearer si existe y la petición va hacia el backend
    let authReq = request;
    if (token) {
      authReq = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Token expirado o inválido: cerrar sesión
          console.warn('Sesión expirada o no autorizada (401). Redirigiendo a Login...');
          this.authService.logout();
        } else if (error.status === 403) {
          console.error('Acceso denegado (403 Forbidden): Privilegios insuficientes para la acción.');
          alert('Acceso Denegado (403): Su usuario no cuenta con los permisos necesarios para realizar esta operación.');
        }
        return throwError(() => error);
      })
    );
  }
}
