import {Component, OnDestroy, OnInit} from '@angular/core';
import {BehaviorSubject, Subscription} from "rxjs";
import {User} from "../../model/user";
import {UserService} from "../../service/user.service";
import {NotificationService} from "../../service/notification.service";
import {NotificationType} from "../../notification/notification-type";
import {HttpErrorResponse} from "@angular/common/http";
import {NgForm} from "@angular/forms";

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
  public profileImageFileName: string | null;
  public profileImage: File | null;

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
    this.clickButton('openUserInfo');
  }

  public onProfileImageChange(fileList: FileList): void {
    this.profileImageFileName = fileList[0].name;
    this.profileImage = fileList[0];
  }

  private sendErrorNotification(message: string) {
    this.notificationService.notify(NotificationType.ERROR, message ? message : 'An error occurred. Please try again')
  }

  public onAddNewUser(userForm: NgForm): void {
    let formData = this.userService.createUserFormData(null, userForm.value, this.profileImage);
    let subscription = this.userService.addUser(formData)
      .subscribe(
        (user: User) => {
          this.clickButton('new-user-close');
          this.getUsers(false);
          this.invalidateVariables();
          userForm.reset();
          this.notificationService.notify(NotificationType.SUCCESS, `User ${user.username} added successfully`);
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendErrorNotification(errorResponse.error.message);
        }
      );
    this.subscriptions.push(subscription);
  }

  private invalidateVariables(): void {
    this.profileImage = null;
    this.profileImageFileName = null;
  }

  public saveNewUser(): void {
    this.clickButton('new-user-save');
  }

  public searchUsers(searchTerm: string): void {

    if (!searchTerm) {
      this.users = this.userService.getUsersFromLocalStorage();
      return;
    }

    const matchUsers: User[] = [];
    searchTerm = searchTerm.toLowerCase();
    for (const user of this.userService.getUsersFromLocalStorage()) {
      if (user.username.toLowerCase().includes(searchTerm)
        || user.userId.toLowerCase().includes(searchTerm)
        || user.firstName.toLowerCase().includes(searchTerm)
        || user.lastName.toLowerCase().includes(searchTerm)
        || user.email.toLowerCase().includes(searchTerm)) {
        matchUsers.push(user);
      }
    }
    this.users = matchUsers;

  }

  private clickButton(buttonId: string): void {
    document.getElementById(buttonId)?.click();
  }
}
