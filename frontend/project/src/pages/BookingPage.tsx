import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useReservation } from '../context/ReservationContext';
import Calendar from '../components/ui/Calendar';
import TimeSlotPicker from '../components/ui/TimeSlotPicker';
import TableSelector from '../components/TableSelector';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '../components/ui/Card';

const BookingPage: React.FC = () => {
  const navigate = useNavigate();
  const { 
    timeSlots, 
    tables, 
    selectedDate, 
    selectedTime, 
    selectedTable,
    addReservation,
    setSelectedDate,
    setSelectedTime,
    setSelectedTable,
    getAvailableTables,
  } = useReservation();
  
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    customerName: '',
    email: '',
    phone: '',
    guests: 2,
    specialRequests: '',
  });
  
  const [availableTables, setAvailableTables] = useState(tables);
  const [errors, setErrors] = useState<Record<string, string>>({});
  
  useEffect(() => {
    if (selectedDate && selectedTime && formData.guests) {
      const available = getAvailableTables(selectedDate, selectedTime, formData.guests);
      setAvailableTables(available);
      
      // If the currently selected table is not available, reset it
      if (selectedTable && !available.some(table => table.id === selectedTable)) {
        setSelectedTable('');
      }
    }
  }, [selectedDate, selectedTime, formData.guests]);
  
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    
    if (name === 'guests') {
      const guests = parseInt(value) || 1;
      setFormData({ ...formData, [name]: Math.max(1, Math.min(12, guests)) });
    } else {
      setFormData({ ...formData, [name]: value });
    }
    
    // Clear error when field is updated
    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }
  };
  
  const validateStep = () => {
    const newErrors: Record<string, string> = {};
    
    if (step === 1) {
      if (!selectedDate) newErrors.date = 'Please select a date';
      if (!selectedTime) newErrors.time = 'Please select a time';
      if (!formData.guests) newErrors.guests = 'Please enter number of guests';
    } else if (step === 2) {
      if (!selectedTable) newErrors.table = 'Please select a table';
    } else if (step === 3) {
      if (!formData.customerName.trim()) newErrors.customerName = 'Name is required';
      if (!formData.email.trim()) {
        newErrors.email = 'Email is required';
      } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
        newErrors.email = 'Please enter a valid email';
      }
      if (!formData.phone.trim()) newErrors.phone = 'Phone number is required';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  const handleNext = () => {
    if (validateStep()) {
      setStep(step + 1);
    }
  };
  
  const handleBack = () => {
    setStep(step - 1);
  };
  
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (validateStep()) {
      addReservation({
        customerName: formData.customerName,
        email: formData.email,
        phone: formData.phone,
        date: selectedDate,
        time: selectedTime,
        guests: formData.guests,
        tableId: selectedTable,
        specialRequests: formData.specialRequests,
      });
      
      navigate('/confirmation');
    }
  };
  
  return (
    <div className="max-w-3xl mx-auto px-4 py-12 sm:px-6 lg:px-8">
      <div className="mb-8 text-center">
        <h1 className="text-3xl font-bold text-slate-900">Book a Table</h1>
        <p className="mt-2 text-lg text-slate-600">
          Reserve your table in just a few simple steps
        </p>
      </div>
      
      <div className="mb-8">
        <div className="flex items-center justify-between max-w-md mx-auto">
          {[1, 2, 3].map((stepNumber) => (
            <React.Fragment key={stepNumber}>
              <div className="flex flex-col items-center">
                <div 
                  className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-medium ${
                    step >= stepNumber 
                      ? 'bg-blue-900 text-white' 
                      : 'bg-slate-200 text-slate-600'
                  }`}
                >
                  {stepNumber}
                </div>
                <span className="mt-2 text-xs text-slate-600">
                  {stepNumber === 1 && 'Details'}
                  {stepNumber === 2 && 'Table'}
                  {stepNumber === 3 && 'Confirm'}
                </span>
              </div>
              
              {stepNumber < 3 && (
                <div className={`w-16 h-0.5 ${step > stepNumber ? 'bg-blue-900' : 'bg-slate-200'}`} />
              )}
            </React.Fragment>
          ))}
        </div>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>
            {step === 1 && 'Select Date & Party Size'}
            {step === 2 && 'Choose Your Table'}
            {step === 3 && 'Your Details'}
          </CardTitle>
        </CardHeader>
        
        <CardContent>
          <form onSubmit={handleSubmit}>
            {/* Step 1: Date, Time and Guests */}
            {step === 1 && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      Select Date
                    </label>
                    <Calendar 
                      selectedDate={selectedDate} 
                      onDateSelect={setSelectedDate} 
                    />
                    {errors.date && <p className="mt-1 text-sm text-red-600">{errors.date}</p>}
                  </div>
                  
                  <div className="space-y-6">
                    <div>
                      <TimeSlotPicker 
                        timeSlots={timeSlots} 
                        selectedTime={selectedTime} 
                        onTimeSelect={setSelectedTime} 
                      />
                      {errors.time && <p className="mt-1 text-sm text-red-600">{errors.time}</p>}
                    </div>
                    
                    <div>
                      <Input
                        type="number"
                        name="guests"
                        label="Number of Guests"
                        min={1}
                        max={12}
                        value={formData.guests}
                        onChange={handleInputChange}
                        error={errors.guests}
                      />
                    </div>
                  </div>
                </div>
              </div>
            )}
            
            {/* Step 2: Table Selection */}
            {step === 2 && (
              <div className="space-y-6">
                <p className="text-sm text-slate-600 mb-4">
                  Select a table for {formData.guests} guests on {selectedDate} at {selectedTime}
                </p>
                
                <TableSelector
                  tables={availableTables}
                  selectedTable={selectedTable}
                  onTableSelect={setSelectedTable}
                />
                
                {errors.table && <p className="mt-1 text-sm text-red-600">{errors.table}</p>}
                
                {availableTables.length === 0 && (
                  <div className="text-center p-4 bg-amber-50 border border-amber-200 rounded-md">
                    <p className="text-amber-800">
                      No tables available for {formData.guests} guests at this time.
                      Please select a different time or date.
                    </p>
                    <Button 
                      variant="outline" 
                      size="sm" 
                      onClick={() => setStep(1)}
                      className="mt-2"
                    >
                      Change Time or Date
                    </Button>
                  </div>
                )}
              </div>
            )}
            
            {/* Step 3: Customer Details */}
            {step === 3 && (
              <div className="space-y-4">
                <Input
                  name="customerName"
                  label="Full Name"
                  value={formData.customerName}
                  onChange={handleInputChange}
                  error={errors.customerName}
                />
                
                <Input
                  type="email"
                  name="email"
                  label="Email Address"
                  value={formData.email}
                  onChange={handleInputChange}
                  error={errors.email}
                />
                
                <Input
                  type="tel"
                  name="phone"
                  label="Phone Number"
                  value={formData.phone}
                  onChange={handleInputChange}
                  error={errors.phone}
                />
                
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">
                    Special Requests (Optional)
                  </label>
                  <textarea
                    name="specialRequests"
                    rows={3}
                    className="block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                    value={formData.specialRequests}
                    onChange={(e) => setFormData({ ...formData, specialRequests: e.target.value })}
                  />
                </div>
              </div>
            )}
          </form>
        </CardContent>
        
        <CardFooter className="flex justify-between">
          {step > 1 ? (
            <Button variant="outline" onClick={handleBack}>
              Back
            </Button>
          ) : (
            <div></div>
          )}
          
          {step < 3 ? (
            <Button onClick={handleNext}>
              Continue
            </Button>
          ) : (
            <Button onClick={handleSubmit}>
              Complete Reservation
            </Button>
          )}
        </CardFooter>
      </Card>
    </div>
  );
};

export default BookingPage;