import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ChangePassword, UserFilter, UserResponse, UserUpdate } from '../models/user.model';
import { Observable } from 'rxjs';
import { Page } from '../models/page-filter.model';

const BASE = 'http://localhost:8080/api/users';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private http: HttpClient) {}

  search(filter: UserFilter): Observable<Page<UserResponse>> {
    return this.http.post<Page<UserResponse>>(`${BASE}/search`, filter);
  }

  getAll(): Observable<UserResponse[]> {
    return this.http.post<UserResponse[]>(`${BASE}/all`, {});
  }

  update(user: UserUpdate): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${BASE}/update-user`, user);
  }

  deleteMany(ids: number[]): Observable<void> {
    return this.http.post<void>(`${BASE}/delete-users`, ids);
  }

   changePassword(id: number, dto: ChangePassword): Observable<void> {
    return this.http.post<void>(`${BASE}/change-password/${id}`, dto);
  }
}
