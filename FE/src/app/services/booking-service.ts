import { Injectable } from '@angular/core';
import { Page } from '../models/page-filter.model';
import { BookingCreate, BookingFilter, BookingResponse, BookingUpdate } from '../models/booking.model';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const BASE = 'http://localhost:8080/api/bookings';
@Injectable({
  providedIn: 'root'
})
export class BookingService {
  constructor(private http: HttpClient) {}

  search(filter: BookingFilter): Observable<Page<BookingResponse>> {
    /** BE yêu cầu body = BookingFilterDTO (page, limit, sort + tiêu chí) */
    return this.http.post<Page<BookingResponse>>(`${BASE}/search`, filter);
  }

  detail(id: number): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${BASE}/${id}`, {});
  }

  create(dto: BookingCreate): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${BASE}/create-booking`, dto);
  }

  update(dto: BookingUpdate): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${BASE}/update-booking`, dto);
  }
  cancel(id: number): Observable<void> {
    return this.http.post<void>(`${BASE}/delete-booking/${id}`, {});
  }

  confirm(id: number)  : Observable<BookingResponse> { return this.http.post<BookingResponse>(`${BASE}/confirm/${id}`,  {}); }
  checkIn(id: number)  : Observable<BookingResponse> { return this.http.post<BookingResponse>(`${BASE}/check-in/${id}`, {}); }
  checkOut(id: number) : Observable<BookingResponse> { return this.http.post<BookingResponse>(`${BASE}/check-out/${id}`, {}); }

  /* ------------ FILTER SHORTCUTS ------------ */
  byUser(userId: number): Observable<BookingResponse[]> {
    return this.http.post<BookingResponse[]>(`${BASE}/user/${userId}`, {});
  }
  byRoom(roomId: number): Observable<BookingResponse[]> {
    return this.http.post<BookingResponse[]>(`${BASE}/room/${roomId}`, {});
  }

  getAll(): Observable<BookingResponse[]> {
    return this.http.post<BookingResponse[]>(`${BASE}/get-all-booking`, {});
  }
}
