import { apiClient } from './client';

export interface DurationResponse {
  departureStationId: number;
  arrivalStationId: number;
  durationMinutes: number;
}

export interface RouteResponse {
  id: number;
  departureStationId: number;
  departureStationName: string;
  arrivalStationId: number;
  arrivalStationName: string;
  durationMinutes: number;
}

export interface RoutesResponse {
  routes: RouteResponse[];
}

export interface RoutesPageResponse {
  routes: RouteResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface RouteRequest {
  departureStationId: number;
  arrivalStationId: number;
  durationMinutes: number;
  bidirectional?: boolean;
}

export interface RouteDurationRequest {
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

export async function getAdminRoutes(args: {
  q?: string;
  page?: number;
  size?: number;
}): Promise<RoutesPageResponse> {
  const params = new URLSearchParams();
  if (args.q && args.q.trim() !== '') params.set('q', args.q.trim());
  if (args.page !== undefined) params.set('page', String(args.page));
  if (args.size !== undefined) params.set('size', String(args.size));
  const qs = params.toString();
  return apiClient<RoutesPageResponse>(
    `/api/admin/routes${qs ? `?${qs}` : ''}`
  );
}

export async function createRoutes(request: RouteRequest): Promise<RoutesResponse> {
  return apiClient<RoutesResponse>('/api/admin/routes', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function updateRouteDuration(
  routeId: number,
  durationMinutes: number
): Promise<RouteResponse> {
  return apiClient<RouteResponse>(`/api/admin/routes/${routeId}`, {
    method: 'PATCH',
    body: JSON.stringify({ durationMinutes }),
  });
}

export async function deleteRoute(routeId: number): Promise<void> {
  await apiClient<void>(`/api/admin/routes/${routeId}`, {
    method: 'DELETE',
  });
}
