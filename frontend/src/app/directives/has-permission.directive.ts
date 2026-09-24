import { Directive, Input, TemplateRef, ViewContainerRef, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Directive({
  selector: '[hasPermission]',
  standalone: true
})
export class HasPermissionDirective implements OnInit, OnDestroy {
  private permission!: string;
  private isHidden = true;
  private sub!: Subscription;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) {}

  @Input()
  set hasPermission(val: string) {
    this.permission = val;
    this.updateView();
  }

  ngOnInit(): void {
    this.sub = this.authService.currentUser$.subscribe(() => {
      this.updateView();
    });
  }

  ngOnDestroy(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  private updateView(): void {
    if (!this.permission) {
      this.show();
      return;
    }

    const hasAccess = this.authService.hasPermission(this.permission);

    if (hasAccess && this.isHidden) {
      this.show();
    } else if (!hasAccess && !this.isHidden) {
      this.hide();
    }
  }

  private show(): void {
    this.viewContainer.createEmbeddedView(this.templateRef);
    this.isHidden = false;
  }

  private hide(): void {
    this.viewContainer.clear();
    this.isHidden = true;
  }
}
