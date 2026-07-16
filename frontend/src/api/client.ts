import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

apiClient.interceptors.request.use((config) => {
  const storageKey = import.meta.env.VITE_ACCESS_TOKEN_STORAGE_KEY ?? 'daynix.accessToken';
  const token = window.localStorage.getItem(storageKey);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
