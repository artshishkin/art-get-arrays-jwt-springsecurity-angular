import {TestBed} from '@angular/core/testing';

import {AuthenticationGuard} from './authentication.guard';
import {HttpClient} from "@angular/common/http";
import {NotificationModule} from "../notification/notification.module";
import {RouterTestingModule} from "@angular/router/testing";

describe('AuthenticationGuard', () => {
  let guard: AuthenticationGuard;

  beforeEach(() => {

    let httpClientSpy = jasmine.createSpyObj('HttpClient', ['']);

    TestBed.configureTestingModule({
      imports: [NotificationModule, RouterTestingModule.withRoutes([])],
      providers: [
        {provide: HttpClient, useValue: httpClientSpy}
      ]
    });

    guard = TestBed.inject(AuthenticationGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
