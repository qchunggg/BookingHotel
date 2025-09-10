import { AfterViewInit, Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { AuthService } from '../../../services/auth-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { isPlatformBrowser } from '@angular/common';
import { filter } from 'rxjs';

declare const window: any;

@Component({
  selector: 'app-center',
  standalone: true,
  imports: [],
  templateUrl: './center.html',
  styleUrl: './center.css'
})
export class Center implements OnInit, AfterViewInit {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
  private authService = inject(AuthService);

  user = toSignal(this.authService.user$, { initialValue: null });

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.waitForTemplate().then(() => {
        this.callTemplateScripts();

        this.router.events
          .pipe(filter(e => e instanceof NavigationEnd))
          .subscribe((e: NavigationEnd) => {
            if (e.urlAfterRedirects === '/') {
              setTimeout(() => this.callTemplateScripts(), 100);
            }
          });
      });
    }
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      // 👇 DOM đã render xong, gọi jQuery hoặc JS ngoài tại đây
      this.callTemplateScripts();
    }
  }

  logout(): void {
    this.authService.logout().subscribe();
  }

  private callTemplateScripts(): void {
    const T = window.TEMPLATE;
    if (!T) return;
    T.initMenu?.();
    T.initHomeSlider?.();
    T.initDatePicker?.();
    T.initSvg?.();
    T.initGallery?.();
    T.initTestSlider?.();
    T.initBookingSlider?.();
    T.initBlogSlider?.();

    if (typeof window.setHeader === 'function') {
      window.setHeader();
    }

    if (typeof window.bindScrollForHeader === 'function') {
      window.bindScrollForHeader();
    }
  }

  private waitForTemplate(): Promise<void> {
    return new Promise((resolve) => {
      const check = () => {
        if (window.TEMPLATE) {
          return resolve();
        }
        setTimeout(check, 50); // check lại sau 50ms
      };
      check();
    });
  }
}
