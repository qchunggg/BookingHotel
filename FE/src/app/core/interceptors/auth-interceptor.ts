// core/interceptors/auth.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpInterceptor, HttpRequest, HttpHandler, HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';  // đường dẫn chỉnh cho khớp
import { AuthService } from '../../services/auth-service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private auth: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    /* LẤY JWT TỪ AuthService */
    const jwt = this.auth.getToken();

    /* NẾU CÓ TOKEN ⇒ CLONE & GẮN HEADER */
    const authReq = jwt
      ? req.clone({ setHeaders: { Authorization: `Bearer ${jwt}` } })
      : req;

    return next.handle(authReq);
  }
}