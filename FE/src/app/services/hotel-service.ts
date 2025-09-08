import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HotelCreate, HotelFilter, HotelResponse, HotelUpdate } from '../models/hotel.model';
import { Observable } from 'rxjs';

const BASE = 'http://localhost:8080/api/hotels';
@Injectable({
  providedIn: 'root'
})
export class HotelService {
    constructor(private http: HttpClient) {}

  create(payload: HotelCreate): Observable<HotelResponse> {
    return this.http.post<HotelResponse>(`${BASE}/create-hotel`, payload);
  }

  update(payload: HotelUpdate): Observable<HotelResponse> {
    return this.http.post<HotelResponse>(`${BASE}/update-hotel`, payload);
  }

  /** POST /api/hotels/delete-hotels  (ADMIN, MANAGER) */
  delete(ids: number[]): Observable<void> {
    return this.http.post<void>(`${BASE}/delete-hotels`, ids);
  }

  getByUser(filter: HotelFilter & { userId: number }): Observable<any> {
    return this.http.post<any>(`${BASE}/get-by-user`, filter);
  }

  detail(id: number): Observable<HotelResponse> {
    return this.http.get<HotelResponse>(`${BASE}/${id}`);
  }
}
