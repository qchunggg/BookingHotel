// core/interceptors/auth.interceptor.ts
import { inject, Injectable } from '@angular/core';
import {
  HttpInterceptor, HttpRequest, HttpHandler, HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';  // đường dẫn chỉnh cho khớp
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private auth = inject(AuthService);
  private router = inject(Router);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    /* 1️⃣ Gắn Authorization nếu đã có token */
    const jwt = this.auth.getToken();
    const authReq = jwt ? req.clone({ setHeaders: { Authorization: `Bearer ${jwt}` } }) : req;

    /* 2️⃣ Xử lý response + lỗi */
    return next.handle(authReq).pipe(
      catchError((err: HttpErrorResponse) => {
        /* Nếu backend trả 401 → token hết hạn hoặc không hợp lệ */
        if (err.status === 401) {
          /* Xoá token & user local (đăng xuất) */
          this.auth.logout().subscribe({ next: () => { } });   // fire-and-forget

          /* Chuyển về /login + lưu lại URL hiện tại */
          this.router.navigate(['/login'], {
            queryParams: { returnUrl: this.router.url },
          });
        }
        /* Propagate lỗi cho component khác nếu cần */
        return throwError(() => err);
      })
    );
  }
}