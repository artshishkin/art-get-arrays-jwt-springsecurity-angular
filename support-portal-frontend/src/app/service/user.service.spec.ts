import {TestBed} from '@angular/core/testing';

import {UserService} from './user.service';
import {HttpClient} from "@angular/common/http";

describe('UserService', () => {
  let service: UserService;

  beforeEach(() => {
    let httpClientSpy = jasmine.createSpyObj('HttpClient', ['']);

    TestBed.configureTestingModule({
      providers: [{provide: HttpClient, useValue: httpClientSpy}]
    });
    service = TestBed.inject(UserService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
