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
  constructor(public state: AppStateService) {}

  onFileUpload(fileInput: HTMLInputElement, entityId: string, caption: string, type: string) {
    if (fileInput.files && fileInput.files.length > 0) {
      this.state.uploadFile(fileInput.files[0]).subscribe(res => {
        this.state.addVisualReference(entityId, res.url, caption, type);
        fileInput.value = '';
      });
    }
  }

  onGenerateImage(sceneId: string, chapterId: string, prompt: string, dialogue: string) {
    this.state.generateImage(prompt).subscribe(res => {
      // Create a panel using the generated image URL as the visual prompt or asset
      this.state.addPanel(sceneId, chapterId, res.url, dialogue);
    });
  }
}
