'use client';

import { useState } from 'react';
import { Station } from '@/lib/api/stations';
import { KoreaMap } from '@/components/map/KoreaMap';

type TimerMode = 'remaining' | 'elapsed';

function formatTime(totalSeconds: number) {
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = totalSeconds % 60;
  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(
      2,
      '0'
    )}:${String(s).padStart(2, '0')}`;
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

interface Props {
  stations: Station[];
  departure: Station;
  arrival: Station;
  totalTargetSeconds: number;
  accumulatedSeconds: number;
  status: 'RUNNING' | 'PAUSED';
  busy: boolean;
  onPause: () => void;
  onResume: () => void;
  onComplete: () => void;
  onAbort: () => void;
}

export function FocusScreen({
  stations,
  departure,
  arrival,
  totalTargetSeconds,
  accumulatedSeconds,
  status,
  busy,
  onPause,
  onResume,
  onComplete,
  onAbort,
}: Props) {
  const [timerMode, setTimerMode] = useState<TimerMode>('remaining');

  const isRunning = status === 'RUNNING';
  const remainingSeconds = Math.max(0, totalTargetSeconds - accumulatedSeconds);
  const targetReached = accumulatedSeconds >= totalTargetSeconds;
  const progressPct =
    totalTargetSeconds > 0
      ? Math.min(100, (accumulatedSeconds / totalTargetSeconds) * 100)
      : 0;

  const displaySeconds =
    timerMode === 'remaining' ? remainingSeconds : accumulatedSeconds;

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_320px]">
      {/* 큰 지도 */}
      <div className="order-2 lg:order-1">
        <KoreaMap
          stations={stations}
          departureId={String(departure.id)}
          arrivalId={String(arrival.id)}
          onStationClick={() => {}}
          progress={progressPct / 100}
          active={true}
        />

        <div className="mt-4 flex items-center justify-between rounded-2xl border border-gray-100 bg-white px-5 py-3 shadow-sm dark:border-gray-700 dark:bg-gray-800">
          <div className="flex items-center gap-3">
            <span
              className={`rounded-full px-3 py-1 text-xs font-bold ${
                isRunning
                  ? 'bg-[#EBFBFA] text-[#2AC1BC] dark:bg-[#14302f]'
                  : 'bg-gray-100 text-gray-500 dark:bg-gray-700 dark:text-gray-400'
              }`}
            >
              {isRunning ? '🚄 운행 중' : '⏸ 하차 중'}
            </span>
            <span className="text-sm font-bold text-gray-700 dark:text-gray-200">
              {departure.name} <span className="text-gray-300 dark:text-gray-600">→</span>{' '}
              {arrival.name}
            </span>
          </div>
          <span className="font-mono text-xs text-gray-400 dark:text-gray-500">
            {Math.round(progressPct)}%
          </span>
        </div>
      </div>

      {/* 사이드 패널 */}
      <aside className="order-1 space-y-4 lg:order-2">
        <section className="rounded-3xl border border-[#2AC1BC]/30 bg-white p-6 shadow-sm dark:bg-gray-800">
          {/* 타이머 모드 토글 */}
          <div className="mb-3 inline-flex rounded-full border border-gray-200 bg-gray-50 p-1 text-[11px] font-bold dark:border-gray-700 dark:bg-gray-700">
            <button
              onClick={() => setTimerMode('remaining')}
              className={`rounded-full px-3 py-1 transition ${
                timerMode === 'remaining'
                  ? 'bg-white text-[#2AC1BC] shadow dark:bg-gray-900'
                  : 'text-gray-400 hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300'
              }`}
            >
              남은 시간
            </button>
            <button
              onClick={() => setTimerMode('elapsed')}
              className={`rounded-full px-3 py-1 transition ${
                timerMode === 'elapsed'
                  ? 'bg-white text-[#2AC1BC] shadow dark:bg-gray-900'
                  : 'text-gray-400 hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300'
              }`}
            >
              경과 시간
            </button>
          </div>

          <div className="mb-2 font-mono text-5xl font-bold text-[#2AC1BC]">
            {formatTime(displaySeconds)}
          </div>
          <p className="mb-5 text-xs text-gray-400 dark:text-gray-500">
            목표 {Math.floor(totalTargetSeconds / 60)}분
          </p>

          <div className="mb-6 h-2 overflow-hidden rounded-full bg-gray-100 dark:bg-gray-700">
            <div
              className="h-full bg-[#2AC1BC] transition-all duration-500"
              style={{ width: `${progressPct}%` }}
            />
          </div>

          <div className="space-y-2">
            {isRunning ? (
              <button
                onClick={onPause}
                disabled={busy}
                className="w-full rounded-2xl border border-gray-200 bg-white py-3 font-bold text-gray-700 hover:bg-gray-50 disabled:opacity-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200 dark:hover:bg-gray-700"
              >
                ⏸ 하차 (일시정지)
              </button>
            ) : (
              <button
                onClick={onResume}
                disabled={busy}
                className="w-full rounded-2xl bg-[#2AC1BC] py-3 font-bold text-white hover:opacity-90 disabled:opacity-50"
              >
                ▶ 재승차
              </button>
            )}

            <button
              onClick={onComplete}
              disabled={busy || isRunning || !targetReached}
              className="w-full rounded-2xl bg-[#2AC1BC] py-3 font-bold text-white hover:opacity-90 disabled:cursor-not-allowed disabled:bg-gray-200 disabled:text-gray-400 dark:disabled:bg-gray-700 dark:disabled:text-gray-500"
              title={
                isRunning
                  ? '하차(일시정지) 후 완료할 수 있어요'
                  : targetReached
                    ? ''
                    : '목표 시간에 도달해야 완료할 수 있어요'
              }
            >
              🎉 도착 (완료)
            </button>

            <button
              onClick={onAbort}
              disabled={busy || isRunning}
              className="w-full rounded-2xl border border-red-200 bg-red-50 py-3 font-bold text-red-500 hover:bg-red-100 disabled:cursor-not-allowed disabled:border-gray-200 disabled:bg-gray-100 disabled:text-gray-300 dark:border-red-900 dark:bg-red-950 dark:text-red-400 dark:hover:bg-red-900 dark:disabled:border-gray-700 dark:disabled:bg-gray-700 dark:disabled:text-gray-600"
              title={isRunning ? '하차(일시정지) 후 종료할 수 있어요' : ''}
            >
              ⏹ 운행 종료
            </button>
          </div>
        </section>
      </aside>
    </div>
  );
}
