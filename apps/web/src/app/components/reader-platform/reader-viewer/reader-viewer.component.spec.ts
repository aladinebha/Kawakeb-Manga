import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReaderViewerComponent } from './reader-viewer.component';

describe('ReaderViewerComponent', () => {
  let component: ReaderViewerComponent;
  let fixture: ComponentFixture<ReaderViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReaderViewerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReaderViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
