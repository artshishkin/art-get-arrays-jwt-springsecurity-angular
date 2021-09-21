import {Component, OnDestroy, OnInit} from '@angular/core';
import {BehaviorSubject, Subscription} from "rxjs";
import {User} from "../../model/user";
import {UserService} from "../../service/user.service";
import {NotificationService} from "../../service/notification.service";
import {NotificationType} from "../../notification/notification-type";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit, OnDestroy {

  private titleSubject = new BehaviorSubject<string>('Users');
  public titleAction$ = this.titleSubject.asObservable();

  public users: User[] = [];
  public refreshing: boolean;
  private subscriptions: Subscription[] = [];
  public selectedUser: User;

  constructor(private userService: UserService,
              private notificationService: NotificationService) {
  }

  ngOnInit(): void {
    this.getUsers(true);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  public changeTitle(title: string): void {
    this.titleSubject.next(title);
  }

  public getUsers(showNotification: boolean) {
    this.refreshing = true;

    let subscription = this.userService.getAllUsers()
      .subscribe(
        usersPage => {
          this.users = usersPage.content;
          this.userService.addUsersToLocalStorage(this.users);
          if (showNotification)
            this.notificationService.notify(NotificationType.SUCCESS, `${this.users.length} users loaded successfully`)
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendErrorNotification(errorResponse.error.message);
        },
        () => {
          this.refreshing = false;
        }
      );
    this.subscriptions.push(subscription);

  }

  public onSelectUser(selectedUser: User): void {
    this.selectedUser = selectedUser;
    document.getElementById('openUserInfo')?.click();
  }

  private sendErrorNotification(message: string) {
    this.notificationService.notify(NotificationType.ERROR, message ? message : 'An error occurred. Please try again')
  }
}
