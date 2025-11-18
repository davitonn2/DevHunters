import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { BountyDashboardComponent } from '../bounty-dashboard/bounty-dashboard.component';
import { CreateBountyFormComponent } from '../create-bounty-form/create-bounty-form.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BountyDashboardComponent, CreateBountyFormComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  @ViewChild(BountyDashboardComponent) dashboard!: BountyDashboardComponent;
  
  currentUser: any = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
      }
    });
  }

  onBountyCreated(): void {
    if (this.dashboard) {
      this.dashboard.loadBounties();
    }
  }

  logout(): void {
    this.authService.logout();
  }
}

