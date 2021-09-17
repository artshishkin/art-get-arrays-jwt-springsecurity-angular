import {Injectable} from '@angular/core';
import {NotifierService} from "angular-notifier";
import {NotificationType} from "../notification/notification-type";

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private notifier: NotifierService) {
  }

  public notify(type: NotificationType, message: string): void {
    this.notifier.notify(type, message);
  }

}
