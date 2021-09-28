import {TestBed} from '@angular/core/testing';

import {AuthInterceptor} from './auth.interceptor';
import {AuthenticationService} from "../service/authentication.service";

describe('AuthInterceptor', () => {
  beforeEach(() => {

    const spy = jasmine.createSpyObj('AuthenticationService', ['loadToken']);

    TestBed.configureTestingModule({
      providers: [
        AuthInterceptor,
        {provide: AuthenticationService, useValue: spy}
      ]
    })
  });

  it('should be created', () => {
    const interceptor: AuthInterceptor = TestBed.inject(AuthInterceptor);
    expect(interceptor).toBeTruthy();
  });

  it('should not intercept public URLs', () => {
    const interceptor: AuthInterceptor = TestBed.inject(AuthInterceptor);

    expect(interceptor.isRequestUrlMatches('http://google.com')).toBeFalsy();
    expect(interceptor.isRequestUrlMatches('http://example.com:8080/user')).toBeFalsy();
    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user/login')).toBeFalsy();
    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user/register')).toBeFalsy();
    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user/d470296c-97fb-44ce-9253-69f3331d7b77/profile-image/avatar.jpg')).toBeFalsy();
    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user/d470296c-97fb-44ce-9253-69f3331d7b77/profile-image')).toBeFalsy();
  });

  it('should intercept NON-public URLs', () => {
    const interceptor: AuthInterceptor = TestBed.inject(AuthInterceptor);

    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user/art.shyshkin')).toBeTruthy();
    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user')).toBeTruthy();
    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user/login/some')).toBeTruthy();
    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user/art.shyshkin/profileImage')).toBeTruthy();
    expect(interceptor.isRequestUrlMatches('http://localhost:8080/user/123mn1mn3m1')).toBeTruthy();

  });

});
