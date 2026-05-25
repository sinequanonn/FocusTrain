import { apiClient } from './client';

export interface DashboardStatsResponse {
  totalUsers: number;
  activeSessions: number;
  todayStartedSessions: number;
  todayFocusMinutes: number;
  totalStations: number;
  totalRoutes: number;
}

export async function getDashboardStats(): Promise<DashboardStatsResponse> {
  return apiClient<DashboardStatsResponse>('/api/admin/dashboard/stats');
}
