import {TestBed} from '@angular/core/testing';
import {AppComponent} from './app.component';
import {RouterTestingModule} from "@angular/router/testing";
import {By} from "@angular/platform-browser";
import {RouterOutlet} from "@angular/router";

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([])],
      declarations: [
        AppComponent
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'support-portal-frontend'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('support-portal-frontend');
  });

  it('should have a router outlet', () => {
    const fixture = TestBed.createComponent(AppComponent);

    let debugElement = fixture.debugElement.query(By.directive(RouterOutlet));
    expect(debugElement).not.toBeNull();
  });

});
