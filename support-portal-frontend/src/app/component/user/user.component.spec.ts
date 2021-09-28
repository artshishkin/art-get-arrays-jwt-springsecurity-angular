import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserComponent} from './user.component';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {NotificationService} from "../../service/notification.service";
import {AuthenticationService} from "../../service/authentication.service";

describe('UserComponent', () => {
  let component: UserComponent;
  let fixture: ComponentFixture<UserComponent>;

  beforeEach(async () => {

    let httpClientSpy = jasmine.createSpyObj('HttpClient', ['']);
    let routerSpy = jasmine.createSpyObj('Router', ['']);
    let notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['']);
    let authenticationServiceSpy = jasmine.createSpyObj('AuthenticationService', ['']);

    await TestBed.configureTestingModule({
      declarations: [UserComponent],
      providers: [
        {provide: HttpClient, useValue: httpClientSpy},
        {provide: Router, useValue: routerSpy},
        {provide: NotificationService, useValue: notificationServiceSpy},
        {provide: AuthenticationService, useValue: authenticationServiceSpy}
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
