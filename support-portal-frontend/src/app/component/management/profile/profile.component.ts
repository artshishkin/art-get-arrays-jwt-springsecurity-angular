import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../../../service/authentication.service";
import {User} from "../../../model/user";
import {FileUploadStatus} from "../../../model/file-upload.status";
import {NotificationType} from "../../../notification/notification-type";
import {HttpErrorResponse, HttpEvent, HttpEventType} from "@angular/common/http";
import {SubSink} from "subsink";
import {UserService} from "../../../service/user.service";
import {NotificationService} from "../../../service/notification.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  public profileImageFileName: string | null;
  public profileImage: File | null;

  public loggedInUser: User;
  public refreshing: boolean;
  private subs = new SubSink();

  public fileUploadStatus: FileUploadStatus = new FileUploadStatus();

  constructor(private userService: UserService,
              private notificationService: NotificationService,
              private authenticationService: AuthenticationService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.loggedInUser = this.authenticationService.getUserFromLocalStorage();
  }

  public get isAdmin(): boolean {
    return this.authenticationService.isLoggedUserHasRoleAdmin();
  }


  public updateProfileImage(): void {
    this.clickButton('profile-image-input');
  }

  private clickButton(buttonId: string): void {
    document.getElementById(buttonId)?.click();
  }

  onUpdateCurrentUser(user: User) {
    const userId = this.authenticationService.getUserFromLocalStorage().userId;
    this.refreshing = true;

    if (user.role == undefined) user.role = this.loggedInUser.role;
    if (user.active == undefined) user.active = this.loggedInUser.active;
    if (user.notLocked == undefined) user.notLocked = this.loggedInUser.notLocked;

    const formData = this.userService.createUserFormData(user, this.profileImage);
    this.subs.sink = this.userService.updateUser(userId, formData)
      .subscribe(
        (user: User) => {
          this.authenticationService.addUserToLocalStorage(user);
          this.invalidateVariables();
          this.notificationService.notify(NotificationType.SUCCESS, `User ${user.username} updated successfully`);
          this.refreshing = false;
        },
        (errorResponse: HttpErrorResponse) => {
          this.notificationService.notify(NotificationType.ERROR, errorResponse.error.message);
          this.refreshing = false;
        }
      );
  }

  onLogOut() {
    this.authenticationService.logout();
    this.router.navigate(['/login']);
    this.notificationService.notify(NotificationType.SUCCESS, 'You have been successfully logged out');
  }

  private invalidateVariables(): void {
    this.profileImage = null;
    this.profileImageFileName = null;
  }

  public onUpdateProfileImage(): void {
    if (!this.profileImage) return;
    this.refreshing = true;
    const formData = new FormData();
    formData.append("profileImage", this.profileImage);
    let user = this.authenticationService.getUserFromLocalStorage();
    this.subs.sink = this.userService.updateProfileImage(user.userId, formData)
      .subscribe(
        (event: HttpEvent<any>) => {
          this.reportUploadProgress(event);
        },
        (errorResponse: HttpErrorResponse) => {
          this.notificationService.notify(NotificationType.ERROR, errorResponse.error.message);
          this.refreshing = false;
          this.fileUploadStatus.status = 'error';
        },
        () => {
          this.refreshing = false;
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
          this.notificationService.notify(NotificationType.ERROR, 'Unable to upload image. Please try again');
        }
        break;
      default:
        this.fileUploadStatus.status = 'default';
    }
  }

  public onProfileImageChange(fileList: FileList): void {
    this.profileImageFileName = fileList[0].name;
    this.profileImage = fileList[0];
  }
}
