import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReaderPlatformComponent } from './reader-platform.component';

describe('ReaderPlatformComponent', () => {
  let component: ReaderPlatformComponent;
  let fixture: ComponentFixture<ReaderPlatformComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReaderPlatformComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReaderPlatformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
