import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BountyApiService, Bounty } from '../../services/bounty-api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-bounty-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bounty-dashboard.component.html',
  styleUrl: './bounty-dashboard.component.css'
})
export class BountyDashboardComponent implements OnInit {
  bounties: Bounty[] = [];
  loading = false;
  error: string | null = null;
  hunterId: number = 1;

  constructor(
    private bountyService: BountyApiService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.hunterId = user.id;
    }
    this.loadBounties();
  }

  loadBounties(): void {
    this.loading = true;
    this.error = null;
    this.bountyService.getOpenBounties().subscribe({
      next: (data) => {
        this.bounties = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar bounties. Verifique se o backend está rodando.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  claimBounty(bountyId: number): void {
    this.bountyService.claimBounty(bountyId, this.hunterId).subscribe({
      next: () => {
        this.loadBounties();
      },
      error: (err) => {
        this.error = 'Erro ao reivindicar bounty.';
        console.error(err);
      }
    });
  }

  submitBounty(bountyId: number): void {
    this.bountyService.submitBounty(bountyId, this.hunterId).subscribe({
      next: () => {
        alert('Bounty entregue com sucesso! Aguardando revisão...');
        this.loadBounties();
      },
      error: (err) => {
        this.error = 'Erro ao entregar bounty.';
        console.error(err);
      }
    });
  }

  deleteBounty(bountyId: number): void {
    if (confirm('Tem certeza que deseja deletar esta bounty?')) {
      this.bountyService.deleteBounty(bountyId).subscribe({
        next: () => {
          this.loadBounties();
        },
        error: (err) => {
          this.error = 'Erro ao deletar bounty.';
          console.error(err);
        }
      });
    }
  }
}

