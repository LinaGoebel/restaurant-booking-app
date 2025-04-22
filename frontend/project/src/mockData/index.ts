import { Reservation, Table, TimeSlot, User } from '../types';

export const mockTables: Table[] = [
  { id: '1', name: 'Table 1', capacity: 2, location: 'window', isAvailable: true },
  { id: '2', name: 'Table 2', capacity: 4, location: 'window', isAvailable: true },
  { id: '3', name: 'Table 3', capacity: 4, location: 'center', isAvailable: true },
  { id: '4', name: 'Table 4', capacity: 6, location: 'center', isAvailable: true },
  { id: '5', name: 'Table 5', capacity: 2, location: 'bar', isAvailable: true },
  { id: '6', name: 'Table 6', capacity: 8, location: 'private', isAvailable: true },
  { id: '7', name: 'Table 7', capacity: 2, location: 'outdoor', isAvailable: true },
  { id: '8', name: 'Table 8', capacity: 4, location: 'outdoor', isAvailable: true },
];

export const mockTimeSlots: TimeSlot[] = [
  { id: '1', time: '17:00', available: true },
  { id: '2', time: '17:30', available: true },
  { id: '3', time: '18:00', available: true },
  { id: '4', time: '18:30', available: false },
  { id: '5', time: '19:00', available: true },
  { id: '6', time: '19:30', available: false },
  { id: '7', time: '20:00', available: true },
  { id: '8', time: '20:30', available: true },
  { id: '9', time: '21:00', available: true },
];

export const mockReservations: Reservation[] = [
  {
    id: '1',
    customerName: 'Anna Schmidt',
    email: 'anna@example.com',
    phone: '+1234567890',
    date: '2025-04-15',
    time: '19:00',
    guests: 2,
    tableId: '1',
    status: 'confirmed',
    createdAt: '2025-04-10T10:23:00Z',
  },
  {
    id: '2',
    customerName: 'Thomas Meyer',
    email: 'thomas@example.com',
    phone: '+1234567891',
    date: '2025-04-15',
    time: '20:00',
    guests: 4,
    tableId: '3',
    specialRequests: 'Birthday celebration',
    status: 'confirmed',
    createdAt: '2025-04-09T14:45:00Z',
  },
  {
    id: '3',
    customerName: 'Sofia Becker',
    email: 'sofia@example.com',
    phone: '+1234567892',
    date: '2025-04-16',
    time: '18:30',
    guests: 6,
    tableId: '4',
    status: 'pending',
    createdAt: '2025-04-11T09:15:00Z',
  },
  {
    id: '4',
    customerName: 'David Fischer',
    email: 'david@example.com',
    phone: '+1234567893',
    date: '2025-04-14',
    time: '19:30',
    guests: 2,
    tableId: '7',
    specialRequests: 'Outdoor seating preferred',
    status: 'cancelled',
    createdAt: '2025-04-08T16:30:00Z',
  },
];

export const mockUsers: User[] = [
  { id: '1', name: 'Admin User', role: 'admin' },
  { id: '2', name: 'Staff Member', role: 'staff' },
  { id: '3', name: 'Customer', role: 'customer' },
];