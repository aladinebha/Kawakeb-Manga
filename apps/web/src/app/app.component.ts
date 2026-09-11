import { Component, OnInit, OnDestroy, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppStateService } from './core/app-state.service';
import { AppTopBarComponent } from './components/app-top-bar/app-top-bar.component';
import { AuthComponent } from './components/auth/auth.component';
import { StudioPlatformComponent } from './components/studio-platform/studio-platform.component';
import { ReaderPlatformComponent } from './components/reader-platform/reader-platform.component';
import { StarfieldComponent } from './components/starfield/starfield.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    AppTopBarComponent, 
    AuthComponent, 
    StudioPlatformComponent, 
    ReaderPlatformComponent,
    StarfieldComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  state = inject(AppStateService);

  @HostListener('window:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    this.state.handleKeyboardEvent(event);
  }

  ngOnInit() {
    this.state.ngOnInit();
  }

  ngOnDestroy() {
    this.state.ngOnDestroy();
  }
}
