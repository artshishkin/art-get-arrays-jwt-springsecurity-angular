export class User {
  userId: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  profileImageUrl: string;
  lastLoginDate: Date;
  lastLoginDateDisplay: Date;
  joinDate: Date;
  role: string; //ROLE_USER, ROLE_ADMIN
  authorities: string[];
  isActive: boolean;
  isNotLocked: boolean;
}
