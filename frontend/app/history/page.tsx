'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks/useAuth';
import {
  getSessionHistory,
  FocusSessionHistoryItem,
  FocusSessionHistoryPageResponse,
} from '@/lib/api/sessions';
import { ApiError } from '@/lib/api/client';

type StatusFilter = 'ALL' | 'COMPLETED' | 'ABORTED';

const PAGE_SIZE = 10;

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatDuration(seconds: number) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  if (h > 0) return `${h}시간 ${m}분`;
  return `${m}분`;
}

export default function HistoryListPage() {
  const router = useRouter();
  const { user, loading } = useAuth();

  const [page, setPage] = useState(0);
  const [status, setStatus] = useState<StatusFilter>('ALL');
  const [data, setData] = useState<FocusSessionHistoryPageResponse | null>(
    null
  );
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!loading && !user) router.replace('/login');
  }, [user, loading, router]);

  useEffect(() => {
    if (!user) return;
    (async () => {
      try {
        const res = await getSessionHistory(
          page,
          PAGE_SIZE,
          status === 'ALL' ? undefined : status
        );
        setData(res);
      } catch (e) {
        if (e instanceof ApiError) {
          setError(`[${e.status}] ${e.errorCode}: ${e.message}`);
        } else if (e instanceof Error) {
          setError(e.message);
        }
      }
    })();
  }, [user, page, status]);

  if (loading || !user) {
    return <main className="p-8 text-gray-500 dark:text-gray-400">로딩 중...</main>;
  }

  return (
    <main className="mx-auto max-w-4xl p-6 md:p-10">
      <header className="mb-6 flex items-center justify-between">
        <div>
          <Link
            href="/"
            className="mb-2 inline-block text-xs text-gray-400 hover:text-[#2AC1BC] dark:text-gray-500"
          >
            ← 메인으로
          </Link>
          <h1 className="text-2xl md:text-3xl font-bold tracking-tight">
            <span className="text-[#2AC1BC]">이동</span>{' '}
            <span className="text-gray-800 dark:text-gray-100">기록</span>
          </h1>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            지금까지 완주한 모든 몰입 여정
          </p>
        </div>
      </header>

      {error && (
        <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
          {error}
        </div>
      )}

      {/* 필터 */}
      <div className="mb-4 flex gap-2">
        {(['ALL', 'COMPLETED', 'ABORTED'] as const).map((s) => (
          <button
            key={s}
            onClick={() => {
              setStatus(s);
              setPage(0);
            }}
            className={`rounded-full px-4 py-1.5 text-xs font-bold transition ${
              status === s
                ? 'bg-[#2AC1BC] text-white'
                : 'border border-gray-200 bg-white text-gray-600 hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700'
            }`}
          >
            {s === 'ALL' ? '전체' : s === 'COMPLETED' ? '완료' : '중단'}
          </button>
        ))}
      </div>

      {/* 목록 */}
      <section className="rounded-3xl border border-gray-100 bg-white p-2 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        {!data ? (
          <p className="p-6 text-sm text-gray-400 dark:text-gray-500">불러오는 중...</p>
        ) : data.content.length === 0 ? (
          <p className="p-10 text-center text-sm italic text-gray-400 dark:text-gray-500">
            기록이 없습니다.
          </p>
        ) : (
          <ul className="divide-y divide-gray-100 dark:divide-gray-700">
            {data.content.map((h) => (
              <HistoryRow key={h.sessionId} item={h} />
            ))}
          </ul>
        )}
      </section>

      {/* 페이지네이션 */}
      {data && data.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-2">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-bold disabled:opacity-30 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200"
          >
            이전
          </button>
          <span className="text-sm text-gray-500 dark:text-gray-400">
            {data.page + 1} / {data.totalPages}
          </span>
          <button
            onClick={() => setPage((p) => p + 1)}
            disabled={!data.hasNext}
            className="rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-bold disabled:opacity-30 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200"
          >
            다음
          </button>
        </div>
      )}
    </main>
  );
}

function HistoryRow({ item }: { item: FocusSessionHistoryItem }) {
  return (
    <li>
      <Link
        href={`/history/${item.sessionId}`}
        className="flex items-center justify-between p-4 transition hover:bg-gray-50 dark:hover:bg-gray-700"
      >
        <div className="flex items-center gap-3">
          <span
            className={`rounded-full px-2.5 py-0.5 text-[10px] font-bold ${
              item.status === 'COMPLETED'
                ? 'bg-[#EBFBFA] text-[#2AC1BC] dark:bg-[#14302f]'
                : 'bg-gray-100 text-gray-500 dark:bg-gray-700 dark:text-gray-400'
            }`}
          >
            {item.status === 'COMPLETED' ? '완료' : '중단'}
          </span>
          <div>
            <p className="text-sm font-bold text-gray-800 dark:text-gray-100">
              {item.departure.name} → {item.arrival.name}
            </p>
            <p className="text-[10px] text-gray-400 dark:text-gray-500">
              {formatDate(item.endedAt)}
            </p>
          </div>
        </div>
        <div className="text-right">
          <span className="text-base font-bold text-[#2AC1BC]">
            {formatDuration(item.totalFocusSeconds)}
          </span>
          <p className="text-[10px] text-gray-400 dark:text-gray-500">자세히 →</p>
        </div>
      </Link>
    </li>
  );
}
