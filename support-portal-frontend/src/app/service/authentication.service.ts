import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient, HttpErrorResponse, HttpResponse} from "@angular/common/http";
import {UserLogin} from "../dto/user-login";
import {Observable} from "rxjs";
import {User} from "../model/user";
import {JwtHelperService} from "@auth0/angular-jwt";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  public USER_STORAGE_KEY = "user";
  public JWT_TOKEN_STORAGE_KEY = "jwt-token";

  private host: string = environment.apiUrl;
  private token: string | null;
  private loggedInUser: string | null;
  private storage = localStorage;

  //first install this module: `npm install @auth0/angular-jwt`
  private jwtHelper: JwtHelperService = new JwtHelperService();

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
    this.storage.removeItem(this.JWT_TOKEN_STORAGE_KEY);
    this.storage.removeItem(this.USER_STORAGE_KEY);
    this.storage.removeItem("users");
  }

  public saveToken(token: string): void {
    this.token = token;
    this.storage.setItem(this.JWT_TOKEN_STORAGE_KEY, token);
  }

  public loadToken(): void {
    this.token = this.storage.getItem(this.JWT_TOKEN_STORAGE_KEY);
  }

  public addUserToLocalStorage(user: User) {
    this.storage.setItem(this.USER_STORAGE_KEY, JSON.stringify(user));
  }

  public getUserFromLocalStorage(): User {
    let userJson = this.storage.getItem(this.USER_STORAGE_KEY);
    return JSON.parse(userJson!);
  }

  public getToken(): string | null {
    return this.token;
  }

  public isLoggedIn(): boolean {
    this.loadToken();
    if (this.token != null && this.token !== '') {
      let subject = this.jwtHelper.decodeToken(this.token).sub;
      if (subject != null || '') {
        if (!this.jwtHelper.isTokenExpired(this.token!)) {
          this.loggedInUser = subject;
          return true;
        }
      }
    }
    this.logout()
    return false;
  }

}
