import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient, HttpErrorResponse, HttpResponse} from "@angular/common/http";
import {UserLogin} from "../dto/user-login";
import {Observable} from "rxjs";
import {User} from "../model/user";

const USER_STORAGE_KEY = "user";
const JWT_TOKEN_STORAGE_KEY = "jwt-token";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private host: string = environment.apiUrl;
  private token: string | null;
  private loggedInUser: string | null;
  private storage = localStorage;

  constructor(private httpClient: HttpClient) {
  }

  public login(userDto: UserLogin): Observable<HttpResponse<any> | HttpErrorResponse> {
    return this.httpClient.post<HttpResponse<any> | HttpErrorResponse>
    (`${this.host}/user/login`, userDto, {observe: 'response'});
  }

  public register(user: User): Observable<User | HttpErrorResponse> {
    return this.httpClient
      .post<User | HttpErrorResponse>(`${this.host}/user/register`, user);
  }

  public logout(): void {
    this.token = null;
    this.loggedInUser = null;
    this.storage.removeItem(JWT_TOKEN_STORAGE_KEY);
    this.storage.removeItem(USER_STORAGE_KEY);
    this.storage.removeItem("users");
  }

  public saveToken(token: string): void {
    this.token = token;
    this.storage.setItem(JWT_TOKEN_STORAGE_KEY, token);
  }

  public loadToken(): void {
    this.token = this.storage.getItem(JWT_TOKEN_STORAGE_KEY);
  }

  public addUserToLocalStorage(user: User) {
    this.storage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  }

  public getUserFromLocalStorage(): User {
    let userJson = this.storage.getItem(USER_STORAGE_KEY);
    return JSON.parse(userJson!);
  }

}
