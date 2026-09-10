import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReaderDiscoveryComponent } from './reader-discovery.component';

describe('ReaderDiscoveryComponent', () => {
  let component: ReaderDiscoveryComponent;
  let fixture: ComponentFixture<ReaderDiscoveryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReaderDiscoveryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReaderDiscoveryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
