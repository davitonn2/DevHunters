import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BountyApiService, BountyCreateDTO } from '../../services/bounty-api.service';

@Component({
  selector: 'app-create-bounty-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-bounty-form.component.html',
  styleUrl: './create-bounty-form.component.css'
})
export class CreateBountyFormComponent {
  @Output() bountyCreated = new EventEmitter<void>();

  bounty: BountyCreateDTO = {
    title: '',
    description: '',
    rewardValue: 0
  };

  loading = false;
  error: string | null = null;

  constructor(private bountyService: BountyApiService) { }

  onSubmit(): void {
    if (!this.bounty.title || !this.bounty.description || !this.bounty.rewardValue || this.bounty.rewardValue <= 0) {
      this.error = 'Preencha todos os campos corretamente.';
      return;
    }

    this.loading = true;
    this.error = null;

    const bountyData = {
      ...this.bounty,
      rewardXp: Number(this.bounty.rewardValue)
    };

    this.bountyService.createBounty(bountyData).subscribe({
      next: () => {
        this.bounty = { title: '', description: '', rewardValue: 0 };
        this.loading = false;
        this.bountyCreated.emit();
        alert('Bounty criada com sucesso!');
      },
      error: (err) => {
        this.error = err?.error?.message ?? err?.error ?? 'Erro ao criar bounty.';
        this.loading = false;
        console.error(err);
      }
    });
  }
}


