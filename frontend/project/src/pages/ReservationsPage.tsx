import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Filter, Calendar } from 'lucide-react';
import { useReservation } from '../context/ReservationContext';
import ReservationCard from '../components/ReservationCard';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';

const ReservationsPage: React.FC = () => {
  const navigate = useNavigate();
  const { reservations, updateReservation, deleteReservation } = useReservation();
  
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'confirmed' | 'pending' | 'cancelled'>('all');
  const [dateFilter, setDateFilter] = useState('');
  
  const handleEdit = (id: string) => {
    // In a real application, this would navigate to an edit form
    // For now, we'll just log the action
    console.log('Edit reservation', id);
  };
  
  const handleCancel = (id: string) => {
    // Update reservation status to cancelled
    updateReservation(id, { status: 'cancelled' });
  };
  
  const handleDelete = (id: string) => {
    // Delete reservation completely
    deleteReservation(id);
  };
  
  // Filter and sort reservations
  const filteredReservations = reservations
    .filter(reservation => {
      // Search term filter
      const searchMatch = 
        reservation.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        reservation.id.toLowerCase().includes(searchTerm.toLowerCase());
      
      // Status filter
      const statusMatch = statusFilter === 'all' || reservation.status === statusFilter;
      
      // Date filter
      const dateMatch = !dateFilter || reservation.date === dateFilter;
      
      return searchMatch && statusMatch && dateMatch;
    })
    .sort((a, b) => {
      // Sort by date (newest first) and then by time
      const dateComparison = new Date(b.date).getTime() - new Date(a.date).getTime();
      if (dateComparison !== 0) return dateComparison;
      
      return a.time.localeCompare(b.time);
    });
  
  return (
    <div className="max-w-7xl mx-auto px-4 py-12 sm:px-6 lg:px-8">
      <div className="md:flex md:items-center md:justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Your Reservations</h1>
          <p className="mt-2 text-lg text-slate-600">
            Manage and view all your upcoming and past reservations
          </p>
        </div>
        
        <div className="mt-4 md:mt-0">
          <Button
            onClick={() => navigate('/book')}
            className="flex items-center"
          >
            <Plus className="mr-2 h-4 w-4" />
            New Reservation
          </Button>
        </div>
      </div>
      
      <Card className="mb-8">
        <CardHeader>
          <CardTitle>Filter Reservations</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div>
              <Input
                placeholder="Search by name or ID"
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
      
      {filteredReservations.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredReservations.map((reservation) => (
            <ReservationCard
              key={reservation.id}
              reservation={reservation}
              onEdit={handleEdit}
              onCancel={handleCancel}
              onDelete={handleDelete}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-12 bg-slate-50 rounded-lg border border-slate-200">
          <p className="text-lg text-slate-600">No reservations found</p>
          {searchTerm || statusFilter !== 'all' || dateFilter ? (
            <p className="mt-2 text-slate-500">Try clearing your filters</p>
          ) : (
            <Button
              variant="outline"
              className="mt-4"
              onClick={() => navigate('/book')}
            >
              Make your first reservation
            </Button>
          )}
        </div>
      )}
    </div>
  );
};

export default ReservationsPage;