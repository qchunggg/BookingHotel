import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AfterViewInit, Component, inject, PLATFORM_ID } from '@angular/core';
import { RouterModule } from '@angular/router';

declare const window: any;

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule,
    RouterModule
  ],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements AfterViewInit {

  private platformId = inject(PLATFORM_ID);

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId) && window.TEMPLATE) {
      const T = window.TEMPLATE;
      T.initMenu?.();
      T.initHomeSlider?.();
      T.initDatePicker?.();
      T.initSvg?.();
      T.initGallery?.();
      T.initTestSlider?.();
      T.initBookingSlider?.();
      T.initBlogSlider?.();
    }
  }
}
