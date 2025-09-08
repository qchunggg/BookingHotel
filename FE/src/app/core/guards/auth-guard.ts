// src/app/core/guards/auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

export const authGuardFn: CanActivateFn = (_route, state) => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  /* ĐÃ đăng nhập  */
  if (auth.isLoggedIn()) return true;

  /* CHƯA đăng nhập → chuyển sang /login và nhớ URL gốc */
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};
