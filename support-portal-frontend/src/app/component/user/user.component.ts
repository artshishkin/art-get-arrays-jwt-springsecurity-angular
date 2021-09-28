import {Component, OnDestroy, OnInit} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {User} from "../../model/user";
import {UserService} from "../../service/user.service";
import {NotificationService} from "../../service/notification.service";
import {NotificationType} from "../../notification/notification-type";
import {HttpErrorResponse, HttpEvent, HttpEventType} from "@angular/common/http";
import {NgForm} from "@angular/forms";
import {CustomHttpResponse} from "../../dto/custom-http-response";
import {AuthenticationService} from "../../service/authentication.service";
import {Router} from "@angular/router";
import {FileUploadStatus} from "../../model/file-upload.status";
import {Role} from "../../enum/role.enum";
import {SubSink} from "subsink";

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
  private subs = new SubSink();
  public selectedUser: User;
  public profileImageFileName: string | null;
  public profileImage: File | null;
  public editUser: User = new User();
  private currentUsername: string;

  public fileUploadStatus: FileUploadStatus = new FileUploadStatus();

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
    this.subs.unsubscribe();
  }

  public changeTitle(title: string): void {
    this.titleSubject.next(title);
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
          this.sendErrorNotification(errorResponse.error.message);
        },
        () => {
          this.refreshing = false;
        }
      );

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
    this.sendNotification(NotificationType.ERROR, message);
  }

  private sendNotification(type: NotificationType, message: string) {
    this.notificationService.notify(type, message ? message : 'An error occurred. Please try again')
  }

  public onAddNewUser(userForm: NgForm): void {
    let formData = this.userService.createUserFormData(null, userForm.value, this.profileImage);
    this.subs.sink = this.userService.addUser(formData)
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
    this.subs.sink = this.userService.updateUser(this.editUser.userId, formData)
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
  }

  onDeleteUser(user: User) {
    this.subs.sink = this.userService.deleteUser(user.userId)
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
          this.sendNotification(NotificationType.WARNING, errorResponse.error.message);
          this.refreshing = false;
        },
        () => {
          this.refreshing = false;
          emailForm.reset();
        }
      );
  }

  onUpdateCurrentUser(user: User) {
    this.currentUsername = this.authenticationService.getUserFromLocalStorage().username;
    const userId = this.authenticationService.getUserFromLocalStorage().userId;
    this.refreshing = true;

    if (user.role == undefined) user.role = this.loggedInUser.role;
    if (user.active == undefined) user.active = this.loggedInUser.active;
    if (user.notLocked == undefined) user.notLocked = this.loggedInUser.notLocked;

    console.log(user);
    console.log(this.loggedInUser);

    const formData = this.userService.createUserFormData(this.currentUsername, user, this.profileImage);
    this.subs.sink = this.userService.updateUser(userId, formData)
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
    if (!this.profileImage) return;
    this.refreshing = true;
    const formData = new FormData();
    formData.append("profileImage", this.profileImage);
    let user = this.authenticationService.getUserFromLocalStorage();
    this.subs.sink = this.userService.updateProfileImage(user.username, formData)
      .subscribe(
        (event: HttpEvent<any>) => {
          this.reportUploadProgress(event);
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendErrorNotification(errorResponse.error.message);
          this.refreshing = false;
          this.fileUploadStatus.status = 'error';
        },
        () => {
          this.refreshing = false;
          this.getUsers(false);
        }
      );
  }

  private reportUploadProgress(event: HttpEvent<any>): void {

    switch (event.type) {
      case HttpEventType.UploadProgress:
        this.fileUploadStatus.percentage = Math.round(100 * event.loaded / event.total!);
        this.fileUploadStatus.status = 'progress';
        break;
      case HttpEventType.Response:
        if (event.status === 200) {
          //for browser to fetch image when updating (because name left the same)
          this.loggedInUser.profileImageUrl = `${event.body.profileImageUrl}?time=${new Date().getTime()}`;
          this.notificationService.notify(NotificationType.SUCCESS, `${event.body.firstName}'s image updated successfully`);
          this.fileUploadStatus.status = 'done';
        } else {
          this.sendErrorNotification('Unable to upload image. Please try again');
        }
        break;
      default:
        this.fileUploadStatus.status = 'default';

    }
  }

  public get isAdmin(): boolean {
    return this.loggedInUser.role === Role.ADMIN || this.loggedInUser.role === Role.SUPER_ADMIN;
  }

  public get isManager(): boolean {
    return this.isAdmin || this.loggedInUser.role === Role.MANAGER;
  }
}
