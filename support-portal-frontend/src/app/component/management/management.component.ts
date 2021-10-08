import {Component, OnInit} from '@angular/core';
import {Role} from "../../enum/role.enum";
import {User} from "../../model/user";
import {AuthenticationService} from "../../service/authentication.service";
import {Router} from "@angular/router";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'app-management',
  templateUrl: './management.component.html',
  styleUrls: ['./management.component.css']
})
export class ManagementComponent implements OnInit {

  public loggedInUser: User;
  private titleSubject = new BehaviorSubject<string>('Users');
  public titleAction$ = this.titleSubject.asObservable();

  constructor(private authenticationService: AuthenticationService,
              private router: Router) { }

  ngOnInit(): void {
    this.loggedInUser = this.authenticationService.getUserFromLocalStorage();
  }

  public changeTitle(title: string): void {
    this.titleSubject.next(title);
  }

  public get isAdmin(): boolean {
    return this.loggedInUser.role === Role.ADMIN || this.loggedInUser.role === Role.SUPER_ADMIN;
  }

  public get isManager(): boolean {
    return this.isAdmin || this.loggedInUser.role === Role.MANAGER;
  }
}
