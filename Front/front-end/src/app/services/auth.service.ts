import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';

export interface User {
  id: number;
  name: string;
  email: string;
  role: 'HUNTER' | 'MASTER';
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  name: string;
  email: string;
  password: string;
  role: 'HUNTER' | 'MASTER';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private router: Router) {}

  login(credentials: LoginCredentials): Observable<boolean> {
    // Simulação de login - em produção, chamaria a API
    return new Observable(observer => {
      setTimeout(() => {
        // Simulação: aceita qualquer email/password
        const user: User = {
          id: 1,
          name: credentials.email.split('@')[0],
          email: credentials.email,
          role: 'HUNTER'
        };
        this.setUser(user);
        observer.next(true);
        observer.complete();
      }, 500);
    });
  }

  register(data: RegisterData): Observable<boolean> {
    // Simulação de registro - em produção, chamaria a API
    return new Observable(observer => {
      setTimeout(() => {
        const user: User = {
          id: Date.now(),
          name: data.name,
          email: data.email,
          role: data.role
        };
        this.setUser(user);
        observer.next(true);
        observer.complete();
      }, 500);
    });
  }

  logout(): void {
    localStorage.removeItem('devhunter_user');
    localStorage.removeItem('devhunter_token');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private setUser(user: User): void {
    localStorage.setItem('devhunter_user', JSON.stringify(user));
    localStorage.setItem('devhunter_token', 'fake-jwt-token');
    this.currentUserSubject.next(user);
  }

  private getStoredUser(): User | null {
    const userStr = localStorage.getItem('devhunter_user');
    if (userStr) {
      return JSON.parse(userStr);
    }
    return null;
  }
}

