import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AfterViewInit, Component, ElementRef, HostListener, inject, NgZone, PLATFORM_ID, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';

declare const window: any;

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
  ],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header implements AfterViewInit {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
  private zone = inject(NgZone);
  private authService = inject(AuthService);



  // --- helper an toàn cho SSR ---
  private raf(fn: FrameRequestCallback) {
    if (isPlatformBrowser(this.platformId) && typeof window !== 'undefined' && 'requestAnimationFrame' in window) {
      return window.requestAnimationFrame(fn);
    }
    // fallback SSR
    return setTimeout(() => fn(0 as any), 0);
  }

  nav = [
    { label: 'Trang chủ', path: '/' },
    { label: 'Khách sạn', path: '/hotels' },
    { label: 'Phòng', path: '/rooms' },
    { label: 'Tài khoản', path: '/users' },
  ];

  isScrolled = false;
  mobileOpen = false;

  user = toSignal(this.authService.user$, { initialValue: null });

  @ViewChild('navList', { read: ElementRef }) navList!: ElementRef<HTMLUListElement>;
  @ViewChildren('navItem', { read: ElementRef }) navItems!: QueryList<ElementRef<HTMLLIElement>>;

  indicatorStyle: { width?: string; transform?: string; } = {};
  showIndicator = true;
  activeIndex = 0;

  constructor() {
    // Tính lại mỗi lần đổi route
    this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(() => {
      this.afterRouteChange();
    });
  }

  ngAfterViewInit(): void {
    this.setActive(0);
    // Khi danh sách item render/changing → đo lại
    this.navItems.changes.subscribe(() => {
      this.zone.runOutsideAngular(() => this.raf(() => this.updateIndicator()));
    });

    // Lần đầu render view
    this.zone.runOutsideAngular(() => this.raf(() => this.afterRouteChange()));
  }

  private afterRouteChange(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    const url = (this.router.url || '/').split('?')[0];

    // Ẩn trên /login và /register
    this.showIndicator = !(url.startsWith('/login') || url.startsWith('/register'));

    // Xác định index đang active (match prefix dài nhất)
    let idx = -1, best = -1;
    this.nav.forEach((n, i) => {
      const p = n.path;
      if ((p === '/' && url === '/') || (p !== '/' && url.startsWith(p))) {
        if (p.length > best) { best = p.length; idx = i; }
      }
    });

    this.activeIndex = this.showIndicator ? (idx >= 0 ? idx : 0) : 0;

    this.updateIndicator();
  }

  setActive(i: number): void {
    this.activeIndex = i;
    this.updateIndicator();
  }

  @HostListener('window:scroll')
  onScroll() {
    if (!isPlatformBrowser(this.platformId)) return;
    this.isScrolled = window.scrollY > 8;
  }

  @HostListener('window:resize')
  onResize() {
    this.updateIndicator();
  }

  private updateIndicator(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    if (!this.showIndicator || this.activeIndex < 0) {
      this.indicatorStyle = { width: '0px', transform: 'translateX(0)' };
      return;
    }

    const navEl = this.navList?.nativeElement;
    const items = this.navItems?.toArray() ?? [];
    const li = items[this.activeIndex]?.nativeElement;
    if (!navEl || !li) return;

    const navRect = navEl.getBoundingClientRect();
    const liRect = li.getBoundingClientRect();

    const width = Math.round(liRect.width);
    const left = Math.round(liRect.left - navRect.left);

    this.indicatorStyle = {
      width: `${width}px`,
      transform: `translateX(${left}px)`
    };
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}
