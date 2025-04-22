export interface Reservation {
  id: string;
  customerName: string;
  email: string;
  phone: string;
  date: string;
  time: string;
  guests: number;
  tableId: string;
  specialRequests?: string;
  status: 'confirmed' | 'pending' | 'cancelled';
  createdAt: string;
}

export interface Table {
  id: string;
  name: string;
  capacity: number;
  location: 'window' | 'center' | 'bar' | 'outdoor' | 'private';
  isAvailable: boolean;
}

export interface TimeSlot {
  id: string;
  time: string;
  available: boolean;
}

export interface User {
  id: string;
  name: string;
  role: 'admin' | 'staff' | 'customer';
}