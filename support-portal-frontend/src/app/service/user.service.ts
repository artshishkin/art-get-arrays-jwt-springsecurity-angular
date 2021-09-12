import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../model/user";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private host: string = environment.apiUrl;

  constructor(private httpClient: HttpClient) {
  }

  public getAllUsers(): Observable<UserPage | HttpErrorResponse> {
    return this.httpClient
      .get<UserPage>(`${this.host}/user`);
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
