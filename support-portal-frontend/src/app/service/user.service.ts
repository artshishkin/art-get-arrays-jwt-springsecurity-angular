import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient, HttpEvent} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../model/user";
import {CustomHttpResponse} from "../dto/custom-http-response";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private host: string = environment.apiUrl;
  private storage = localStorage;

  private selectedUser: User;

  constructor(private httpClient: HttpClient) {
  }

  public getAllUsers(): Observable<UserPage> {
    return this.httpClient
      .get<UserPage>(`${this.host}/user?size=2147483647`);
  }

  public addUser(formData: FormData): Observable<User> {
    return this.httpClient
      .post<User>(`${this.host}/user/add`, formData);
  }

  public updateUser(userId: string, formData: FormData): Observable<User> {
    return this.httpClient
      .put<User>(`${this.host}/user/${userId}`, formData);
  }

  public resetPassword(email: string): Observable<CustomHttpResponse> {
    return this.httpClient
      .post<CustomHttpResponse>(`${this.host}/user/resetPassword/${email}`, null);
  }

  public updateProfileImage(userId: string, formData: FormData): Observable<HttpEvent<User>> {
    return this.httpClient
      .put<User>(`${this.host}/user/${userId}/profile-image`, formData,
        {
          reportProgress: true,
          observe: 'events'
        });
  }

  public deleteUser(userId: string): Observable<CustomHttpResponse> {
    return this.httpClient
      .delete<CustomHttpResponse>(`${this.host}/user/${userId}`);
  }

  public addUsersToLocalStorage(users: User[]) {
    this.storage.setItem('users', JSON.stringify(users));
  }

  public getUsersFromLocalStorage(): User[] {
    let users = this.storage.getItem('users');
    if (users) {
      return JSON.parse(users);
    }
    return [];
  }

  public createUserFormData(user: User, profileImage: File | null): FormData {

    const formData = new FormData();

    formData.append('firstName', user.firstName);
    formData.append('lastName', user.lastName);
    formData.append("username", user.username);
    formData.append("email", user.email);
    formData.append("role", user.role);
    formData.append("active", user.active ? 'true' : 'false');
    formData.append("notLocked", user.notLocked ? 'true' : 'false');
    if (profileImage)
      formData.append("profileImage", profileImage);

    return formData;
  }

  public setSelectedUser(user: User): void {
    this.selectedUser = user;
  }

  public getSelectedUser(): User {
    return this.selectedUser;
  }

  public findUserById(id: string): User | Observable<User> {
    let cachedUsers = this.getUsersFromLocalStorage();
    const foundUser = cachedUsers.find((u) => u.userId === id);

    if (foundUser) return foundUser;

    return this.getAllUsers()
      .pipe(
        map((page: UserPage, idx: number) => page.content),
        map(users => users.find(u => u.userId === id)!)
      );
  }

}

export interface UserPage {

  content: User[];
  last: boolean;
  first: boolean;
  totalElements: number;
  size: number;
  numberOfElements: number;
  number: number;
  empty: boolean;

}



