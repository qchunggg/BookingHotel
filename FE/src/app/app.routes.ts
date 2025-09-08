import { Routes } from "@angular/router";
import { Booking } from "./pages/booking/booking";
import { DetailHotel } from "./pages/hotels/detail-hotel/detail-hotel";
import { Hotels } from "./pages/hotels/hotels";

import { DetailRoom } from "./pages/rooms/detail-room/detail-room";
import { Rooms } from "./pages/rooms/rooms";
import { Login } from "./pages/auth/login/login";
import { Register } from "./pages/auth/register/register";
import { authGuardFn } from "./core/guards/auth-guard";
import { CreateHotel } from "./pages/hotels/create-hotel/create-hotel";
import { UpdateHotel } from "./pages/hotels/update-hotel/update-hotel";
import { CreateRoom } from "./pages/rooms/create-room/create-room";
import { UpdateRoom } from "./pages/rooms/update-room/update-room";
import { roleGuard } from "./core/guards/role-guard-guard";
import { UserRole } from "./enums/user-role.enum";
import { Home } from './pages/home/home';

export const routes: Routes = [
    /* ---------- PUBLIC ---------- */
    { path: '', component: Home },
    { path: 'hotels', component: Hotels },
    { path: 'hotel/:id', component: DetailHotel },

    { path: 'rooms', component: Rooms },
    { path: 'room/:id', component: DetailRoom },

    { path: 'login', component: Login },
    { path: 'register', component: Register },

    /* ---------- PRIVATE (need login) ---------- */
    { path: 'booking', component: Booking, canActivate: [authGuardFn] },

    /* ---------- MANAGER hoặc ADMIN ---------- */
    { path: 'my-hotels', component: Hotels, canActivate: [roleGuard([UserRole.MANAGER, UserRole.ADMIN])] },
    { path: 'my-hotels/new', component: CreateHotel, canActivate: [roleGuard([UserRole.MANAGER, UserRole.ADMIN])] },
    { path: 'my-hotels/:id/edit', component: UpdateHotel, canActivate: [roleGuard([UserRole.MANAGER, UserRole.ADMIN])] },

    { path: 'my-rooms', component: Rooms, canActivate: [roleGuard([UserRole.MANAGER, UserRole.ADMIN])] },
    { path: 'my-rooms/new', component: CreateRoom, canActivate: [roleGuard([UserRole.MANAGER, UserRole.ADMIN])] },
    { path: 'my-rooms/:id/edit', component: UpdateRoom, canActivate: [roleGuard([UserRole.MANAGER, UserRole.ADMIN])] },

    /* Quản lý booking (lễ tân / admin) */
    //   { path: 'manage-bookings',      component: BookingManage, canActivate: [authGuardFn] },

    /* Quản lý người dùng (admin) */
    //   { path: 'admin/users',          component: UserManage,    canActivate: [authGuardFn] },

    /* Fallback */
    { path: '**', redirectTo: '' },
];