import {Component, OnDestroy, OnInit} from '@angular/core';
import {BehaviorSubject, Subscription} from "rxjs";
import {User} from "../../model/user";
import {UserService} from "../../service/user.service";
import {NotificationService} from "../../service/notification.service";
import {NotificationType} from "../../notification/notification-type";
import {HttpErrorResponse} from "@angular/common/http";
import {NgForm} from "@angular/forms";
import {CustomHttpResponse} from "../../dto/custom-http-response";
import {AuthenticationService} from "../../service/authentication.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit, OnDestroy {

  private titleSubject = new BehaviorSubject<string>('Users');
  public titleAction$ = this.titleSubject.asObservable();

  public users: User[] = [];

  public loggedInUser: User;

  public refreshing: boolean;
  private subscriptions: Subscription[] = [];
  public selectedUser: User;
  public profileImageFileName: string | null;
  public profileImage: File | null;
  public editUser: User = new User();
  private currentUsername: string;

  constructor(private userService: UserService,
              private notificationService: NotificationService,
              private authenticationService: AuthenticationService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.getUsers(true);
    this.loggedInUser = this.authenticationService.getUserFromLocalStorage();
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
    this, this.sendNotification(NotificationType.ERROR, message);
  }

  private sendNotification(type: NotificationType, message: string) {
    this.notificationService.notify(type, message ? message : 'An error occurred. Please try again')
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

  public onEditUser(user: User): void {
    this.editUser = user;
    this.currentUsername = user.username;
    this.clickButton('openUserEdit');
  }

  public onUpdateUser(): void {
    const formData = this.userService.createUserFormData(this.currentUsername, this.editUser, this.profileImage);
    let subscription = this.userService.updateUser(formData)
      .subscribe(
        (user: User) => {
          this.clickButton('closeEditUserButton');
          this.getUsers(false);
          this.invalidateVariables();
          this.notificationService.notify(NotificationType.SUCCESS, `User ${user.username} updated successfully`);
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendErrorNotification(errorResponse.error.message);
        }
      );
    this.subscriptions.push(subscription);
  }

  onDeleteUser(user: User) {
    const subscription = this.userService.deleteUser(user.userId)
      .subscribe(
        (response: CustomHttpResponse) => {
          this.getUsers(false);
          this.invalidateVariables();
          this.notificationService.notify(NotificationType.SUCCESS, response.message);
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendErrorNotification(errorResponse.error.message);
        }
      );
    this.subscriptions.push(subscription);
  }

  public onResetPassword(emailForm: NgForm): void {
    this.refreshing = true;
    let email = emailForm.value['reset-password-email'];
    let subscription = this.userService.resetPassword(email)
      .subscribe(
        (response: CustomHttpResponse) => {
          this.notificationService.notify(NotificationType.SUCCESS, response.message);
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendNotification(NotificationType.WARNING, errorResponse.error.message);
          this.refreshing = false;
        },
        () => {
          this.refreshing = false;
          emailForm.reset();
        }
      );
    this.subscriptions.push(subscription);
  }

  onUpdateCurrentUser(user: User) {
    this.currentUsername = this.authenticationService.getUserFromLocalStorage().username;
    this.refreshing = true;
    const formData = this.userService.createUserFormData(this.currentUsername, user, this.profileImage);
    let subscription = this.userService.updateUser(formData)
      .subscribe(
        (user: User) => {
          this.authenticationService.addUserToLocalStorage(user);
          this.getUsers(false);
          this.invalidateVariables();
          this.notificationService.notify(NotificationType.SUCCESS, `User ${user.username} updated successfully`);
          this.refreshing = false;
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendErrorNotification(errorResponse.error.message);
          this.refreshing = false;
        }
      );
    this.subscriptions.push(subscription);
  }

  onLogOut() {
    this.authenticationService.logout();
    this.router.navigate(['/login']);
    this.sendNotification(NotificationType.SUCCESS, 'You have been successfully logged out');
  }

  public updateProfileImage(): void {
    this.clickButton('profile-image-input');
  }

  public onUpdateProfileImage(): void {

  }
}
