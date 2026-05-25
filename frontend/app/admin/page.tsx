'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { ApiError } from '@/lib/api/client';
import { DashboardStatsResponse, getDashboardStats } from '@/lib/api/admin';

const REFRESH_INTERVAL_MS = 30_000;

export default function AdminHomePage() {
  const [stats, setStats] = useState<DashboardStatsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdatedAt, setLastUpdatedAt] = useState<Date | null>(null);

  const fetchStats = useCallback(async () => {
    try {
      const res = await getDashboardStats();
      setStats(res);
      setError(null);
      setLastUpdatedAt(new Date());
    } catch (e) {
      setError(toErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, REFRESH_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [fetchStats]);

  return (
    <main className="mx-auto max-w-4xl p-6 md:p-10">
      <Link
        href="/"
        className="mb-2 inline-block text-xs text-gray-400 hover:text-[#2AC1BC] dark:text-gray-500"
      >
        ← 메인으로
      </Link>
      <div className="mb-6 flex items-end justify-between gap-2">
        <h1 className="text-2xl md:text-3xl font-bold tracking-tight">
          <span className="text-[#2AC1BC]">관리자</span>{' '}
          <span className="text-gray-800 dark:text-gray-100">홈</span>
        </h1>
        <p className="text-xs text-gray-400 dark:text-gray-500">
          {lastUpdatedAt
            ? `${formatTime(lastUpdatedAt)} 기준 · 30초마다 자동 새로고침`
            : '불러오는 중...'}
        </p>
      </div>

      {error && (
        <div className="mb-6 rounded-3xl border border-red-200 bg-red-50 p-4 text-sm font-bold text-red-600 dark:border-red-800 dark:bg-red-900/30 dark:text-red-300">
          {error}
        </div>
      )}

      <section className="grid grid-cols-1 gap-3 sm:grid-cols-2 md:grid-cols-3">
        <InfoCard
          label="총 가입자"
          value={stats?.totalUsers}
          unit="명"
          loading={loading}
        />
        <LinkCard
          label="진행 중 세션"
          value={stats?.activeSessions}
          unit="개"
          loading={loading}
          href="/admin/sessions"
          accent
        />
        <InfoCard
          label="오늘 시작된 세션"
          value={stats?.todayStartedSessions}
          unit="개"
          loading={loading}
        />
        <InfoCard
          label="오늘 누적 집중 시간"
          value={stats?.todayFocusMinutes}
          unit="분"
          loading={loading}
        />
        <LinkCard
          label="등록된 역"
          value={stats?.totalStations}
          unit="개"
          loading={loading}
          href="/admin/stations"
        />
        <LinkCard
          label="등록된 노선"
          value={stats?.totalRoutes}
          unit="개"
          loading={loading}
          href="/admin/routes"
        />
      </section>
    </main>
  );
}

function InfoCard({
  label,
  value,
  unit,
  loading,
}: {
  label: string;
  value: number | undefined;
  unit: string;
  loading: boolean;
}) {
  return (
    <div className="rounded-3xl border border-gray-100 bg-white p-5 shadow-sm dark:border-gray-700 dark:bg-gray-800">
      <p className="mb-2 text-xs font-bold uppercase text-gray-400 dark:text-gray-500">
        {label}
      </p>
      <p className="text-2xl font-bold text-gray-800 dark:text-gray-100">
        {loading && value === undefined ? (
          <span className="text-gray-300 dark:text-gray-600">···</span>
        ) : (
          <>
            {formatNumber(value ?? 0)}
            <span className="ml-1 text-sm font-normal text-gray-400 dark:text-gray-500">
              {unit}
            </span>
          </>
        )}
      </p>
    </div>
  );
}

function LinkCard({
  label,
  value,
  unit,
  loading,
  href,
  accent,
}: {
  label: string;
  value: number | undefined;
  unit: string;
  loading: boolean;
  href: string;
  accent?: boolean;
}) {
  const valueColor = accent
    ? 'text-[#2AC1BC]'
    : 'text-gray-800 dark:text-gray-100';
  return (
    <Link
      href={href}
      className="group flex flex-col rounded-3xl border border-gray-100 bg-white p-5 shadow-sm transition hover:border-[#2AC1BC] dark:border-gray-700 dark:bg-gray-800"
    >
      <div className="mb-2 flex items-center justify-between">
        <p className="text-xs font-bold uppercase text-gray-400 dark:text-gray-500">
          {label}
        </p>
        <span className="text-xs text-gray-300 transition group-hover:text-[#2AC1BC] dark:text-gray-600">
          →
        </span>
      </div>
      <p className={`text-2xl font-bold ${valueColor}`}>
        {loading && value === undefined ? (
          <span className="text-gray-300 dark:text-gray-600">···</span>
        ) : (
          <>
            {formatNumber(value ?? 0)}
            <span className="ml-1 text-sm font-normal text-gray-400 dark:text-gray-500">
              {unit}
            </span>
          </>
        )}
      </p>
      <p className="mt-2 text-xs text-gray-400 dark:text-gray-500">{href}</p>
    </Link>
  );
}

function formatNumber(n: number): string {
  return n.toLocaleString('ko-KR');
}

function formatTime(d: Date): string {
  const hh = String(d.getHours()).padStart(2, '0');
  const mi = String(d.getMinutes()).padStart(2, '0');
  const ss = String(d.getSeconds()).padStart(2, '0');
  return `${hh}:${mi}:${ss}`;
}

function toErrorMessage(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.errorCode === 'AUTH_FORBIDDEN_ADMIN_ONLY') return '관리자 권한이 필요합니다.';
    return `[${e.status}] ${e.errorCode}: ${e.message}`;
  }
  if (e instanceof Error) return e.message;
  return '알 수 없는 오류가 발생했습니다.';
}
