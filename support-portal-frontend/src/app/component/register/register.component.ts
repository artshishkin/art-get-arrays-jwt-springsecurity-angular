import {Component, OnInit} from '@angular/core';
import {User} from "../../model/user";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  public showLoading: boolean;

  constructor() {
  }

  ngOnInit(): void {
  }

  public onRegister(user: User) {

  }

}
