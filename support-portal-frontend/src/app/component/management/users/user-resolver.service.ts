import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {User} from "../../../model/user";
import {Observable} from "rxjs";
import {UserService} from "../../../service/user.service";

@Injectable({
  providedIn: 'root'
})
export class UserResolver implements Resolve<User> {

  constructor(private userService: UserService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<User> | Promise<User> | User {
    const id = route.params['id'];
    return this.userService.findUserById(id);
  }
}
