import React, { createContext, useContext, useState } from 'react';
import { Reservation, Table, TimeSlot } from '../types';
import { mockReservations, mockTables, mockTimeSlots } from '../mockData';

interface ReservationContextType {
  reservations: Reservation[];
  tables: Table[];
  timeSlots: TimeSlot[];
  selectedDate: string;
  selectedTime: string;
  selectedTable: string;
  addReservation: (reservation: Omit<Reservation, 'id' | 'status' | 'createdAt'>) => void;
  updateReservation: (id: string, updates: Partial<Reservation>) => void;
  deleteReservation: (id: string) => void;
  setSelectedDate: (date: string) => void;
  setSelectedTime: (time: string) => void;
  setSelectedTable: (tableId: string) => void;
  getAvailableTables: (date: string, time: string, guests: number) => Table[];
  getReservation: (id: string) => Reservation | undefined;
}

const ReservationContext = createContext<ReservationContextType | undefined>(undefined);

export const ReservationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [reservations, setReservations] = useState<Reservation[]>(mockReservations);
  const [tables, setTables] = useState<Table[]>(mockTables);
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>(mockTimeSlots);
  const [selectedDate, setSelectedDate] = useState<string>('');
  const [selectedTime, setSelectedTime] = useState<string>('');
  const [selectedTable, setSelectedTable] = useState<string>('');

  const addReservation = (newReservation: Omit<Reservation, 'id' | 'status' | 'createdAt'>) => {
    const reservation: Reservation = {
      ...newReservation,
      id: Date.now().toString(),
      status: 'confirmed',
      createdAt: new Date().toISOString(),
    };
    
    setReservations([...reservations, reservation]);
    
    // Update table availability
    const updatedTables = tables.map(table => {
      if (table.id === newReservation.tableId) {
        return { ...table, isAvailable: false };
      }
      return table;
    });
    
    setTables(updatedTables);
  };

  const updateReservation = (id: string, updates: Partial<Reservation>) => {
    setReservations(
      reservations.map(reservation =>
        reservation.id === id ? { ...reservation, ...updates } : reservation
      )
    );
  };

  const deleteReservation = (id: string) => {
    const reservation = reservations.find(r => r.id === id);
    
    if (reservation) {
      // Update table availability if the reservation was confirmed
      if (reservation.status === 'confirmed') {
        const updatedTables = tables.map(table => {
          if (table.id === reservation.tableId) {
            return { ...table, isAvailable: true };
          }
          return table;
        });
        
        setTables(updatedTables);
      }
      
      setReservations(reservations.filter(reservation => reservation.id !== id));
    }
  };

  const getAvailableTables = (date: string, time: string, guests: number) => {
    // Check which tables are already reserved for this date and time
    const reservedTableIds = reservations
      .filter(r => r.date === date && r.time === time && r.status === 'confirmed')
      .map(r => r.tableId);

    // Return available tables that can accommodate the guest count
    return tables.filter(
      table => !reservedTableIds.includes(table.id) && table.capacity >= guests
    );
  };

  const getReservation = (id: string) => {
    return reservations.find(reservation => reservation.id === id);
  };

  return (
    <ReservationContext.Provider
      value={{
        reservations,
        tables,
        timeSlots,
        selectedDate,
        selectedTime,
        selectedTable,
        addReservation,
        updateReservation,
        deleteReservation,
        setSelectedDate,
        setSelectedTime,
        setSelectedTable,
        getAvailableTables,
        getReservation,
      }}
    >
      {children}
    </ReservationContext.Provider>
  );
};

export const useReservation = (): ReservationContextType => {
  const context = useContext(ReservationContext);
  if (context === undefined) {
    throw new Error('useReservation must be used within a ReservationProvider');
  }
  return context;
};