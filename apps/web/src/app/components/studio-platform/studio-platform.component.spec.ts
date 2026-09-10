import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioPlatformComponent } from './studio-platform.component';

describe('StudioPlatformComponent', () => {
  let component: StudioPlatformComponent;
  let fixture: ComponentFixture<StudioPlatformComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudioPlatformComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudioPlatformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
