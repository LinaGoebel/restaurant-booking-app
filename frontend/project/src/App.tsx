import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ReservationProvider } from './context/ReservationContext';
import Layout from './components/layout/Layout';
import Home from './pages/Home';
import BookingPage from './pages/BookingPage';
import ConfirmationPage from './pages/ConfirmationPage';
import ReservationsPage from './pages/ReservationsPage';
import AdminPage from './pages/AdminPage';
import LoginPage from './pages/LoginPage';
import ErrorPage from './pages/ErrorPage';

// Protected route component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ReservationProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            
            <Route path="/" element={<Layout><Home /></Layout>} />
            
            <Route path="/book" element={<Layout><BookingPage /></Layout>} />
            
            <Route 
              path="/confirmation" 
              element={
                <Layout><ConfirmationPage /></Layout>
              } 
            />
            
            <Route 
              path="/reservations" 
              element={
                <ProtectedRoute>
                  <Layout><ReservationsPage /></Layout>
                </ProtectedRoute>
              } 
            />
            
            <Route 
              path="/admin" 
              element={
                <ProtectedRoute>
                  <Layout><AdminPage /></Layout>
                </ProtectedRoute>
              } 
            />
            
            <Route path="*" element={<ErrorPage />} />
          </Routes>
        </ReservationProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;