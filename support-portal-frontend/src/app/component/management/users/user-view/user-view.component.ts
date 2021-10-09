import {Component, OnInit} from '@angular/core';
import {UserService} from "../../../../service/user.service";
import {User} from "../../../../model/user";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-user-view',
  templateUrl: './user-view.component.html',
  styleUrls: ['./user-view.component.css']
})
export class UserViewComponent implements OnInit {

  user: User;

  constructor(private userService: UserService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.data.subscribe((data) => this.user = data['user']);
    setTimeout(() => this.clickButton('openUserInfo'), 100);
  }

  private clickButton(buttonId: string): void {
    document.getElementById(buttonId)?.click();
  }

  onCloseModal() {
    this.router.navigate(['../../'], {relativeTo: this.route});
  }
}
