import { apiClient } from './client';

export interface DurationResponse {
  departureStationId: number;
  arrivalStationId: number;
  durationMinutes: number;
}

export async function getDuration(
  departureStationId: number,
  arrivalStationId: number
): Promise<DurationResponse> {
  const params = new URLSearchParams({
    departureStationId: String(departureStationId),
    arrivalStationId: String(arrivalStationId),
  });
  return apiClient<DurationResponse>(`/api/routes/duration?${params}`);
}
