import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { BountyApiService, Bounty } from '../../services/bounty-api.service';
import { AuthService, User } from '../../services/auth.service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-bounty-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bounty-dashboard.component.html',
  styleUrl: './bounty-dashboard.component.css'
})
export class BountyDashboardComponent implements OnInit, OnDestroy {
  bounties: Bounty[] = [];
  pendingBounties: Bounty[] = [];
  loading = false;
  error: string | null = null;
  hunterId: number | null = null;
  currentUser: User | null = null;
  private subscriptions = new Subscription();
  private pollHandle: any = null;

  constructor(
    private bountyService: BountyApiService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.subscriptions.add(
      this.authService.currentUser$.subscribe(user => {
        this.currentUser = user;
        this.hunterId = user?.id ?? null;
      })
    );

    this.loadBounties();

    this.pollHandle = setInterval(() => {

      // Atualiza Bounties
      this.loadBounties();

      // Atualiza Saldo
      if (this.currentUser) {
        this.authService.refreshCurrentUser().subscribe({
          next: (user: User) => {

            this.currentUser = user;

            this.cdr.detectChanges();
          },
          error: (err) => console.error('❌ Erro no polling:', err)
        });
      } else {
      }
    }, 5000);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    if (this.pollHandle) {
      clearInterval(this.pollHandle);
      this.pollHandle = null;
    }
  }

  loadBounties(): void {
    this.loading = true;
    this.error = null;

    this.bountyService.getBounties().subscribe({
      next: (data) => {
        this.bounties = data;
        this.bountyService.getPendingBounties().subscribe({
          next: (pending) => {

            if (this.currentUser?.role === 'HUNTER') {
              this.pendingBounties = pending.filter(b => b.hunter?.id === this.hunterId);
            } else {
              this.pendingBounties = pending.filter(b => b.createdBy?.id === this.currentUser?.id);
            }
            this.loading = false;
          },
          error: (err) => {
            this.error = this.resolveError(err, 'Erro ao carregar bounties pendentes.');
            this.loading = false;
            console.error(err);
          }
        });
      },
      error: (err) => {
        this.error = this.resolveError(err, 'Erro ao carregar bounties. Verifique se o backend está rodando.');
        this.loading = false;
        console.error(err);
      }
    });
  }

  claimBounty(bountyId: number): void {
    if (!this.hunterId) {
      this.error = 'Você precisa estar autenticado como Hunter para reivindicar.';
      return;
    }

    this.bountyService.claimBounty(bountyId, this.hunterId).subscribe({
      next: () => {
        alert('Interesse registrado! Aguarde o master aprovar sua solicitação.');
        this.loadBounties();
      },
      error: (err) => {
        this.error = this.resolveError(err, 'Erro ao reivindicar bounty.');
        console.error(err);
      }
    });
  }

  submitBounty(bountyId: number): void {
    if (!this.hunterId) {
      this.error = 'Você precisa estar autenticado como Hunter para entregar.';
      return;
    }

    this.bountyService.submitBounty(bountyId, this.hunterId).subscribe({
      next: () => {
        alert('Bounty entregue com sucesso! Aguardando revisão...');
        this.loadBounties();
      },
      error: (err) => {
        this.error = this.resolveError(err, 'Erro ao entregar bounty.');
        console.error(err);
      }
    });
  }

  approveClaim(bountyId: number): void {
    this.bountyService.approveClaim(bountyId).subscribe({
      next: () => {
        alert('Reivindicação aprovada com sucesso.');
        this.loadBounties();
      },
      error: (err) => {
        this.error = this.resolveError(err, 'Erro ao aprovar reivindicação.');
        console.error(err);
      }
    });
  }

  rejectClaim(bountyId: number): void {
    this.bountyService.rejectClaim(bountyId).subscribe({
      next: () => {
        alert('Reivindicação recusada.');
        this.loadBounties();
      },
      error: (err) => {
        this.error = this.resolveError(err, 'Erro ao recusar reivindicação.');
        console.error(err);
      }
    });
  }

  completeBounty(bountyId: number): void {
    this.bountyService.completeBounty(bountyId).subscribe({
      next: () => {
        alert('Bounty finalizada com sucesso! Recompensa creditado ao Hunter.');
        setTimeout(() =>{

        this.authService.refreshCurrentUser().subscribe((userAtualizado:User) => {
          this.currentUser = userAtualizado;
        });
        }, 500);
        this.loadBounties();
      },
      error: (err) => {
        this.error = this.resolveError(err, 'Erro ao finalizar bounty.');
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
          this.error = this.resolveError(err, 'Erro ao deletar bounty.');
          console.error(err);
        }
      });
    }
  }

  canClaim(bounty: Bounty): boolean {
    return bounty.status === 'ABERTA' && !!this.currentUser && this.currentUser.role === 'HUNTER';
  }

  canSubmit(bounty: Bounty): boolean {
    return bounty.status === 'EM_ANDAMENTO' && !!bounty.hunter && bounty.hunter.id === this.hunterId;
  }

  canDelete(bounty: Bounty): boolean {
    return this.currentUser?.role === 'MASTER' && bounty.createdBy?.id === this.currentUser?.id;
  }

  canApprove(bounty: Bounty): boolean {
    return bounty.status === 'AGUARDANDO_APROVACAO'
      && this.currentUser?.role === 'MASTER'
      && bounty.createdBy?.id === this.currentUser?.id;
  }

  canComplete(bounty: Bounty): boolean {
    return bounty.status === 'EM_REVISAO'
      && this.currentUser?.role === 'MASTER'
      && bounty.createdBy?.id === this.currentUser?.id;
  }

  statusClass(status?: string): string {
    if (!status) {
      return 'status-default';
    }
    return 'status-' + status.toLowerCase().replace(/_/g, '-');
  }

  private resolveError(err: any, fallback: string): string {
    if (!err) {
      return fallback;
    }
    if (typeof err.error === 'string') {
      return err.error;
    }
    if (err.error?.message) {
      return err.error.message;
    }
    return fallback;
  }
}
