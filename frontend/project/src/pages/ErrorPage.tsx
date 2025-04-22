import React from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/ui/Button';

const ErrorPage: React.FC = () => {
  const navigate = useNavigate();
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4 py-12 sm:px-6 lg:px-8">
      <div className="text-center">
        <h1 className="text-9xl font-bold text-blue-900">404</h1>
        <h2 className="mt-4 text-3xl font-semibold text-slate-900">Page Not Found</h2>
        <p className="mt-4 text-lg text-slate-600 max-w-md mx-auto">
          We couldn't find the page you were looking for. Perhaps you took a wrong turn?
        </p>
        <div className="mt-8">
          <Button onClick={() => navigate('/')}>
            Return to Home
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ErrorPage;