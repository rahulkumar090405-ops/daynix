/// <reference types="vite/client" />

interface ImportMetaEnv {
	readonly VITE_API_BASE_URL?: string;
	readonly VITE_TRACKER_CUSTOMER_ID?: string;
	readonly VITE_ACCESS_TOKEN_STORAGE_KEY?: string;
}
