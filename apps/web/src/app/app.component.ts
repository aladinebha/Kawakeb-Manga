import { Component, OnInit, OnDestroy, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppStateService } from './core/app-state.service';
import { AuthComponent } from './components/auth/auth.component';
import { AppTopBarComponent } from './components/app-top-bar/app-top-bar.component';
import { StudioPlatformComponent } from './components/studio-platform/studio-platform.component';
import { ReaderPlatformComponent } from './components/reader-platform/reader-platform.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    AuthComponent, 
    AppTopBarComponent, 
    StudioPlatformComponent, 
    ReaderPlatformComponent
  ],
  templateUrl: './app.component.html'
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
