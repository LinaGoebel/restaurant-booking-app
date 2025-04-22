import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Utensils, Calendar, Users, LogOut } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import Button from '../ui/Button';

const Header: React.FC = () => {
  const { currentUser, logout, isAuthenticated } = useAuth();
  const location = useLocation();
  
  const isActive = (path: string) => {
    return location.pathname === path;
  };
  
  return (
    <header className="bg-white shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center">
              <Utensils className="h-8 w-8 text-blue-900" />
              <span className="ml-2 text-xl font-semibold text-slate-900">ReservEase</span>
            </Link>
          </div>
          
          {isAuthenticated && (
            <nav className="hidden md:flex space-x-4">
              <Link
                to="/"
                className={`px-3 py-2 rounded-md text-sm font-medium ${
                  isActive('/') 
                    ? 'bg-blue-100 text-blue-900' 
                    : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                Home
              </Link>
              
              <Link
                to="/reservations"
                className={`px-3 py-2 rounded-md text-sm font-medium ${
                  isActive('/reservations') 
                    ? 'bg-blue-100 text-blue-900' 
                    : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                Reservations
              </Link>
              
              {currentUser?.role === 'admin' && (
                <Link
                  to="/admin"
                  className={`px-3 py-2 rounded-md text-sm font-medium ${
                    isActive('/admin') 
                      ? 'bg-blue-100 text-blue-900' 
                      : 'text-slate-600 hover:bg-slate-100'
                  }`}
                >
                  Admin
                </Link>
              )}
            </nav>
          )}
          
          <div className="flex items-center">
            {isAuthenticated ? (
              <div className="flex items-center gap-2">
                <span className="text-sm text-slate-600 hidden sm:inline-block">
                  {currentUser?.name}
                </span>
                <Button 
                  variant="ghost" 
                  size="sm" 
                  onClick={logout}
                  className="flex items-center"
                >
                  <LogOut size={16} className="mr-1" />
                  <span className="hidden sm:inline-block">Logout</span>
                </Button>
              </div>
            ) : (
              <div className="flex gap-2">
                <Button 
                  variant="outline" 
                  size="sm" 
                  onClick={() => {}}
                  className="hidden sm:flex"
                >
                  Sign up
                </Button>
                <Button 
                  variant="primary" 
                  size="sm" 
                  onClick={() => {}}
                >
                  Login
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;