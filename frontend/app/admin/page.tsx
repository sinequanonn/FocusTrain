'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { apiClient, ApiError } from '@/lib/api/client';

type PingStatus =
  | { kind: 'loading' }
  | { kind: 'ok' }
  | { kind: 'error'; message: string };

export default function AdminHomePage() {
  const [status, setStatus] = useState<PingStatus>({ kind: 'loading' });

  useEffect(() => {
    (async () => {
      try {
        const res = await apiClient<{ ok: boolean }>('/api/admin/ping');
        setStatus(res.ok ? { kind: 'ok' } : { kind: 'error', message: 'ok=false' });
      } catch (e) {
        if (e instanceof ApiError) {
          setStatus({ kind: 'error', message: `[${e.status}] ${e.errorCode}: ${e.message}` });
        } else if (e instanceof Error) {
          setStatus({ kind: 'error', message: e.message });
        } else {
          setStatus({ kind: 'error', message: 'unknown error' });
        }
      }
    })();
  }, []);

  return (
    <main className="mx-auto max-w-2xl p-6 md:p-10">
      <Link
        href="/"
        className="mb-2 inline-block text-xs text-gray-400 hover:text-[#2AC1BC] dark:text-gray-500"
      >
        ← 메인으로
      </Link>
      <h1 className="mb-6 text-2xl md:text-3xl font-bold tracking-tight">
        <span className="text-[#2AC1BC]">관리자</span>{' '}
        <span className="text-gray-800 dark:text-gray-100">홈</span>
      </h1>

      <section className="mb-6 rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        <h2 className="mb-3 text-sm font-bold uppercase text-gray-400 dark:text-gray-500">
          가드 동작 확인
        </h2>
        {status.kind === 'loading' && (
          <p className="text-sm text-gray-400 dark:text-gray-500">확인 중...</p>
        )}
        {status.kind === 'ok' && (
          <p className="text-sm font-bold text-[#2AC1BC]">@AdminOnly 가드 통과 (200)</p>
        )}
        {status.kind === 'error' && (
          <p className="text-sm font-bold text-red-600 dark:text-red-400">{status.message}</p>
        )}
      </section>

      <section className="mb-6 rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        <h2 className="mb-3 text-sm font-bold uppercase text-gray-400 dark:text-gray-500">
          관리 메뉴
        </h2>
        <ul className="space-y-2 text-sm">
          <li>
            <Link
              href="/admin/stations"
              className="flex items-center justify-between rounded-2xl border border-gray-100 px-4 py-3 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] dark:border-gray-700"
            >
              <span className="font-bold text-gray-800 dark:text-gray-100">역 관리</span>
              <span className="text-xs text-gray-400 dark:text-gray-500">/admin/stations</span>
            </Link>
          </li>
          <li>
            <Link
              href="/admin/routes"
              className="flex items-center justify-between rounded-2xl border border-gray-100 px-4 py-3 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] dark:border-gray-700"
            >
              <span className="font-bold text-gray-800 dark:text-gray-100">노선 관리</span>
              <span className="text-xs text-gray-400 dark:text-gray-500">/admin/routes</span>
            </Link>
          </li>
          <li>
            <Link
              href="/admin/sessions"
              className="flex items-center justify-between rounded-2xl border border-gray-100 px-4 py-3 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] dark:border-gray-700"
            >
              <span className="font-bold text-gray-800 dark:text-gray-100">진행 중 세션</span>
              <span className="text-xs text-gray-400 dark:text-gray-500">/admin/sessions</span>
            </Link>
          </li>
        </ul>
      </section>

      <section className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        <h2 className="mb-3 text-sm font-bold uppercase text-gray-400 dark:text-gray-500">
          Epic C 진행 예정
        </h2>
        <ul className="space-y-2 text-sm text-gray-700 dark:text-gray-300">
          <li>· #25 역 등록/수정 (완료)</li>
          <li>· #26 노선 등록/수정/삭제 (완료)</li>
          <li>· #27 진행 중 세션 조회 (진행 중)</li>
          <li>· #28 관리자 대시보드 통계</li>
        </ul>
      </section>
    </main>
  );
}
