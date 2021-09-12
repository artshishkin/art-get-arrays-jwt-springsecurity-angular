import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient, HttpErrorResponse, HttpEvent} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../model/user";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private host: string = environment.apiUrl;
  private storage = localStorage;

  constructor(private httpClient: HttpClient) {
  }

  public getAllUsers(): Observable<UserPage | HttpErrorResponse> {
    return this.httpClient
      .get<UserPage>(`${this.host}/user`);
  }

  public addUser(formData: FormData): Observable<User | HttpErrorResponse> {
    return this.httpClient
      .post<User | HttpErrorResponse>(`${this.host}/user/add`, formData);
  }

  public updateUser(formData: FormData): Observable<User | HttpErrorResponse> {
    let currentUsername = formData.get(`currentUsername`);
    return this.httpClient
      .put<User | HttpErrorResponse>(`${this.host}/user/${currentUsername}`, formData);
  }

  public resetPassword(email: string): Observable<HttpResponse> {
    return this.httpClient
      .post<HttpResponse>(`${this.host}/user/resetPassword/${email}`, null);
  }

  public updateProfileImage(username: string, formData: FormData): Observable<HttpEvent<User | HttpErrorResponse>> {
    return this.httpClient
      .put<User | HttpErrorResponse>(`${this.host}/user/${username}/profileImage`, formData,
        {
          reportProgress: true,
          observe: 'events'
        });
  }

  public deleteUser(userId: string): Observable<HttpResponse | HttpErrorResponse> {
    return this.httpClient
      .delete<HttpResponse | HttpErrorResponse>(`${this.host}/user/${userId}`);
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

export interface HttpResponse {
  timestamp: Date;
  httpStatusCode: number;
  httpStatus: string;
  reason: string;
  message: string;
}

