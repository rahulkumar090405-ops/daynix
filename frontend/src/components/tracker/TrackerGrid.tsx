import { Box, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import { alpha, useTheme } from '@mui/material/styles';
import dayjs from 'dayjs';
import type { TrackerGrid, TrackerRow } from '../../types/tracker';
import { TrackerCellView } from './TrackerCell';

type TrackerGridProps = {
  grid: TrackerGrid | undefined;
  isLoading: boolean;
  isUpdating: boolean;
  filteredRows: TrackerRow[];
  selectedDate: string;
  onCycle: (taskId: string, date: string) => void;
};

export function TrackerGridTable({ grid, isLoading, isUpdating, filteredRows, selectedDate, onCycle }: TrackerGridProps) {
  const hasGrid = Boolean(grid);
  const theme = useTheme();

  if (isLoading || !grid) {
    return <GridSkeleton />;
  }

  return (
    <TableContainer
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 3,
        overflowX: 'auto',
        bgcolor: 'background.paper'
      }}
    >
      <Table stickyHeader sx={{ minWidth: 1200, tableLayout: 'fixed' }}>
        <TableHead>
          <TableRow>
            <TableCell
              sx={{
                position: 'sticky',
                left: 0,
                zIndex: 4,
                minWidth: 260,
                bgcolor: 'background.paper',
                borderRight: '1px solid',
                borderColor: 'divider'
              }}
            >
              <Typography variant="subtitle2" fontWeight={700}>
                Task / Time Slot
              </Typography>
            </TableCell>
            {grid.days.map((day) => {
              const isSelected = day.date === selectedDate;
              return (
                <TableCell
                  key={day.date}
                  align="center"
                  sx={{
                    minWidth: 78,
                    bgcolor: isSelected ? alpha(theme.palette.primary.main, 0.08) : 'background.paper',
                    color: isSelected ? 'primary.main' : 'text.secondary',
                    fontWeight: 700,
                    borderBottomColor: isSelected ? 'primary.main' : 'divider'
                  }}
                >
                  <Typography variant="caption" display="block" sx={{ opacity: 0.8 }}>
                    {day.dayName}
                  </Typography>
                  <Typography variant="body2" fontWeight={800}>
                    {day.dayOfMonth}
                  </Typography>
                </TableCell>
              );
            })}
          </TableRow>
        </TableHead>
        <TableBody>
          {filteredRows.map((row) => {
            const currentHourRow = isCurrentHourRow(row.startTime, row.endTime);
            return (
              <TableRow key={row.mappingId} hover sx={{ '&:hover td': { bgcolor: 'action.hover' } }}>
                <TableCell
                  sx={{
                    position: 'sticky',
                    left: 0,
                    zIndex: 3,
                    minWidth: 260,
                    bgcolor: currentHourRow ? 'rgba(37, 99, 235, 0.06)' : 'background.paper',
                    borderRight: '1px solid',
                    borderColor: 'divider'
                  }}
                >
                  <Typography variant="subtitle2" fontWeight={800} noWrap>
                    {row.taskTitle}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" noWrap>
                    {row.categoryName} · {formatTimeRange(row.startTime, row.endTime)}
                  </Typography>
                </TableCell>
                {grid.days.map((day) => {
                  const cell = row.cells.find((currentCell) => currentCell.date === day.date);
                  if (!cell) {
                    return (
                      <TableCell key={`${row.mappingId}-${day.date}`} align="center">
                        <Box sx={{ height: 54, borderRadius: 1.5, border: '1px dashed', borderColor: 'divider' }} />
                      </TableCell>
                    );
                  }
                  return (
                    <TableCell key={`${row.mappingId}-${day.date}`} align="center" sx={{ p: 1 }}>
                      <TrackerCellView
                        cell={cell}
                        isCurrentDate={day.date === selectedDate}
                        isCurrentHourRow={currentHourRow}
                        isUpdating={isUpdating}
                        onCycle={onCycle}
                      />
                    </TableCell>
                  );
                })}
              </TableRow>
            );
          })}
          {hasGrid && filteredRows.length === 0 && (
            <TableRow>
              <TableCell colSpan={grid.days.length + 1}>
                <Box sx={{ py: 8, textAlign: 'center' }}>
                  <Typography variant="h6" fontWeight={700} gutterBottom>
                    No rows match your filters.
                  </Typography>
                  <Typography color="text.secondary">
                    Clear search or category filters to bring back the tracker rows.
                  </Typography>
                </Box>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

function GridSkeleton() {
  return (
    <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3, bgcolor: 'background.paper' }}>
      <Table stickyHeader sx={{ minWidth: 1200, tableLayout: 'fixed' }}>
        <TableHead>
          <TableRow>
            <TableCell sx={{ minWidth: 260 }}>
              <Skeleton width={140} />
            </TableCell>
            {Array.from({ length: 10 }).map((_, index) => (
              <TableCell key={index} align="center">
                <Skeleton width={32} sx={{ mx: 'auto' }} />
                <Skeleton width={20} sx={{ mx: 'auto' }} />
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {Array.from({ length: 6 }).map((_, rowIndex) => (
            <TableRow key={rowIndex}>
              <TableCell>
                <Skeleton width="70%" />
                <Skeleton width="45%" />
              </TableCell>
              {Array.from({ length: 10 }).map((__, columnIndex) => (
                <TableCell key={columnIndex} align="center">
                  <Skeleton variant="rounded" height={54} />
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

function isCurrentHourRow(startTime: string, endTime: string) {
  const now = dayjs();
  const today = now.format('YYYY-MM-DD');
  const start = dayjs(`${today}T${startTime}`);
  const end = dayjs(`${today}T${endTime}`);
  return now.isAfter(start) && now.isBefore(end);
}

function formatTimeRange(startTime: string, endTime: string) {
  return `${dayjs(`2000-01-01T${startTime}`).format('hh:mm A')} - ${dayjs(`2000-01-01T${endTime}`).format('hh:mm A')}`;
}
