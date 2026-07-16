import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
  Typography
} from '@mui/material';
import dayjs from 'dayjs';
import { useMemo, useState } from 'react';
import { fetchHealth } from '../api/health';
import { DEFAULT_TRACKER_CUSTOMER_ID } from '../api/tracker';
import { useTrackerGrid } from '../hooks/useTrackerGrid';
import { TrackerGridTable } from '../components/tracker/TrackerGrid';
import { useQuery } from '@tanstack/react-query';

export function DashboardPage() {
  const [selectedMonth, setSelectedMonth] = useState(dayjs().startOf('month'));
  const [search, setSearch] = useState('');
  const [categoryId, setCategoryId] = useState('all');

  const healthQuery = useQuery({
    queryKey: ['health'],
    queryFn: fetchHealth
  });

  const year = selectedMonth.year();
  const month = selectedMonth.month() + 1;
  const customerId = DEFAULT_TRACKER_CUSTOMER_ID;
  const tracker = useTrackerGrid(customerId, year, month);

  const categories = useMemo(() => {
    const seen = new Map<string, string>();
    tracker.gridQuery.data?.rows.forEach((row) => {
      seen.set(row.categoryId, row.categoryName);
    });
    return Array.from(seen.entries()).map(([id, name]) => ({ id, name }));
  }, [tracker.gridQuery.data]);

  const filteredRows = useMemo(() => {
    const rows = tracker.gridQuery.data?.rows ?? [];
    return rows.filter((row) => {
      const matchesSearch =
        search.trim().length === 0 ||
        row.taskTitle.toLowerCase().includes(search.trim().toLowerCase()) ||
        row.categoryName.toLowerCase().includes(search.trim().toLowerCase());
      const matchesCategory = categoryId === 'all' || row.categoryId === categoryId;
      return matchesSearch && matchesCategory;
    });
  }, [categoryId, search, tracker.gridQuery.data?.rows]);

  const currentDate = dayjs();
  const selectedMonthLabel = selectedMonth.format('MMMM YYYY');

  return (
    <Stack spacing={3}>
      <Paper
        sx={{
          p: { xs: 2, md: 3 },
          background: 'linear-gradient(135deg, rgba(37,99,235,0.12), rgba(22,163,74,0.08))',
          border: '1px solid',
          borderColor: 'divider'
        }}
        variant="outlined"
      >
        <Stack spacing={2}>
          <Box display="flex" justifyContent="space-between" alignItems={{ xs: 'flex-start', md: 'center' }} gap={2} flexWrap="wrap">
            <Box>
              <Typography variant="overline" letterSpacing={2} color="primary.main" fontWeight={800}>
                Daynix Tracker
              </Typography>
              <Typography component="h2" variant="h4" fontWeight={800}>
                Excel-style monthly activity grid
              </Typography>
              <Typography color="text.secondary" sx={{ maxWidth: 780 }}>
                Click any cell to cycle Pending, Completed, and Missed. The grid auto-saves on every click and stays responsive on smaller screens.
              </Typography>
            </Box>
            <Stack direction="row" spacing={1} flexWrap="wrap">
              {healthQuery.data ? (
                <Chip color="success" label={`${healthQuery.data.service} ${healthQuery.data.status}`} />
              ) : (
                <Chip variant="outlined" label="Backend connected via API" />
              )}
              <Chip label={customerId.slice(0, 8)} variant="outlined" />
            </Stack>
          </Box>

          <Card variant="outlined" sx={{ bgcolor: 'rgba(255,255,255,0.72)' }}>
            <CardContent>
              <Stack spacing={2}>
                <Stack direction={{ xs: 'column', lg: 'row' }} spacing={2} alignItems={{ xs: 'stretch', lg: 'center' }} justifyContent="space-between">
                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} alignItems={{ xs: 'stretch', sm: 'center' }}>
                    <Button variant="outlined" onClick={() => setSelectedMonth((value) => value.subtract(1, 'month'))}>
                      Previous
                    </Button>
                    <Button variant="outlined" onClick={() => setSelectedMonth(dayjs())}>
                      Today
                    </Button>
                    <Button variant="outlined" onClick={() => setSelectedMonth((value) => value.add(1, 'month'))}>
                      Next
                    </Button>
                    <TextField
                      label="Jump to month"
                      type="month"
                      value={selectedMonth.format('YYYY-MM')}
                      onChange={(event) => setSelectedMonth(dayjs(`${event.target.value}-01`))}
                      sx={{ minWidth: 180 }}
                    />
                  </Stack>

                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} alignItems={{ xs: 'stretch', sm: 'center' }}>
                    <TextField
                      label="Search tasks"
                      value={search}
                      onChange={(event) => setSearch(event.target.value)}
                      placeholder="Wake Up, Workout..."
                      sx={{ minWidth: { xs: '100%', sm: 260 } }}
                    />
                    <FormControl sx={{ minWidth: 220 }}>
                      <InputLabel>Category</InputLabel>
                      <Select label="Category" value={categoryId} onChange={(event) => setCategoryId(event.target.value)}>
                        <MenuItem value="all">All categories</MenuItem>
                        {categories.map((category) => (
                          <MenuItem key={category.id} value={category.id}>
                            {category.name}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Stack>
                </Stack>

                <Box display="flex" flexWrap="wrap" gap={1.5} alignItems="center" justifyContent="space-between">
                  <Typography variant="h6" fontWeight={800}>
                    {selectedMonthLabel}
                  </Typography>
                  <Stack direction="row" spacing={1} flexWrap="wrap">
                    <Chip label={`Today: ${currentDate.format('MMM D')}`} variant="outlined" />
                    <Chip label={`Rows: ${filteredRows.length}`} variant="outlined" />
                    {tracker.gridQuery.isFetching && <Chip label="Refreshing" color="primary" />}
                  </Stack>
                </Box>
              </Stack>
            </CardContent>
          </Card>

          {tracker.gridQuery.isError && <Alert severity="error">Unable to load tracker data. Confirm the backend API is running and the customer id is valid.</Alert>}
        </Stack>
      </Paper>

      <Box sx={{ position: 'relative' }}>
        {tracker.gridQuery.isFetching && tracker.gridQuery.data && (
          <Box sx={{ position: 'absolute', top: 16, right: 16, zIndex: 2 }}>
            <CircularProgress size={22} />
          </Box>
        )}
        <TrackerGridTable
          grid={tracker.gridQuery.data}
          isLoading={tracker.gridQuery.isLoading}
          isUpdating={tracker.isUpdating}
          filteredRows={filteredRows}
          selectedDate={currentDate.format('YYYY-MM-DD')}
          onCycle={(taskId, date) => {
            void tracker.cycleCell({ taskId, date });
          }}
        />
      </Box>

      <Divider />
      <Typography variant="caption" color="text.secondary">
        The grid is fully data-driven from the backend month response. Search and category filtering are applied client-side on the rendered grid rows.
      </Typography>
    </Stack>
  );
}
