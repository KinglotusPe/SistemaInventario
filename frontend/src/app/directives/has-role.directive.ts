import { Directive, Input, TemplateRef, ViewContainerRef, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Directive({
  selector: '[hasRole]',
  standalone: true
})
export class HasRoleDirective implements OnInit, OnDestroy {
  private role!: string;
  private isHidden = true;
  private sub!: Subscription;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) {}

  @Input()
  set hasRole(val: string) {
    this.role = val;
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
    if (!this.role) {
      this.show();
      return;
    }

    const hasAccess = this.authService.hasRole(this.role);

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
