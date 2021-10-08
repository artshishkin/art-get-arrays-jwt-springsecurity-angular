import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {AuthenticationService} from "./service/authentication.service";
import {UserService} from "./service/user.service";
import {AuthInterceptor} from "./interceptor/auth.interceptor";
import {AuthenticationGuard} from "./guard/authentication.guard";
import {NotificationModule} from "./notification/notification.module";
import {LoginComponent} from './component/login/login.component';
import {RegisterComponent} from './component/register/register.component';
import {UserComponent} from './component/user/user.component';
import {AppRoutingModule} from './app-routing.module';
import {FormsModule} from "@angular/forms";
import {ManagementComponent} from './component/management/management.component';
import {UsersComponent} from './component/management/users/users.component';
import {SettingsComponent} from './component/management/settings/settings.component';
import {ProfileComponent} from './component/management/profile/profile.component';
import {UsersTableComponent} from './component/management/users/users-table/users-table.component';
import {UserRowComponent} from './component/management/users/users-table/user-row/user-row.component';
import {UserViewComponent} from './component/management/users/user-view/user-view.component';
import {UserEditComponent} from './component/management/users/user-edit/user-edit.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    UserComponent,
    ManagementComponent,
    UsersComponent,
    SettingsComponent,
    ProfileComponent,
    UsersTableComponent,
    UserRowComponent,
    UserViewComponent,
    UserEditComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    NotificationModule,
    AppRoutingModule,
    FormsModule
  ],
  providers: [AuthenticationGuard, AuthenticationService, UserService,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
