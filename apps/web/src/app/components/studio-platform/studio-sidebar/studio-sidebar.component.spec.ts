import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioSidebarComponent } from './studio-sidebar.component';

describe('StudioSidebarComponent', () => {
  let component: StudioSidebarComponent;
  let fixture: ComponentFixture<StudioSidebarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudioSidebarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudioSidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
