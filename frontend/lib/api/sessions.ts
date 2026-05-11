import { apiClient } from './client';
import { Station } from './stations';

export type FocusSessionStatus = 'RUNNING' | 'PAUSED' | 'COMPLETED' | 'ABORTED';

export interface FocusSessionCreatedRequest {
  departureStationId: number;
  arrivalStationId: number;
  delayMinutes?: number;
}

export interface FocusSessionCreatedResponse {
  sessionId: number;
  status: FocusSessionStatus;
  departure: Station;
  arrival: Station;
  baseDurationMinutes: number;
  delayMinutes: number;
  totalTargetMinutes: number;
  startedAt: string;
  plannedEndAt: string;
}

export interface FocusSessionProgressResponse {
  sessionId: number;
  status: FocusSessionStatus;
  accumulatedSeconds: number;
  remainingSeconds: number;
}

export interface FocusSessionEndedResponse {
  sessionId: number;
  status: FocusSessionStatus;
  totalFocusSeconds: number;
  startedAt: string;
  endedAt: string;
}

export interface FocusSessionDetailResponse {
  sessionId: number;
  status: FocusSessionStatus;
  departure: Station;
  arrival: Station;
  totalTargetSeconds: number;
  accumulatedSeconds: number;
  remainingSeconds: number;
  startedAt: string;
  plannedEndAt: string;
  endedAt: string | null;
}

export interface ActiveFocusSessionResponse {
  hasActiveSession: boolean;
  session: FocusSessionDetailResponse | null;
}

export interface FocusSessionHistoryItem {
  sessionId: number;
  status: FocusSessionStatus;
  departure: Station;
  arrival: Station;
  totalFocusSeconds: number;
  startedAt: string;
  endedAt: string;
}

export interface FocusSessionHistoryPageResponse {
  content: FocusSessionHistoryItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export async function createSession(
  req: FocusSessionCreatedRequest
): Promise<FocusSessionCreatedResponse> {
  return apiClient('/api/sessions', {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export async function pauseSession(sessionId: number) {
  return apiClient<FocusSessionProgressResponse>(
    `/api/sessions/${sessionId}/alight`,
    { method: 'POST' }
  );
}

export async function resumeSession(sessionId: number) {
  return apiClient<FocusSessionProgressResponse>(
    `/api/sessions/${sessionId}/reboard`,
    { method: 'POST' }
  );
}

export async function completeSession(sessionId: number) {
  return apiClient<FocusSessionEndedResponse>(
    `/api/sessions/${sessionId}/complete`,
    { method: 'POST' }
  );
}

export async function abortSession(sessionId: number) {
  return apiClient<FocusSessionEndedResponse>(
    `/api/sessions/${sessionId}/abort`,
    { method: 'POST' }
  );
}

export async function getActiveSession(): Promise<ActiveFocusSessionResponse> {
  return apiClient<ActiveFocusSessionResponse>('/api/sessions/active');
}

export async function getSessionDetail(
  sessionId: number
): Promise<FocusSessionDetailResponse> {
  return apiClient<FocusSessionDetailResponse>(`/api/sessions/${sessionId}`);
}

export async function getSessionHistory(
  page = 0,
  size = 10,
  status?: 'COMPLETED' | 'ABORTED'
): Promise<FocusSessionHistoryPageResponse> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  if (status) params.set('status', status);
  return apiClient<FocusSessionHistoryPageResponse>(
    `/api/sessions/history?${params}`
  );
}

export interface LegResponse {
  legNumber: number;
  startedAt: string;
  endedAt: string;
  durationSeconds: number;
}

export interface FocusSessionHistoryDetailResponse {
  session: {
    sessionId: number;
    status: FocusSessionStatus;
    departure: Station;
    arrival: Station;
    totalTargetSeconds: number;
    totalFocusSeconds: number;
    startedAt: string;
    plannedEndAt: string;
    endedAt: string;
  };
  legs: LegResponse[];
}

export async function getSessionHistoryDetail(
  sessionId: number
): Promise<FocusSessionHistoryDetailResponse> {
  return apiClient<FocusSessionHistoryDetailResponse>(
    `/api/sessions/${sessionId}/detail`
  );
}
