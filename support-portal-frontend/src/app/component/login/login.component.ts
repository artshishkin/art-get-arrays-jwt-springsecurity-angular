import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {AuthenticationService} from "../../service/authentication.service";
import {NotificationService} from "../../service/notification.service";
import {NotificationType} from "../../notification/notification-type";
import {Subscription} from "rxjs";
import {UserLogin} from "../../dto/user-login";
import {HttpErrorResponse, HttpResponse} from "@angular/common/http";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {

  public showLoading: boolean;
  private subscriptions: Subscription[] = [];

  constructor(private router: Router,
              private authenticationService: AuthenticationService,
              private notificationService: NotificationService) {
  }

  ngOnInit(): void {
    if (this.authenticationService.isUserLoggedIn()) {
      this.router.navigateByUrl("/user/management");
      this.notificationService.notify(NotificationType.INFO, "You are already logged in");
    } else {
      // this.router.navigateByUrl("/login");
    }
  }

  public onLogin(userLogin: UserLogin): void {
    this.showLoading = true;

    let subscription = this.authenticationService
      .login(userLogin)
      .subscribe((response: HttpResponse<any> | HttpErrorResponse) => {
        const token = response.headers.get("Jwt-Token");
      });

    this.subscriptions.push(subscription);
  }

  ngOnDestroy(): void {
  }

}
