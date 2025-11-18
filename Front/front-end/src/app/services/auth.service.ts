import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, switchMap, tap } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

export interface User {
  id: number;
  name: string;
  email: string;
  role: 'HUNTER' | 'MASTER' | 'ADMIN';
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

interface LoginResponse {
  token: string;
  userId: number;
  name: string;
  login: string;
  role: 'HUNTER' | 'MASTER' | 'ADMIN';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'devhunter_token';
  private readonly USER_KEY = 'devhunter_user';
  private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private router: Router, private http: HttpClient) {}

  login(credentials: LoginCredentials): Observable<boolean> {
    const payload = { login: credentials.email, password: credentials.password };
    return this.http.post<LoginResponse>('/api/auth/login', payload).pipe(
      tap(response => this.persistSession(response)),
      map(() => true)
    );
  }

  register(data: RegisterData): Observable<boolean> {
    const payload = {
      login: data.email,
      password: data.password,
      role: data.role,
      name: data.name
    };

    return this.http.post<void>('/api/auth/register', payload).pipe(
      switchMap(() => this.login({ email: data.email, password: data.password }))
    );
  }

  logout(): void {
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  hasRole(role: User['role']): boolean {
    return this.currentUserSubject.value?.role === role;
  }

  private persistSession(response: LoginResponse): void {
    const user: User = {
      id: response.userId,
      name: response.name ?? response.login,
      email: response.login,
      role: response.role
    };
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    localStorage.setItem(this.TOKEN_KEY, response.token);
    this.currentUserSubject.next(user);
  }

  private getStoredUser(): User | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userStr = localStorage.getItem(this.USER_KEY);
    if (!token || !userStr) {
      return null;
    }
    try {
      return JSON.parse(userStr) as User;
    } catch {
      return null;
    }
  }
}


