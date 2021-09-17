import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {AuthenticationService} from "../../service/authentication.service";
import {NotificationService} from "../../service/notification.service";
import {NotificationType} from "../../notification/notification-type";
import {Subscription} from "rxjs";
import {UserLogin} from "../../dto/user-login";
import {HttpErrorResponse, HttpResponse} from "@angular/common/http";
import {User} from "../../model/user";
import {HeaderType} from "../../enum/header-type.enum";

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
      .subscribe((response: HttpResponse<User>) => {

          const token = response.headers.get(HeaderType.JWT_TOKEN);
          this.authenticationService.saveToken(token!);

          this.authenticationService.addUserToLocalStorage(response.body!);

          this.router.navigateByUrl('/user/management');
          this.showLoading = false;
        },
        (errorResponse: HttpErrorResponse) => {
          console.log(errorResponse);
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
    this.notificationService.notify(NotificationType.ERROR, message ? message : 'AN ERROR OCCURRED. PLEASE TRY AGAIN')
  }
}
