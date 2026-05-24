import { apiClient } from './client';

export interface LoginResponse {
  userId: number;
  email: string;
  nickname: string;
}

export type Role = 'NORMAL' | 'ADMIN';

export interface MeResponse {
  userId: number;
  email: string;
  nickname: string;
  role: Role;
  departureStationId: number | null;
  departureStationName: string | null;
  createdAt: string;
}

export async function login(idToken: string): Promise<LoginResponse> {
  return apiClient<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ idToken }),
  });
}

export async function getMe(): Promise<MeResponse> {
  return apiClient<MeResponse>('/api/auth/me');
}

export async function updateNickname(nickname: string): Promise<MeResponse> {
  return apiClient<MeResponse>('/api/users/me', {
    method: 'PATCH',
    body: JSON.stringify({ nickname }),
  });
}

export async function updateDepartureStation(
  stationId: number
): Promise<MeResponse> {
  return apiClient<MeResponse>('/api/users/me/departure-station', {
    method: 'PUT',
    body: JSON.stringify({ stationId }),
  });
}
