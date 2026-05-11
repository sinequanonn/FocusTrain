import { apiClient } from './client';

export interface LoginResponse {
  userId: number;
  email: string;
  nickname: string;
}

export interface MeResponse {
  userId: number;
  email: string;
  nickname: string;
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
