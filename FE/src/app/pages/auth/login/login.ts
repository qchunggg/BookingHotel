import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth-service';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule,
    ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loading = signal(false);
  errorMsg = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    userName: ['', [Validators.required]],
    password: ['', [Validators.required]],
    remember: false,
  });

  get f() {
    return this.form.controls;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);

    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);

        const container = document.querySelector('.login-container') as HTMLElement | null;
        const go = () => this.router.navigateByUrl(returnUrl);

        if (!container) {
          go();
          return;
        }

        container.classList.add('leave');

        const onDone = () => {
          container.removeEventListener('transitionend', onDone);
          go();
        };
        container.addEventListener('transitionend', onDone);
        setTimeout(onDone, 420);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(
          err?.error?.message ?? 'Đăng nhập thất bại, vui lòng thử lại!'
        );
      },
    });
  }

  register(): void {
    const container = document.querySelector('.login-container') as HTMLElement | null;
    if (container) {
      container.style.transition = 'opacity 0.3s ease';
      container.style.opacity = '0';

      setTimeout(() => {
        this.router.navigate(['/register']).then(() => {
          const registerContainer = document.querySelector('.login-container') as HTMLElement | null;
          if (registerContainer) {
            registerContainer.style.opacity = '0';
            setTimeout(() => {
              registerContainer.style.transition = 'opacity 0.3s ease';
              registerContainer.style.opacity = '1';
            }, 50);
          }
        });
      }, 500);
    }
  }
}
