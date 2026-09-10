import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioPanelsComponent } from './studio-panels.component';

describe('StudioPanelsComponent', () => {
  let component: StudioPanelsComponent;
  let fixture: ComponentFixture<StudioPanelsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudioPanelsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudioPanelsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
