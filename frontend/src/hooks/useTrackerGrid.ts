import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { TrackerCell, TrackerGrid, TrackerStatus } from '../types/tracker';
import { cycleTrackerCellStatus, fetchTrackerGrid } from '../api/tracker';

export function useTrackerGrid(customerId: string, year: number, month: number) {
  const queryClient = useQueryClient();

  const gridQuery = useQuery({
    queryKey: ['tracker-grid', customerId, year, month],
    queryFn: () => fetchTrackerGrid(customerId, year, month),
    placeholderData: keepPreviousData
  });

  const cycleMutation = useMutation({
    mutationFn: (payload: { taskId: string; date: string }) =>
      cycleTrackerCellStatus(customerId, payload.taskId, payload.date),
    onMutate: async (payload) => {
      await queryClient.cancelQueries({ queryKey: ['tracker-grid', customerId, year, month] });
      const previousGrid = queryClient.getQueryData<TrackerGrid>(['tracker-grid', customerId, year, month]);
      if (!previousGrid) {
        return { previousGrid };
      }

      const nextGrid = applyOptimisticCycle(previousGrid, payload.taskId, payload.date);
      queryClient.setQueryData(['tracker-grid', customerId, year, month], nextGrid);
      return { previousGrid };
    },
    onError: (_error, _payload, context) => {
      if (context?.previousGrid) {
        queryClient.setQueryData(['tracker-grid', customerId, year, month], context.previousGrid);
      }
    },
    onSuccess: (updatedCell) => {
      queryClient.setQueryData<TrackerGrid>(['tracker-grid', customerId, year, month], (current) => {
        if (!current) {
          return current;
        }
        return syncUpdatedCell(current, updatedCell.taskId, updatedCell.date, updatedCell.status, updatedCell.taskLogId, updatedCell.version, updatedCell.loggedAt);
      });
    }
  });

  return {
    gridQuery,
    cycleCell: cycleMutation.mutateAsync,
    isUpdating: cycleMutation.isPending
  };
}

function applyOptimisticCycle(grid: TrackerGrid, taskId: string, date: string): TrackerGrid {
  return {
    ...grid,
    rows: grid.rows.map((row) => ({
      ...row,
      cells: row.cells.map((cell) =>
        cell.taskId === taskId && cell.date === date
          ? updateCellStatus(cell, nextStatus(cell.status), null, null, null)
          : cell
      )
    }))
  };
}

function syncUpdatedCell(
  grid: TrackerGrid,
  taskId: string,
  date: string,
  status: TrackerStatus,
  taskLogId: string | null,
  version: number | null,
  loggedAt: string | null
): TrackerGrid {
  return {
    ...grid,
    rows: grid.rows.map((row) => ({
      ...row,
      cells: row.cells.map((cell) =>
        cell.taskId === taskId && cell.date === date
          ? updateCellStatus(cell, status, taskLogId, version, loggedAt)
          : cell
      )
    }))
  };
}

function updateCellStatus(
  cell: TrackerCell,
  status: TrackerStatus,
  taskLogId: string | null,
  version: number | null,
  loggedAt: string | null
): TrackerCell {
  return {
    ...cell,
    status,
    taskLogId,
    version,
    loggedAt
  };
}

function nextStatus(status: TrackerStatus): TrackerStatus {
  if (status === 'PENDING') return 'COMPLETED';
  if (status === 'COMPLETED') return 'MISSED';
  return 'PENDING';
}
