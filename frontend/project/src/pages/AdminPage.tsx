import React, { useState } from 'react';
import { Calendar, Filter, Search, Users, Clock } from 'lucide-react';
import { useReservation } from '../context/ReservationContext';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '../components/ui/Card';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { Reservation } from '../types';

const AdminPage: React.FC = () => {
  const { currentUser } = useAuth();
  const { reservations, updateReservation, deleteReservation } = useReservation();
  
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'confirmed' | 'pending' | 'cancelled'>('all');
  const [dateFilter, setDateFilter] = useState('');
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);
  
  // Redirect if not admin
  if (!currentUser || currentUser.role !== 'admin') {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12 sm:px-6 lg:px-8 text-center">
        <h1 className="text-3xl font-bold text-slate-900 mb-4">Access Denied</h1>
        <p className="text-lg text-slate-600">
          You need administrator privileges to access this page.
        </p>
      </div>
    );
  }
  
  // Filter and sort reservations
  const filteredReservations = reservations
    .filter(reservation => {
      // Search term filter
      const searchMatch = 
        reservation.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        reservation.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        reservation.email.toLowerCase().includes(searchTerm.toLowerCase());
      
      // Status filter
      const statusMatch = statusFilter === 'all' || reservation.status === statusFilter;
      
      // Date filter
      const dateMatch = !dateFilter || reservation.date === dateFilter;
      
      return searchMatch && statusMatch && dateMatch;
    })
    .sort((a, b) => {
      // Sort by date (newest first)
      return new Date(b.date).getTime() - new Date(a.date).getTime();
    });
  
  const handleStatusChange = (id: string, status: 'confirmed' | 'pending' | 'cancelled') => {
    updateReservation(id, { status });
  };
  
  const handleDelete = (id: string) => {
    deleteReservation(id);
    if (selectedReservation && selectedReservation.id === id) {
      setSelectedReservation(null);
    }
  };
  
  const handleSelectReservation = (reservation: Reservation) => {
    setSelectedReservation(reservation);
  };
  
  return (
    <div className="max-w-7xl mx-auto px-4 py-12 sm:px-6 lg:px-8">
      <div className="md:flex md:items-center md:justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Admin Dashboard</h1>
          <p className="mt-2 text-lg text-slate-600">
            Manage all restaurant reservations
          </p>
        </div>
      </div>
      
      <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Card className="mb-6">
            <CardHeader>
              <CardTitle>Filter Reservations</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <div>
                  <Input
                    placeholder="Search by name, email or ID"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full"
                    prefix={<Search className="h-4 w-4 text-slate-400" />}
                  />
                </div>
                
                <div>
                  <div className="flex items-center space-x-2">
                    <Filter className="h-4 w-4 text-slate-400" />
                    <select
                      className="block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                      value={statusFilter}
                      onChange={(e) => setStatusFilter(e.target.value as any)}
                    >
                      <option value="all">All statuses</option>
                      <option value="confirmed">Confirmed</option>
                      <option value="pending">Pending</option>
                      <option value="cancelled">Cancelled</option>
                    </select>
                  </div>
                </div>
                
                <div>
                  <div className="flex items-center space-x-2">
                    <Calendar className="h-4 w-4 text-slate-400" />
                    <input
                      type="date"
                      className="block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                      value={dateFilter}
                      onChange={(e) => setDateFilter(e.target.value)}
                    />
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
          
          <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 sm:rounded-lg">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Guest
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Date & Time
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Party
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Table
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-200">
                {filteredReservations.length > 0 ? (
                  filteredReservations.map((reservation) => (
                    <tr 
                      key={reservation.id} 
                      className={`cursor-pointer hover:bg-slate-50 ${
                        selectedReservation?.id === reservation.id ? 'bg-blue-50' : ''
                      }`}
                      onClick={() => handleSelectReservation(reservation)}
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="font-medium text-slate-900">{reservation.customerName}</div>
                        <div className="text-sm text-slate-500">{reservation.email}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-slate-900">{reservation.date}</div>
                        <div className="text-sm text-slate-500">{reservation.time}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-900">
                        {reservation.guests} guests
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <StatusBadge status={reservation.status} />
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-900">
                        Table {reservation.tableId}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="px-6 py-12 text-center text-sm text-slate-500">
                      No reservations found
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
        
        <div>
          <Card className="sticky top-8">
            {selectedReservation ? (
              <>
                <CardHeader>
                  <CardTitle>Reservation Details</CardTitle>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div>
                    <h3 className="text-sm font-medium text-slate-500">Guest Information</h3>
                    <div className="mt-2">
                      <p className="text-base font-medium text-slate-900">{selectedReservation.customerName}</p>
                      <p className="text-sm text-slate-600">{selectedReservation.email}</p>
                      <p className="text-sm text-slate-600">{selectedReservation.phone}</p>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <h3 className="text-sm font-medium text-slate-500 flex items-center">
                        <Calendar className="h-4 w-4 mr-1" /> Date
                      </h3>
                      <p className="mt-1 text-base text-slate-900">{selectedReservation.date}</p>
                    </div>
                    
                    <div>
                      <h3 className="text-sm font-medium text-slate-500 flex items-center">
                        <Clock className="h-4 w-4 mr-1" /> Time
                      </h3>
                      <p className="mt-1 text-base text-slate-900">{selectedReservation.time}</p>
                    </div>
                    
                    <div>
                      <h3 className="text-sm font-medium text-slate-500 flex items-center">
                        <Users className="h-4 w-4 mr-1" /> Party Size
                      </h3>
                      <p className="mt-1 text-base text-slate-900">{selectedReservation.guests} guests</p>
                    </div>
                    
                    <div>
                      <h3 className="text-sm font-medium text-slate-500">Table</h3>
                      <p className="mt-1 text-base text-slate-900">Table {selectedReservation.tableId}</p>
                    </div>
                  </div>
                  
                  {selectedReservation.specialRequests && (
                    <div>
                      <h3 className="text-sm font-medium text-slate-500">Special Requests</h3>
                      <p className="mt-1 text-sm text-slate-600 whitespace-pre-line">{selectedReservation.specialRequests}</p>
                    </div>
                  )}
                  
                  <div>
                    <h3 className="text-sm font-medium text-slate-500">Status</h3>
                    <div className="mt-2 flex space-x-2">
                      <Button 
                        size="sm" 
                        variant={selectedReservation.status === 'confirmed' ? 'primary' : 'outline'}
                        onClick={() => handleStatusChange(selectedReservation.id, 'confirmed')}
                      >
                        Confirm
                      </Button>
                      <Button 
                        size="sm" 
                        variant={selectedReservation.status === 'pending' ? 'primary' : 'outline'}
                        onClick={() => handleStatusChange(selectedReservation.id, 'pending')}
                      >
                        Pending
                      </Button>
                      <Button 
                        size="sm" 
                        variant={selectedReservation.status === 'cancelled' ? 'danger' : 'outline'}
                        onClick={() => handleStatusChange(selectedReservation.id, 'cancelled')}
                      >
                        Cancel
                      </Button>
                    </div>
                  </div>
                </CardContent>
                <CardFooter>
                  <Button 
                    variant="ghost" 
                    className="text-red-600 w-full flex justify-center"
                    onClick={() => handleDelete(selectedReservation.id)}
                  >
                    Delete Reservation
                  </Button>
                </CardFooter>
              </>
            ) : (
              <CardContent className="p-12 text-center">
                <p className="text-slate-500">Select a reservation to view details</p>
              </CardContent>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
};

export default AdminPage;