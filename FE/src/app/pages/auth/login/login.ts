import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth-service';
import { Router } from '@angular/router';
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

  /* ======= STATE ======= */
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  /** Reactive Form */
  form = this.fb.nonNullable.group({
    userName: ['', [Validators.required]],
    password: ['', [Validators.required]],
    remember: false,
  });

  /** Helper cho template */
  get f(): { [K in keyof typeof this.form.controls]: AbstractControl } {
    return this.form.controls;
  }

  submit(): void {
    /* Hiển thị validate ngay khi bấm */
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        /* Điều hướng về trang home (đổi route tuỳ ý) */
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        this.loading.set(false);
        /* backend trả về message → lấy message, else hiển thị mặc định */
        this.errorMsg.set(
          err?.error?.message ?? 'Đăng nhập thất bại, vui lòng thử lại!'
        );
      },
    });
  }
}
