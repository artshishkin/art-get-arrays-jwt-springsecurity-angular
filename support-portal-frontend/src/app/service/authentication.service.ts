import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient, HttpErrorResponse, HttpResponse} from "@angular/common/http";
import {UserLogin} from "../dto/user-login";
import {Observable} from "rxjs";
import {User} from "../model/user";

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
    this.storage.removeItem("jwt-token");
    this.storage.removeItem("user");
    this.storage.removeItem("users");
  }
}
