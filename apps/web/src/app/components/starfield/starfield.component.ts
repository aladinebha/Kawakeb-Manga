import { Component, ElementRef, ViewChild, AfterViewInit, HostListener, NgZone, OnDestroy } from '@angular/core';

@Component({
  selector: 'app-starfield',
  standalone: true,
  templateUrl: './starfield.component.html',
  styleUrl: './starfield.component.css'
})
export class StarfieldComponent implements AfterViewInit, OnDestroy {
  @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;
  
  private ctx!: CanvasRenderingContext2D;
  private stars: any[] = [];
  private numStars = 200;
  private mouseX = 0;
  private mouseY = 0;
  private animationFrameId = 0;

  constructor(private ngZone: NgZone) {}

  ngAfterViewInit() {
    const canvas = this.canvasRef.nativeElement;
    this.ctx = canvas.getContext('2d')!;
    this.resizeCanvas();
    this.initStars();
    
    // Run animation outside Angular zone for performance
    this.ngZone.runOutsideAngular(() => {
      this.animate();
    });
  }

  ngOnDestroy() {
    cancelAnimationFrame(this.animationFrameId);
  }

  @HostListener('window:resize')
  onResize() {
    this.resizeCanvas();
    this.initStars();
  }

  @HostListener('window:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    this.mouseX = (event.clientX - window.innerWidth / 2) * 0.05;
    this.mouseY = (event.clientY - window.innerHeight / 2) * 0.05;
  }

  private resizeCanvas() {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }

  private initStars() {
    this.stars = [];
    const canvas = this.canvasRef.nativeElement;
    for (let i = 0; i < this.numStars; i++) {
      this.stars.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        z: Math.random() * canvas.width,
        o: '0.' + Math.floor(Math.random() * 99) + 1,
        radius: Math.random() * 1.5
      });
    }
  }

  private animate() {
    const canvas = this.canvasRef.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    for (let i = 0; i < this.numStars; i++) {
      let star = this.stars[i];
      
      // Parallax effect based on mouse movement
      let px = star.x + (this.mouseX * (star.z * 0.001));
      let py = star.y + (this.mouseY * (star.z * 0.001));

      // Wrap around
      if (px < 0) px = canvas.width;
      if (px > canvas.width) px = 0;
      if (py < 0) py = canvas.height;
      if (py > canvas.height) py = 0;

      this.ctx.beginPath();
      this.ctx.arc(px, py, star.radius, 0, Math.PI * 2);
      this.ctx.fillStyle = `rgba(255, 255, 255, ${star.o})`;
      this.ctx.fill();
    }
    
    this.animationFrameId = requestAnimationFrame(() => this.animate());
  }
}
