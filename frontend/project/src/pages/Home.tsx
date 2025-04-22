import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Utensils, Calendar, Clock, Users } from 'lucide-react';
import Button from '../components/ui/Button';
import { Card, CardContent } from '../components/ui/Card';

const Home: React.FC = () => {
  const navigate = useNavigate();
  
  return (
    <div>
      {/* Hero Section */}
      <section className="relative bg-blue-900 text-white">
        <div 
          className="absolute inset-0 bg-cover bg-center opacity-20" 
          style={{ 
            backgroundImage: "url('https://images.pexels.com/photos/941861/pexels-photo-941861.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2')" 
          }}
        ></div>
        <div className="relative max-w-7xl mx-auto px-4 py-24 sm:px-6 lg:px-8 flex flex-col items-center text-center">
          <h1 className="text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl mb-6">
            Reserve Your Perfect Dining Experience
          </h1>
          <p className="max-w-xl mx-auto text-lg opacity-90 mb-8">
            Elegant table booking for your special moments. Quick, simple, and refined reservations.
          </p>
          <Button 
            size="lg" 
            variant="secondary"
            onClick={() => navigate('/book')}
          >
            Book a Table
          </Button>
        </div>
      </section>
      
      {/* How It Works Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-slate-900">How It Works</h2>
            <p className="mt-4 text-lg text-slate-600 max-w-2xl mx-auto">
              Three simple steps to reserve your table with minimal effort.
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Card className="text-center">
              <CardContent className="p-6">
                <div className="w-12 h-12 mx-auto rounded-full bg-blue-100 flex items-center justify-center mb-4">
                  <Calendar className="h-6 w-6 text-blue-900" />
                </div>
                <h3 className="text-xl font-medium text-slate-900 mb-2">Choose Date & Time</h3>
                <p className="text-slate-600">
                  Select your preferred date and time for your reservation.
                </p>
              </CardContent>
            </Card>
            
            <Card className="text-center">
              <CardContent className="p-6">
                <div className="w-12 h-12 mx-auto rounded-full bg-blue-100 flex items-center justify-center mb-4">
                  <Users className="h-6 w-6 text-blue-900" />
                </div>
                <h3 className="text-xl font-medium text-slate-900 mb-2">Party Size & Table</h3>
                <p className="text-slate-600">
                  Tell us how many guests and choose your ideal table.
                </p>
              </CardContent>
            </Card>
            
            <Card className="text-center">
              <CardContent className="p-6">
                <div className="w-12 h-12 mx-auto rounded-full bg-blue-100 flex items-center justify-center mb-4">
                  <Utensils className="h-6 w-6 text-blue-900" />
                </div>
                <h3 className="text-xl font-medium text-slate-900 mb-2">Confirm & Enjoy</h3>
                <p className="text-slate-600">
                  Receive confirmation instantly and enjoy your dining experience.
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>
      
      {/* CTA Section */}
      <section className="py-16 bg-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col items-center text-center">
          <h2 className="text-3xl font-bold text-slate-900 mb-6">Ready to Book Your Table?</h2>
          <p className="max-w-2xl text-lg text-slate-600 mb-8">
            Make a reservation now and secure your spot for a memorable dining experience.
          </p>
          <Button 
            size="lg" 
            onClick={() => navigate('/book')}
          >
            Reserve Now
          </Button>
        </div>
      </section>
      
      {/* Gallery Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-slate-900">Our Restaurant</h2>
            <p className="mt-4 text-lg text-slate-600 max-w-2xl mx-auto">
              Explore our elegant dining spaces and atmosphere.
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div className="aspect-video overflow-hidden rounded-lg">
              <img 
                src="https://images.pexels.com/photos/67468/pexels-photo-67468.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2" 
                alt="Restaurant interior" 
                className="w-full h-full object-cover transition-transform hover:scale-105 duration-300"
              />
            </div>
            <div className="aspect-video overflow-hidden rounded-lg">
              <img 
                src="https://images.pexels.com/photos/262978/pexels-photo-262978.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2" 
                alt="Private dining" 
                className="w-full h-full object-cover transition-transform hover:scale-105 duration-300"
              />
            </div>
            <div className="aspect-video overflow-hidden rounded-lg">
              <img 
                src="https://images.pexels.com/photos/696218/pexels-photo-696218.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2" 
                alt="Outdoor seating" 
                className="w-full h-full object-cover transition-transform hover:scale-105 duration-300"
              />
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;