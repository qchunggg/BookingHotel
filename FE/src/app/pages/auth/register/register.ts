import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register implements AfterViewInit {

  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  errorMsg = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    userName: ['', [Validators.required, Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    email: [
      '',
      [Validators.required, Validators.email],
    ],
    phone: [
      '',
      [Validators.pattern(/^0\d{9}$/), Validators.maxLength(10)],
    ],
  });

  get f(): { [K in keyof typeof this.form.controls]: AbstractControl } {
    return this.form.controls;
  }

  ngAfterViewInit(): void {
    const el = document.querySelector('.register-container') as HTMLElement | null;
    if (el) {
      requestAnimationFrame(() => el.classList.add('enter'));
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);

    const payload = this.form.getRawValue();

    this.auth.register(payload).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(
          err?.error?.message ?? 'Đăng ký thất bại. Vui lòng thử lại!'
        );
      },
    });
  }

  login(): void {
    const container = document.querySelector('.register-container') as HTMLElement | null;
    if (container) {
      container.style.transition = 'opacity 0.3s ease';
      container.style.opacity = '0';

      setTimeout(() => {
        this.router.navigate(['/login']).then(() => {
          // Thêm hiệu ứng fade-in trên trang register
          const loginContainer = document.querySelector('.register-container') as HTMLElement | null;
          if (loginContainer) {
            loginContainer.style.opacity = '0';
            setTimeout(() => {
              loginContainer.style.transition = 'opacity 0.3s ease';
              loginContainer.style.opacity = '1';
            }, 50);
          }
        });
      }, 500);
    }
  }
}
