import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {LoginComponent} from "./component/login/login.component";
import {RegisterComponent} from "./component/register/register.component";
import {UserComponent} from "./component/user/user.component";
import {AuthenticationGuard} from "./guard/authentication.guard";
import {ManagementComponent} from "./component/management/management.component";
import {UsersComponent} from "./component/management/users/users.component";
import {SettingsComponent} from "./component/management/settings/settings.component";
import {ProfileComponent} from "./component/management/profile/profile.component";
import {UserEditComponent} from "./component/management/users/user-edit/user-edit.component";
import {UserViewComponent} from "./component/management/users/user-view/user-view.component";
import {UserResolver} from "./component/management/users/user-resolver.service";

export const routes: Routes = [
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'user/management', component: UserComponent, canActivate: [AuthenticationGuard]},
  {
    path: 'management', component: ManagementComponent, canActivate: [AuthenticationGuard],
    children: [
      {path: 'settings', component: SettingsComponent},
      {path: 'profile', component: ProfileComponent},
      {
        path: 'users', component: UsersComponent,
        children: [
          {path: ':id/view', component: UserViewComponent, resolve: {user: UserResolver}},
          {path: ':id/edit', component: UserEditComponent}
        ]
      }
    ]
  },
  {path: '', redirectTo: '/login', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
