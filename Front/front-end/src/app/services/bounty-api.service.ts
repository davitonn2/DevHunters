import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Bounty {
  id?: number;
  title: string;
  description: string;
  rewardXp: number;
  status?: string;
  hunter?: {
    id: number;
    name: string;
  };
}

export interface BountyCreateDTO {
  title: string;
  description: string;
  rewardXp: number;
}

export interface BountyClaimDTO {
  hunterId: number;
}

export interface BountySubmissionRequestDTO {
  hunterId: number;
}

@Injectable({
  providedIn: 'root'
})
export class BountyApiService {
  // No Docker, o nginx faz proxy de /api para o bounty-service
  // Localmente, use http://localhost:8080/bounties
  private apiUrl = '/api/bounties';

  constructor(private http: HttpClient) { }

  getOpenBounties(): Observable<Bounty[]> {
    return this.http.get<Bounty[]>(this.apiUrl);
  }

  createBounty(data: BountyCreateDTO): Observable<Bounty> {
    return this.http.post<Bounty>(this.apiUrl, data);
  }

  claimBounty(id: number, hunterId: number): Observable<Bounty> {
    return this.http.put<Bounty>(`${this.apiUrl}/${id}/claim`, { hunterId });
  }

  submitBounty(id: number, hunterId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/submit`, { hunterId });
  }

  deleteBounty(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

