import React from 'react';
import { Table } from '../types';

interface TableSelectorProps {
  tables: Table[];
  selectedTable: string;
  onTableSelect: (tableId: string) => void;
}

const TableSelector: React.FC<TableSelectorProps> = ({
  tables,
  selectedTable,
  onTableSelect,
}) => {
  const locationColors = {
    window: 'bg-blue-100 border-blue-300',
    center: 'bg-amber-100 border-amber-300',
    bar: 'bg-green-100 border-green-300',
    outdoor: 'bg-teal-100 border-teal-300',
    private: 'bg-violet-100 border-violet-300',
  };
  
  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-2">
        {Object.entries(locationColors).map(([location, color]) => (
          <div key={location} className="flex items-center gap-1">
            <div className={`w-3 h-3 rounded-full ${color.split(' ')[0]}`}></div>
            <span className="text-xs text-slate-600 capitalize">{location}</span>
          </div>
        ))}
      </div>
      
      <div className="p-6 border border-slate-200 rounded-lg bg-slate-50 grid grid-cols-3 gap-4 md:grid-cols-4 lg:grid-cols-5">
        {tables.map((table) => {
          const locationColor = locationColors[table.location];
          
          return (
            <button
              key={table.id}
              onClick={() => table.isAvailable && onTableSelect(table.id)}
              disabled={!table.isAvailable}
              className={`
                relative aspect-square rounded-lg border-2 transition-all flex flex-col items-center justify-center
                ${selectedTable === table.id ? 'ring-2 ring-blue-900 ring-offset-2' : ''}
                ${!table.isAvailable ? 'bg-slate-100 border-slate-200 text-slate-400 cursor-not-allowed' : `${locationColor} hover:shadow-md`}
              `}
            >
              <span className="font-medium text-slate-800">{table.name}</span>
              <span className="text-xs text-slate-600">{table.capacity} seats</span>
              
              {!table.isAvailable && (
                <div className="absolute inset-0 bg-slate-200 bg-opacity-60 flex items-center justify-center rounded-md">
                  <span className="text-xs font-medium text-slate-600">Reserved</span>
                </div>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default TableSelector;