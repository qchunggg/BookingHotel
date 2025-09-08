import { RoomStatus } from "../enums/room-status.enum";
import { RoomType } from "../enums/room-type.enum";
import { PageFilter } from "./page-filter.model";

export interface RoomResponse {
    id: number;
    roomNumber: string;
    roomType: RoomType;
    pricePerDay: number;
    roomStatus: RoomStatus;
    roomImage?: string;
    hotelId: number;
    name: string;
}

export interface RoomFilter extends PageFilter {
    hotelId?:          number;        
    name?:             string;        
    roomStatus?:       RoomStatus;
    roomType?:         RoomType;
    minPricePerDay?:   number;
    maxPricePerDay?:   number;
    checkIn?:          string | Date;
    checkOut?:         string | Date;

}

export interface RoomCreate {
  roomNumber:   string;
  roomType:     RoomType;
  pricePerDay:  number;
  roomStatus:   RoomStatus;
  roomImage?:   string;
  hotelId:      number;
}

export interface RoomUpdate {
    id:           number;
    roomNumber:   string;
    roomType:     RoomType;
    pricePerDay:  number;
    roomStatus:   RoomStatus;
    roomImage?:   string;
    hotelId:      number;
}