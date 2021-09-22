import {Role} from "../enum/role.enum";

export class User {
  userId: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  profileImageUrl: string;
  lastLoginDateDisplay: Date;
  joinDate: Date;
  role: Role; //ROLE_USER, ROLE_ADMIN
  authorities: string[];
  active: boolean;
  notLocked: boolean;
}
