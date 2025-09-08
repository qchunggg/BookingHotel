import { RoomStatus } from "../enums/room-status.enum";
import { PageFilter } from "./page-filter.model";

export interface BookingResponse {
    id:            number;
    userId:        number;
    fullName:      string;
    roomId:        number;
    checkInDate:  string;
    checkOutDate: string;
    totalPrice:    number;
    pricePerDay:   number;
    depositAmount: number;
    bookingStatus:  string;
    roomStatus:  RoomStatus;

}

export interface BookingFilter extends PageFilter {
    userId?:        number;
    fullName?:      string;
    checkInDate?:  string;
    checkOutDate?: string;
}

export interface BookingCreate {
    userId:        number;
    roomId:        number;
    checkInDate:  string;
    checkOutDate: string;
}

export interface BookingUpdate {
    id:            number;
    userId:        number;
    roomId:        number;
    checkInDate:  string;
    checkOutDate: string;
}

