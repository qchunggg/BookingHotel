import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { UserCreate, UserResponse } from '../models/user.model';
import { HttpClient } from '@angular/common/http';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { HotelFilter, HotelResponse } from '../models/hotel.model';
import { Page } from '../models/page-filter.model';
import { RoomFilter, RoomResponse } from '../models/room.model';

const BASE = 'http://localhost:8080/auth';
const TOKEN_KEY = 'JWT';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

    /** Giữ thông tin user hiện tại */
  private _user$ = new BehaviorSubject<UserResponse | null>(null);
  user$          = this._user$.asObservable();

  constructor(private http: HttpClient) {}

  register(dto: UserCreate): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${BASE}/register`, dto);
  }

  /** POST /auth/login */
  login(dto: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${BASE}/login`, dto).pipe(
      tap(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
        this._user$.next(res.userInfo);
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${BASE}/logout`, {}).pipe(
      tap(() => {
        localStorage.removeItem(TOKEN_KEY);
        this._user$.next(null);
      })
    );
  }

  getToken(): string | null { return localStorage.getItem(TOKEN_KEY); }
  isLoggedIn(): boolean      { return !!this.getToken(); }

  searchHotels(f: HotelFilter): Observable<Page<HotelResponse>> {
    return this.http.post<Page<HotelResponse>>(`${BASE}/search-hotels`, f);
  }

  /** POST /auth/detail-hotel/{id} */
  hotelDetail(id: number): Observable<HotelResponse> {
    return this.http.post<HotelResponse>(`${BASE}/detail-hotel/${id}`, {});
  }

  /** POST /auth/search-rooms */
  searchRooms(f: RoomFilter): Observable<Page<RoomResponse>> {
    return this.http.post<Page<RoomResponse>>(`${BASE}/search-rooms`, f);
  }

  /** POST /auth/hotel/{hotelId} */
  roomsByHotel(hotelId: number): Observable<RoomResponse[]> {
    return this.http.post<RoomResponse[]>(`${BASE}/hotel/${hotelId}`, {});
  }

  /** POST /auth/detail-room/{id} */
  roomDetail(id: number): Observable<RoomResponse> {
    return this.http.post<RoomResponse>(`${BASE}/detail-room/${id}`, {});
  }
}
