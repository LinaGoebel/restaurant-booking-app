import React from 'react';
import { TimeSlot } from '../../types';

interface TimeSlotPickerProps {
  timeSlots: TimeSlot[];
  selectedTime: string;
  onTimeSelect: (time: string) => void;
}

const TimeSlotPicker: React.FC<TimeSlotPickerProps> = ({
  timeSlots,
  selectedTime,
  onTimeSelect,
}) => {
  return (
    <div className="space-y-2">
      <h3 className="text-sm font-medium text-slate-700">Select Time</h3>
      <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-5">
        {timeSlots.map((slot) => (
          <button
            key={slot.id}
            onClick={() => slot.available && onTimeSelect(slot.time)}
            disabled={!slot.available}
            className={`
              h-10 rounded-md text-sm transition-colors
              ${selectedTime === slot.time ? 'bg-blue-900 text-white' : ''}
              ${!slot.available ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-white border border-slate-300 hover:bg-slate-50'}
            `}
          >
            {slot.time}
          </button>
        ))}
      </div>
    </div>
  );
};

export default TimeSlotPicker;