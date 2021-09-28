import {routes} from './app-routing.module';
import {UserComponent} from "./component/user/user.component";
import {LoginComponent} from "./component/login/login.component";
import {RegisterComponent} from "./component/register/register.component";
import {AuthenticationGuard} from "./guard/authentication.guard";

describe('routes', () => {

  it('should contain a route for /login', () => {
    expect(routes).toContain({path: "login", component: LoginComponent});
  });

  it('should contain a route for /register', () => {
    expect(routes).toContain({path: "register", component: RegisterComponent});
  });

  it('should contain a route for /user/management', () => {
    expect(routes).toContain({path: 'user/management', component: UserComponent, canActivate: [AuthenticationGuard]});
  });

  it('should redirect to /login endpoint if no route found', () => {
    expect(routes).toContain({path: '', redirectTo: '/login', pathMatch: 'full'});
  });

})
