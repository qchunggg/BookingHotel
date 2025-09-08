import { PageFilter } from "./page-filter.model";

export interface HotelResponse {
    id:          number;
    name:        string;
    address:     string;
    city:        string;
    description: string;
    rating:      number;
    totalRooms:  number;
    hotelImage?: string;
    userId:      number;
}

export interface HotelFilter extends PageFilter {
  keyword?:   string;
  city?:      string;
  minRating?: number;
}

export interface HotelCreate {
  name:        string;
  address:     string;
  city:        string;
  description?: string;

  rating:      number;            // BE yêu cầu, nên để bắt buộc
  hotelImage?: string;
  totalRooms:  number;

  payosClientId:   string;
  payosApiKey:     string;
  payosChecksumKey:string;

  userId:      number;
}

export interface HotelUpdate {
  id:          number;
  name:        string;
  address:     string;
  city:        string;
  description?: string;

  rating:      number;            // BE yêu cầu, nên để bắt buộc
  hotelImage?: string;
  totalRooms:  number;

  payosClientId:   string;
  payosApiKey:     string;
  payosChecksumKey:string;

  userId:      number;
}