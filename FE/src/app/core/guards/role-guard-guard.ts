import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

export function roleGuard(allow: string[]): CanActivateFn {
  return (_route, state) => {
    const auth   = inject(AuthService);
    const router = inject(Router);

    /* 1. Chưa đăng nhập → về /login */
    if (!auth.isLoggedIn()) {
      return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }

    /* 2. Đã login nhưng không có quyền → về trang chủ (hoặc /403) */
    if (!auth.hasRole(...allow)) {
      return router.createUrlTree(['/']);
    }

    /* 3. Được phép */
    return true;
  };
}
