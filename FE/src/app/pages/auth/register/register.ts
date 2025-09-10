import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, inject, OnInit, signal } from '@angular/core';
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
export class Register implements OnInit {

  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  errorMsg = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    userName: ['', [Validators.required, Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]],
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.pattern(/^0\d{9}$/), Validators.maxLength(10)]],
  });

  // Custom validator để kiểm tra confirmPassword khớp với password
  ngOnInit() {
    this.form.controls.confirmPassword.addValidators((control: AbstractControl) => {
      const password = this.form.controls.password.value;
      const confirmPassword = control.value;
      return password === confirmPassword ? null : { mismatch: true };
    });
  }

  get f(): { [K in keyof typeof this.form.controls]: AbstractControl } {
    return this.form.controls;
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

  // SỬA LẠI HÀM login() ĐỂ GIỐNG BÊN LOGIN COMPONENT
  login(): void {
    const container = document.querySelector('.register-container') as HTMLElement | null;

    if (!container) {
      this.router.navigate(['/login']);
      return;
    }

    container.classList.add('leave');

    const onDone = () => {
      container.removeEventListener('transitionend', onDone);
      this.router.navigate(['/login']);
    };
    container.addEventListener('transitionend', onDone);

    setTimeout(() => this.router.navigate(['/login']), 200);
  }
}
