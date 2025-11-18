import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, RegisterData } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerData: RegisterData = {
    name: '',
    email: '',
    password: '',
    role: 'HUNTER'
  };

  confirmPassword = '';
  loading = false;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (!this.registerData.name || !this.registerData.email || !this.registerData.password) {
      this.error = 'Preencha todos os campos';
      return;
    }

    if (this.registerData.password !== this.confirmPassword) {
      this.error = 'As senhas não coincidem';
      return;
    }

    if (this.registerData.password.length < 6) {
      this.error = 'A senha deve ter pelo menos 6 caracteres';
      return;
    }

    this.loading = true;
    this.error = null;

    this.authService.register(this.registerData).subscribe({
      next: (success) => {
        if (success) {
          this.router.navigate(['/dashboard']);
        } else {
          this.error = 'Erro ao criar conta';
          this.loading = false;
        }
      },
      error: () => {
        this.error = 'Erro ao criar conta';
        this.loading = false;
      }
    });
  }
}

