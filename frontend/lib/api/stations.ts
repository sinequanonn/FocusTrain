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

export async function getStations(): Promise<StationsResponse> {
  return apiClient<StationsResponse>('/api/stations');
}
