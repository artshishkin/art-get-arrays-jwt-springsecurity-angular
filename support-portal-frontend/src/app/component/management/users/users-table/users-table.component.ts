import {Component, OnDestroy, OnInit} from '@angular/core';
import {User} from "../../../../model/user";
import {NotificationType} from "../../../../notification/notification-type";
import {HttpErrorResponse} from "@angular/common/http";
import {SubSink} from "subsink";
import {UserService} from "../../../../service/user.service";
import {NotificationService} from "../../../../service/notification.service";
import {AuthenticationService} from "../../../../service/authentication.service";
import {Router} from "@angular/router";
import {CustomHttpResponse} from "../../../../dto/custom-http-response";

@Component({
  selector: 'app-users-table',
  templateUrl: './users-table.component.html',
  styleUrls: ['./users-table.component.css']
})
export class UsersTableComponent implements OnInit, OnDestroy {

  public users: User[] = [];

  public refreshing: boolean;
  private subs = new SubSink();

  constructor(private userService: UserService,
              private notificationService: NotificationService,
              private authenticationService: AuthenticationService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.getUsers(true);
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  public getUsers(showNotification: boolean) {
    this.refreshing = true;

    this.subs.sink = this.userService.getAllUsers()
      .subscribe(
        usersPage => {
          this.users = usersPage.content;
          this.userService.addUsersToLocalStorage(this.users);
          if (showNotification)
            this.notificationService.notify(NotificationType.SUCCESS, `${this.users.length} users loaded successfully`)
        },
        (errorResponse: HttpErrorResponse) => {
          this.notificationService.notify(NotificationType.ERROR, errorResponse.error.message);
          this.refreshing = false;
        },
        () => {
          this.refreshing = false;
        }
      );

  }

  public get isAdmin(): boolean {
    return this.authenticationService.isLoggedUserHasRoleAdmin();
  }

  onSelectUser(user: User) {
    console.log(`User ${user.username} is selected`);
  }

  onEditUser(user: User) {
    console.log(`User ${user.username} is clicked to be edited`);
  }

  onDeleteUser(user: User) {
    this.subs.sink = this.userService.deleteUser(user.userId)
      .subscribe(
        (response: CustomHttpResponse) => {
          this.getUsers(false);
          this.notificationService.notify(NotificationType.SUCCESS, response.message);
        },
        (errorResponse: HttpErrorResponse) => {
          this.notificationService.notify(NotificationType.ERROR, errorResponse.error.message);
        }
      );
  }
}
