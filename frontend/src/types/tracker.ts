export type TrackerStatus = 'PENDING' | 'COMPLETED' | 'MISSED';

export type TrackerDayColumn = {
  date: string;
  dayOfMonth: number;
  dayName: string;
  weekend: boolean;
};

export type TrackerCell = {
  taskId: string;
  timeSlotId: string;
  date: string;
  status: TrackerStatus;
  taskLogId: string | null;
  version: number | null;
  loggedAt: string | null;
};

export type TrackerRow = {
  mappingId: string;
  taskId: string;
  taskTitle: string;
  categoryId: string;
  categoryName: string;
  timeSlotId: string;
  startTime: string;
  endTime: string;
  displayOrder: number;
  cells: TrackerCell[];
};

export type TrackerGrid = {
  customerId: string;
  year: number;
  month: number;
  days: TrackerDayColumn[];
  rows: TrackerRow[];
};

export type TrackerCellUpdateResponse = {
  taskId: string;
  timeSlotId: string | null;
  date: string;
  status: TrackerStatus;
  taskLogId: string | null;
  version: number | null;
  loggedAt: string | null;
};
