import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from "../../../service/authentication.service";
import {NgForm} from "@angular/forms";
import {CustomHttpResponse} from "../../../dto/custom-http-response";
import {NotificationType} from "../../../notification/notification-type";
import {HttpErrorResponse} from "@angular/common/http";
import {SubSink} from "subsink";
import {UserService} from "../../../service/user.service";
import {NotificationService} from "../../../service/notification.service";

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit, OnDestroy {

  public refreshing: boolean = false;

  private subs = new SubSink();

  constructor(private userService: UserService,
              private notificationService: NotificationService,
              private authenticationService: AuthenticationService) {
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  public get isAdmin(): boolean {
    return this.authenticationService.isLoggedUserHasRoleAdmin();
  }

  public onResetPassword(emailForm: NgForm): void {
    this.refreshing = true;
    let email = emailForm.value['reset-password-email'];
    this.subs.sink = this.userService.resetPassword(email)
      .subscribe(
        (response: CustomHttpResponse) => {
          this.notificationService.notify(NotificationType.SUCCESS, response.message);
        },
        (errorResponse: HttpErrorResponse) => {
          this.notificationService.notify(NotificationType.WARNING, errorResponse.error.message);
          this.refreshing = false;
        },
        () => {
          this.refreshing = false;
          emailForm.reset();
        }
      );
  }
}
