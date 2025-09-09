import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, inject, PLATFORM_ID } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth-service';
import { toSignal } from '@angular/core/rxjs-interop';

declare const window: any;

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule,
    RouterModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {
  private platformId = inject(PLATFORM_ID);
  private authService = inject(AuthService);

  user = toSignal(this.authService.user$, { initialValue: null });

  logout(): void {
    this.authService.logout().subscribe();
  }

  ngAfterViewInit(): void {
    // 👇 Gọi setHeader nếu cần hiệu ứng scroll
    if (isPlatformBrowser(this.platformId)) {
      if (typeof window.setHeader === 'function') {
        window.setHeader();
      }

      if (typeof window.bindScrollForHeader === 'function') {
        window.bindScrollForHeader();
      }
    }
  }
}
