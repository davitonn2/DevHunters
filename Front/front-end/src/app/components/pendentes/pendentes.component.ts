import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Bounty, BountyApiService } from '../../services/bounty-api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-pendentes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pendentes.component.html',
  styleUrls: ['./pendentes.component.css']
})
export class PendentesComponent implements OnInit {
  bounties: Bounty[] = [];
  userRole: string | null = null;

  constructor(private api: BountyApiService, public auth: AuthService) { }

  ngOnInit(): void {
    this.userRole = this.auth.getCurrentUser()?.role ?? null;
    this.load();
  }

  load() {
    this.api.getPendingBounties().subscribe(list => this.bounties = list);
  }

  approve(bounty: Bounty) {
    this.api.approveClaim(bounty.id!).subscribe(() => this.load());
  }

  rejectClaim(bounty: Bounty) {
    this.api.rejectClaim(bounty.id!).subscribe(() => this.load());
  }

  finish(bounty: Bounty) {
    // Hunter marks as submitted
    this.api.submitBounty(bounty.id!, bounty.hunter?.id).subscribe(() => this.load());
  }

  approveReview(bounty: Bounty) {
    this.api.completeBounty(bounty.id!).subscribe(() => this.load());
  }

  rejectReview(bounty: Bounty) {
    const reason = prompt('Motivo da reprovação (opcional)') || '';
    this.api.rejectReview(bounty.id!, reason).subscribe(() => this.load());
  }
}
