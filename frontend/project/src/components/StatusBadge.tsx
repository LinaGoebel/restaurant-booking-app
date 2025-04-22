import React from 'react';
import Badge from './ui/Badge';

interface StatusBadgeProps {
  status: 'confirmed' | 'pending' | 'cancelled';
}

const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  switch (status) {
    case 'confirmed':
      return <Badge variant="success">Confirmed</Badge>;
    case 'pending':
      return <Badge variant="warning">Pending</Badge>;
    case 'cancelled':
      return <Badge variant="danger">Cancelled</Badge>;
    default:
      return null;
  }
};

export default StatusBadge;