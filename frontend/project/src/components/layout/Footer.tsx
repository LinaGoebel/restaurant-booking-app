import React from 'react';
import { Utensils, Instagram, Facebook, Twitter } from 'lucide-react';

const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-900 text-slate-300">
      <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <div className="flex items-center">
              <Utensils className="h-8 w-8 text-amber-400" />
              <span className="ml-2 text-xl font-semibold text-white">ReservEase</span>
            </div>
            <p className="mt-2 text-sm">
              Elegant restaurant booking solution for fine dining establishments.
              Make your table reservations quick and easy.
            </p>
            <div className="mt-4 flex space-x-4">
              <a href="#" className="text-slate-400 hover:text-white">
                <span className="sr-only">Instagram</span>
                <Instagram size={20} />
              </a>
              <a href="#" className="text-slate-400 hover:text-white">
                <span className="sr-only">Facebook</span>
                <Facebook size={20} />
              </a>
              <a href="#" className="text-slate-400 hover:text-white">
                <span className="sr-only">Twitter</span>
                <Twitter size={20} />
              </a>
            </div>
          </div>
          
          <div>
            <h3 className="text-white font-medium mb-4">Quick Links</h3>
            <ul className="space-y-2">
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition-colors">
                  Home
                </a>
              </li>
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition-colors">
                  Make a Reservation
                </a>
              </li>
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition-colors">
                  Menu
                </a>
              </li>
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition-colors">
                  About Us
                </a>
              </li>
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition-colors">
                  Contact
                </a>
              </li>
            </ul>
          </div>
          
          <div>
            <h3 className="text-white font-medium mb-4">Contact Information</h3>
            <address className="not-italic">
              <p className="mb-2">123 Gourmet Street</p>
              <p className="mb-2">Culinary District, CP 10000</p>
              <p className="mb-4">Fine City</p>
              <p className="mb-2">
                <span className="block text-sm text-slate-400">Email:</span>
                <a href="mailto:info@reservease.com" className="text-slate-300 hover:text-white">
                  info@reservease.com
                </a>
              </p>
              <p>
                <span className="block text-sm text-slate-400">Phone:</span>
                <a href="tel:+1234567890" className="text-slate-300 hover:text-white">
                  +1 (234) 567-890
                </a>
              </p>
            </address>
          </div>
        </div>
        <div className="mt-8 pt-8 border-t border-slate-700 text-center text-sm text-slate-400">
          &copy; {new Date().getFullYear()} ReservEase. All rights reserved.
        </div>
      </div>
    </footer>
  );
};

export default Footer;