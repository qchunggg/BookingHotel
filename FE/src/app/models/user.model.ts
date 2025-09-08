import { UserRole } from "../enums/user-role.enum";
import { PageFilter } from "./page-filter.model";

export interface UserResponse {
      id:       number;
  userName: string;
  fullName: string;
  email:    string;
  phone?:   string;
  role:     UserRole;
}

export interface UserFilter extends PageFilter {
  fullName?: string;
  phone?:    string;
  role?:     UserRole;
}

export interface UserCreate {
  userName: string;
  password: string;
  fullName: string;
  email:    string;
  phone?:   string;
}

export interface UserUpdate {
    id:       number;
  userName: string;
  password: string;
  fullName: string;
  email:    string;
  phone?:   string;
}

export interface ChangePassword {
  oldPassword: string;
  newPassword: string;
}


