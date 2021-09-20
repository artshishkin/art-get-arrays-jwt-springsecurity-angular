import {Component, OnDestroy, OnInit} from '@angular/core';
import {User} from "../../model/user";
import {Router} from "@angular/router";
import {AuthenticationService} from "../../service/authentication.service";
import {NotificationService} from "../../service/notification.service";
import {Subscription} from "rxjs";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationType} from "../../notification/notification-type";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit, OnDestroy {

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
    }
  }

  public onRegister(user: User) {
    this.showLoading = true;

    let subscription = this.authenticationService
      .register(user)
      .subscribe(user => {
          this.notificationService.notify(NotificationType.SUCCESS, `A new account was created for ${user.firstName}.
          Please check your email for password to log in`);
          this.router.navigateByUrl('/login');
          this.showLoading = false;
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendErrorNotification(errorResponse.error.message);
          this.showLoading = false;
        }
      );

    this.subscriptions.push(subscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  private sendErrorNotification(message: string) {
    this.notificationService.notify(NotificationType.ERROR, message ? message : 'An error occurred. Please try again')
  }
}
