import { Box, CircularProgress, Typography } from '@mui/material';
import { alpha, useTheme } from '@mui/material/styles';
import type { TrackerCell, TrackerStatus } from '../../types/tracker';

type TrackerCellProps = {
  cell: TrackerCell;
  isCurrentDate: boolean;
  isCurrentHourRow: boolean;
  isUpdating: boolean;
  onCycle: (taskId: string, date: string) => void;
};

export function TrackerCellView({ cell, isCurrentDate, isCurrentHourRow, isUpdating, onCycle }: TrackerCellProps) {
  const theme = useTheme();
  const palette = getStatusPalette(theme.palette.mode, cell.status);

  return (
    <Box
      component="button"
      type="button"
      onClick={() => onCycle(cell.taskId, cell.date)}
      title={`${cell.status} on ${cell.date}`}
      disabled={isUpdating}
      sx={{
        all: 'unset',
        boxSizing: 'border-box',
        cursor: isUpdating ? 'not-allowed' : 'pointer',
        minHeight: 54,
        width: '100%',
        px: 1,
        py: 1,
        borderRadius: 1.5,
        border: `1px solid ${alpha(palette.border, 0.9)}`,
        bgcolor: alpha(palette.background, isCurrentDate ? 1 : 0.92),
        color: palette.text,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        boxShadow: isCurrentHourRow ? `inset 0 0 0 2px ${theme.palette.info.main}` : 'none',
        transition: 'transform 120ms ease, box-shadow 120ms ease, background-color 120ms ease',
        '&:hover': {
          transform: isUpdating ? 'none' : 'translateY(-1px)',
          boxShadow: `${isCurrentHourRow ? `inset 0 0 0 2px ${theme.palette.info.main}, ` : ''}0 6px 20px ${alpha(palette.border, 0.18)}`
        },
        '&:focus-visible': {
          outline: `2px solid ${theme.palette.primary.main}`,
          outlineOffset: 2
        },
        '&:disabled': {
          opacity: 0.7
        }
      }}
    >
      <Typography variant="body2" fontWeight={700} letterSpacing={0.2}>
        {cell.status[0]}
      </Typography>
      {isUpdating && (
        <CircularProgress
          size={16}
          sx={{ position: 'absolute', right: 6, bottom: 6, color: palette.text }}
        />
      )}
    </Box>
  );
}

function getStatusPalette(mode: 'light' | 'dark', status: TrackerStatus) {
  if (status === 'COMPLETED') {
    return {
      background: mode === 'light' ? '#dcfce7' : '#14532d',
      border: mode === 'light' ? '#22c55e' : '#4ade80',
      text: mode === 'light' ? '#166534' : '#dcfce7'
    };
  }

  if (status === 'MISSED') {
    return {
      background: mode === 'light' ? '#fee2e2' : '#7f1d1d',
      border: mode === 'light' ? '#ef4444' : '#fca5a5',
      text: mode === 'light' ? '#991b1b' : '#fee2e2'
    };
  }

  return {
    background: mode === 'light' ? '#ffffff' : '#0f172a',
    border: mode === 'light' ? '#cbd5e1' : '#334155',
    text: mode === 'light' ? '#0f172a' : '#e2e8f0'
  };
}
