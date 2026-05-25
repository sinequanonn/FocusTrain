import { apiClient } from './client';

export interface Station {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
}

export interface StationsResponse {
  stations: Station[];
}

export interface StationRequest {
  name: string;
  latitude: number;
  longitude: number;
}

export async function getStations(): Promise<StationsResponse> {
  return apiClient<StationsResponse>('/api/stations');
}

export async function createStation(request: StationRequest): Promise<Station> {
  return apiClient<Station>('/api/admin/station', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function updateStation(
  stationId: number,
  request: StationRequest
): Promise<Station> {
  return apiClient<Station>(`/api/admin/station/${stationId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}
