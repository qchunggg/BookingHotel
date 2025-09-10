import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule
  ],
  templateUrl: './footer.html',
  styleUrl: './footer.css'
})
export class Footer {
  readonly currentYear = new Date().getFullYear();

  // (tuỳ chọn) chặn submit form newsletter để không reload trang
  onNewsletterSubmit(ev: Event) {
    ev.preventDefault();
    // TODO: gọi API/hiển thị toast...
  }
}
