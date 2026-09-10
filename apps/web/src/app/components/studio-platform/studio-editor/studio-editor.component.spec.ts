import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioEditorComponent } from './studio-editor.component';

describe('StudioEditorComponent', () => {
  let component: StudioEditorComponent;
  let fixture: ComponentFixture<StudioEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudioEditorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudioEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
