import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppStateService } from '../../core/app-state.service';

@Component({
  selector: 'app-studio-platform',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './studio-platform.component.html',
  styleUrl: './studio-platform.component.css'
})
export class StudioPlatformComponent {
  state = inject(AppStateService);
}
