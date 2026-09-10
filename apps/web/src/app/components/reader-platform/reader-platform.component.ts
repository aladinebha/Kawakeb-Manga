import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppStateService } from '../../core/app-state.service';

@Component({
  selector: 'app-reader-platform',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reader-platform.component.html',
  styleUrl: './reader-platform.component.css'
})
export class ReaderPlatformComponent {
  state = inject(AppStateService);
}
