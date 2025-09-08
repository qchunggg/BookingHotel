import { Routes } from "@angular/router";
import { Booking } from "./pages/booking/booking";
import { DetailHotel } from "./pages/hotels/detail-hotel/detail-hotel";
import { Hotels } from "./pages/hotels/hotels";
import { Login } from "./pages/login/login";
import { Register } from "./pages/register/register";
import { DetailRoom } from "./pages/rooms/detail-room/detail-room";
import { Rooms } from "./pages/rooms/rooms";

export const routes: Routes = [
    { path: '', component: Hotels },       // trang chủ
    { path: 'hotels', component: Hotels },
    { path: 'hotel/:id', component: DetailHotel },

    { path: 'rooms', component: Rooms },
    { path: 'room/:id', component: DetailRoom },

    { path: 'booking', component: Booking },  // sẽ canActivate sau
    { path: 'login', component: Login },
    { path: 'register', component: Register },

    { path: '**', redirectTo: '' }
];