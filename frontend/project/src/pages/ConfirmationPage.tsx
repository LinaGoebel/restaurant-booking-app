import React from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle, ArrowRight, Calendar, Clock, Users, MapPin } from 'lucide-react';
import Button from '../components/ui/Button';
import { Card, CardContent } from '../components/ui/Card';
import { useReservation } from '../context/ReservationContext';

const ConfirmationPage: React.FC = () => {
  const navigate = useNavigate();
  const { reservations } = useReservation();
  
  // Get the latest reservation
  const reservation = [...reservations].sort((a, b) => 
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )[0];
  
  if (!reservation) {
    navigate('/book');
    return null;
  }
  
  const { customerName, date, time, guests, tableId, id } = reservation;
  
  const formattedDate = new Date(date).toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
  
  return (
    <div className="max-w-3xl mx-auto px-4 py-12 sm:px-6 lg:px-8">
      <div className="text-center mb-8">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-100 mb-4">
          <CheckCircle className="h-8 w-8 text-green-600" />
        </div>
        <h1 className="text-3xl font-bold text-slate-900">Reservation Confirmed</h1>
        <p className="mt-2 text-lg text-slate-600">
          Thank you, {customerName}! Your table has been reserved.
        </p>
      </div>
      
      <Card className="mb-8">
        <CardContent className="p-6">
          <h2 className="text-xl font-semibold text-slate-900 mb-4">Reservation Details</h2>
          
          <div className="space-y-3">
            <div className="flex items-center">
              <Calendar className="h-5 w-5 text-blue-900 mr-3" />
              <span className="text-slate-700">{formattedDate}</span>
            </div>
            
            <div className="flex items-center">
              <Clock className="h-5 w-5 text-blue-900 mr-3" />
              <span className="text-slate-700">{time}</span>
            </div>
            
            <div className="flex items-center">
              <Users className="h-5 w-5 text-blue-900 mr-3" />
              <span className="text-slate-700">{guests} {guests === 1 ? 'Guest' : 'Guests'}</span>
            </div>
            
            <div className="flex items-center">
              <MapPin className="h-5 w-5 text-blue-900 mr-3" />
              <span className="text-slate-700">Table {tableId}</span>
            </div>
          </div>
          
          <div className="mt-6 pt-6 border-t border-slate-200">
            <div className="flex items-center">
              <span className="text-sm text-slate-500">Reservation ID:</span>
              <span className="ml-2 text-sm font-medium text-slate-700">#{id.substring(0, 8)}</span>
            </div>
          </div>
        </CardContent>
      </Card>
      
      <div className="text-center space-y-4">
        <p className="text-slate-600">
          You'll receive a confirmation email shortly. If you need to modify or cancel your reservation, 
          please contact us at least 24 hours in advance.
        </p>
        
        <div className="flex flex-col sm:flex-row justify-center gap-4">
          <Button
            variant="outline"
            onClick={() => navigate('/reservations')}
            className="flex items-center justify-center"
          >
            View My Reservations
            <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
          
          <Button
            onClick={() => navigate('/')}
            className="flex items-center justify-center"
          >
            Return to Home
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationPage;