'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { ApiError } from '@/lib/api/client';
import { AdminSessionResponse, getAdminSessions } from '@/lib/api/sessions';

const PAGE_SIZE = 20;

export default function AdminSessionsPage() {
  const [sessions, setSessions] = useState<AdminSessionResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPage = useCallback(async (nextPage: number) => {
    setLoading(true);
    setError(null);
    try {
      const res = await getAdminSessions({ page: nextPage, size: PAGE_SIZE });
      setSessions(res.sessions);
      setPage(res.page);
      setTotalPages(res.totalPages);
      setTotalElements(res.totalElements);
    } catch (e) {
      setError(toErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPage(0);
  }, [fetchPage]);

  const goPrev = () => {
    if (page > 0) fetchPage(page - 1);
  };
  const goNext = () => {
    if (page < totalPages - 1) fetchPage(page + 1);
  };
  const refresh = () => fetchPage(page);

  return (
    <main className="mx-auto max-w-5xl p-6 md:p-10">
      <Link
        href="/admin"
        className="mb-2 inline-block text-xs text-gray-400 hover:text-[#2AC1BC] dark:text-gray-500"
      >
        ← 관리자 홈
      </Link>
      <h1 className="mb-6 text-2xl md:text-3xl font-bold tracking-tight">
        <span className="text-[#2AC1BC]">진행 중</span>{' '}
        <span className="text-gray-800 dark:text-gray-100">세션</span>
      </h1>

      <section className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-sm font-bold uppercase text-gray-400 dark:text-gray-500">
            RUNNING / PAUSED {totalElements > 0 && `(총 ${totalElements})`}
          </h2>
          <button
            type="button"
            onClick={refresh}
            disabled={loading}
            className="rounded-xl border border-gray-200 px-3 py-1 text-xs font-bold text-gray-600 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] disabled:opacity-50 dark:border-gray-600 dark:text-gray-300"
          >
            {loading ? '새로고침 중...' : '새로고침'}
          </button>
        </div>

        {error && (
          <p className="text-sm font-bold text-red-600 dark:text-red-400">{error}</p>
        )}

        {!error && loading && sessions.length === 0 && (
          <p className="text-sm text-gray-400 dark:text-gray-500">불러오는 중...</p>
        )}

        {!error && !loading && sessions.length === 0 && (
          <p className="text-sm text-gray-400 dark:text-gray-500">
            진행 중인 세션이 없습니다.
          </p>
        )}

        {!error && sessions.length > 0 && (
          <ul className="divide-y divide-gray-100 dark:divide-gray-700">
            {sessions.map((session) => (
              <li key={session.id} className="py-3">
                <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
                  <StatusBadge status={session.status} />
                  <p className="text-sm font-bold text-gray-800 dark:text-gray-100">
                    {session.userNickname}
                  </p>
                  <p className="text-xs text-gray-400 dark:text-gray-500">
                    {session.userEmail}
                  </p>
                </div>
                <div className="mt-1 grid grid-cols-1 gap-1 text-xs text-gray-500 dark:text-gray-400 md:grid-cols-3">
                  <span>
                    <span className="text-gray-400 dark:text-gray-500">노선:</span>{' '}
                    {session.departureStationName}{' '}
                    <span className="text-gray-300">→</span>{' '}
                    {session.arrivalStationName}
                  </span>
                  <span>
                    <span className="text-gray-400 dark:text-gray-500">시작:</span>{' '}
                    {formatDateTime(session.startedAt)}{' '}
                    <span className="text-gray-300">
                      ({elapsedFromNow(session.startedAt)})
                    </span>
                  </span>
                  <span>
                    <span className="text-gray-400 dark:text-gray-500">목표:</span>{' '}
                    {session.totalTargetMinutes}분 · 예정 종료{' '}
                    {formatTime(session.plannedEndAt)}
                  </span>
                </div>
              </li>
            ))}
          </ul>
        )}

        {totalPages > 1 && (
          <div className="mt-4 flex items-center justify-between text-sm">
            <button
              type="button"
              onClick={goPrev}
              disabled={page === 0 || loading}
              className="rounded-xl border border-gray-200 px-3 py-1 font-bold text-gray-600 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] disabled:opacity-30 dark:border-gray-600 dark:text-gray-300"
            >
              ← 이전
            </button>
            <span className="text-xs text-gray-400 dark:text-gray-500">
              {page + 1} / {totalPages}
            </span>
            <button
              type="button"
              onClick={goNext}
              disabled={page >= totalPages - 1 || loading}
              className="rounded-xl border border-gray-200 px-3 py-1 font-bold text-gray-600 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] disabled:opacity-30 dark:border-gray-600 dark:text-gray-300"
            >
              다음 →
            </button>
          </div>
        )}
      </section>
    </main>
  );
}

function StatusBadge({ status }: { status: 'RUNNING' | 'PAUSED' }) {
  const cls =
    status === 'RUNNING'
      ? 'bg-[#2AC1BC]/10 text-[#2AC1BC] border-[#2AC1BC]/30'
      : 'bg-amber-50 text-amber-600 border-amber-200 dark:bg-amber-900/30 dark:text-amber-300 dark:border-amber-800';
  const label = status === 'RUNNING' ? '집중 중' : '하차 중';
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[10px] font-bold uppercase ${cls}`}
    >
      {label}
    </span>
  );
}

function formatDateTime(iso: string): string {
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const mi = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
}

function formatTime(iso: string): string {
  const d = new Date(iso);
  const hh = String(d.getHours()).padStart(2, '0');
  const mi = String(d.getMinutes()).padStart(2, '0');
  return `${hh}:${mi}`;
}

function elapsedFromNow(iso: string): string {
  const start = new Date(iso).getTime();
  const now = Date.now();
  const diffSec = Math.max(0, Math.floor((now - start) / 1000));
  const hours = Math.floor(diffSec / 3600);
  const minutes = Math.floor((diffSec % 3600) / 60);
  if (hours > 0) return `${hours}시간 ${minutes}분 경과`;
  return `${minutes}분 경과`;
}

function toErrorMessage(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.errorCode === 'AUTH_FORBIDDEN_ADMIN_ONLY') return '관리자 권한이 필요합니다.';
    return `[${e.status}] ${e.errorCode}: ${e.message}`;
  }
  if (e instanceof Error) return e.message;
  return '알 수 없는 오류가 발생했습니다.';
}
