import dayjs from 'dayjs';
import { apiClient } from './client';
import type { TrackerCellUpdateResponse, TrackerGrid } from '../types/tracker';

export const DEFAULT_TRACKER_CUSTOMER_ID =
  import.meta.env.VITE_TRACKER_CUSTOMER_ID ?? '00000000-0000-0000-0000-000000000002';

export async function fetchTrackerGrid(customerId: string, year: number, month: number): Promise<TrackerGrid> {
  const response = await apiClient.get<TrackerGrid>(`/customers/${customerId}/tracker/grid`, {
    params: { year, month }
  });
  return response.data;
}

export async function cycleTrackerCellStatus(
  customerId: string,
  taskId: string,
  date: string
): Promise<TrackerCellUpdateResponse> {
  const response = await apiClient.patch<TrackerCellUpdateResponse>(
    `/customers/${customerId}/tracker/tasks/${taskId}/dates/${dayjs(date).format('YYYY-MM-DD')}/status`
  );
  return response.data;
}
