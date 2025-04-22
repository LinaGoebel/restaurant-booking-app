import React from 'react';
import { Reservation } from '../types';
import { Card, CardContent } from './ui/Card';
import Button from './ui/Button';
import StatusBadge from './StatusBadge';
import { Calendar, Clock, Users, MapPin } from 'lucide-react';

interface ReservationCardProps {
  reservation: Reservation;
  onEdit?: (id: string) => void;
  onCancel?: (id: string) => void;
  onDelete?: (id: string) => void;
  showActions?: boolean;
}

const ReservationCard: React.FC<ReservationCardProps> = ({
  reservation,
  onEdit,
  onCancel,
  onDelete,
  showActions = true,
}) => {
  const { id, customerName, date, time, guests, status, tableId } = reservation;
  
  const formattedDate = new Date(date).toLocaleDateString('en-US', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
  
  return (
    <Card className="transition-all hover:shadow-md">
      <CardContent className="p-4">
        <div className="flex justify-between items-start">
          <div>
            <h3 className="font-medium text-slate-900">{customerName}</h3>
            <p className="text-sm text-slate-500">Res. #{id.substring(0, 6)}</p>
          </div>
          <StatusBadge status={status} />
        </div>
        
        <div className="mt-4 space-y-2">
          <div className="flex items-center gap-2 text-sm">
            <Calendar size={16} className="text-slate-400" />
            <span>{formattedDate}</span>
          </div>
          
          <div className="flex items-center gap-2 text-sm">
            <Clock size={16} className="text-slate-400" />
            <span>{time}</span>
          </div>
          
          <div className="flex items-center gap-2 text-sm">
            <Users size={16} className="text-slate-400" />
            <span>{guests} guests</span>
          </div>
          
          <div className="flex items-center gap-2 text-sm">
            <MapPin size={16} className="text-slate-400" />
            <span>Table {tableId}</span>
          </div>
        </div>
        
        {showActions && status !== 'cancelled' && (
          <div className="mt-4 flex gap-2">
            {onEdit && (
              <Button 
                variant="outline" 
                size="sm" 
                onClick={() => onEdit(id)}
                className="flex-1"
              >
                Edit
              </Button>
            )}
            
            {onCancel && status !== 'cancelled' && (
              <Button 
                variant="danger" 
                size="sm" 
                onClick={() => onCancel(id)}
                className="flex-1"
              >
                Cancel
              </Button>
            )}
          </div>
        )}
        
        {showActions && status === 'cancelled' && onDelete && (
          <div className="mt-4">
            <Button 
              variant="ghost" 
              size="sm" 
              onClick={() => onDelete(id)}
              className="text-red-600 w-full"
            >
              Delete
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default ReservationCard;