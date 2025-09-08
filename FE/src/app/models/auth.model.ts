import { UserResponse } from "./user.model";

export interface LoginRequest {
  userName: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userInfo: UserResponse;
}
