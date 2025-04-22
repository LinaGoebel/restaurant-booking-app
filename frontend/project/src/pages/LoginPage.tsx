import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { LockIcon, UserIcon } from 'lucide-react';

const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  
  const [role, setRole] = useState<'admin' | 'staff' | 'customer'>('customer');
  
  const handleLogin = () => {
    login(role);
    navigate('/');
  };
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4 py-12 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-slate-900">Welcome Back</h1>
          <p className="mt-2 text-slate-600">Sign in to your account</p>
        </div>
        
        <Card>
          <CardHeader>
            <CardTitle>Sign In</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Input
                label="Email Address"
                type="email"
                placeholder="your.email@example.com"
                prefix={<UserIcon className="h-4 w-4 text-slate-400" />}
              />
            </div>
            
            <div>
              <Input
                label="Password"
                type="password"
                placeholder="••••••••"
                prefix={<LockIcon className="h-4 w-4 text-slate-400" />}
              />
            </div>
            
            <div>
              <label className="text-sm font-medium text-slate-700 block mb-2">
                Select Role (for demo)
              </label>
              <div className="space-y-2">
                <div className="flex items-center">
                  <input
                    id="role-admin"
                    name="role"
                    type="radio"
                    checked={role === 'admin'}
                    onChange={() => setRole('admin')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-slate-300"
                  />
                  <label htmlFor="role-admin" className="ml-2 block text-sm text-slate-700">
                    Admin
                  </label>
                </div>
                <div className="flex items-center">
                  <input
                    id="role-staff"
                    name="role"
                    type="radio"
                    checked={role === 'staff'}
                    onChange={() => setRole('staff')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-slate-300"
                  />
                  <label htmlFor="role-staff" className="ml-2 block text-sm text-slate-700">
                    Staff
                  </label>
                </div>
                <div className="flex items-center">
                  <input
                    id="role-customer"
                    name="role"
                    type="radio"
                    checked={role === 'customer'}
                    onChange={() => setRole('customer')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-slate-300"
                  />
                  <label htmlFor="role-customer" className="ml-2 block text-sm text-slate-700">
                    Customer
                  </label>
                </div>
              </div>
            </div>
          </CardContent>
          <CardFooter>
            <Button onClick={handleLogin} fullWidth>
              Sign In
            </Button>
          </CardFooter>
        </Card>
        
        <p className="text-center text-sm text-slate-600">
          Don't have an account?{' '}
          <a href="#" className="font-medium text-blue-600 hover:text-blue-500">
            Sign up
          </a>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;