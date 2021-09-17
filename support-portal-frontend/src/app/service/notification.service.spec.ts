import {TestBed} from '@angular/core/testing';

import {NotificationService} from './notification.service';
import {NotifierService} from "angular-notifier";
import {NotificationType} from "../notification/notification-type";

describe('NotificationService', () => {
  let service: NotificationService;
  let notifierSpy: jasmine.SpyObj<NotifierService>;

  let notifierServiceSpy = jasmine.createSpyObj('NotifierService', ['notify']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{provide: NotifierService, useValue: notifierServiceSpy}]
    });
    service = TestBed.inject(NotificationService);
    notifierSpy = TestBed.inject(NotifierService) as jasmine.SpyObj<NotifierService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it("should call NotifierService's `notify` method with `info` message type when `notify` method is called with INFO notification type", () => {

    service.notify(NotificationType.INFO, "Dummy msg");

    expect(notifierSpy.notify).toHaveBeenCalledWith("info", "Dummy msg");

  });

  it("should call NotifierService's `notify` method with `default` message type when `notify` method is called with DEFAULT notification type", () => {

    service.notify(NotificationType.DEFAULT, "Dummy msg");

    expect(notifierSpy.notify).toHaveBeenCalledWith("default", "Dummy msg");

  });
});
