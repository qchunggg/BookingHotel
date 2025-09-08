import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RoomCreate, RoomResponse, RoomUpdate } from '../models/room.model';

const BASE = 'http://localhost:8080/api/rooms';
@Injectable({
  providedIn: 'root'
})
export class RoomService {
  constructor(private http: HttpClient) {}

  /* ------------ CREATE ------------ */
  /** POST /api/rooms/create-room  */
  create(payload: RoomCreate): Observable<RoomResponse> {
    return this.http.post<RoomResponse>(`${BASE}/create-room`, payload);
  }

  update(payload: RoomUpdate): Observable<RoomResponse> {
    return this.http.post<RoomResponse>(`${BASE}/update-room`, payload);
  }

  deleteMany(ids: number[]): Observable<void> {
    return this.http.post<void>(`${BASE}/delete-rooms`, ids);
  }
}
