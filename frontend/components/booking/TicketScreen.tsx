'use client';

import { Station } from '@/lib/api/stations';

interface Props {
  departure: Station;
  arrival: Station;
  baseDurationMinutes: number;
  delayMinutes: number;
  onBack: () => void;
  onConfirm: () => void;
  busy: boolean;
}

// 가짜 좌석번호 — 즐거움용
function randomSeat() {
  const car = Math.floor(Math.random() * 9) + 1;
  const row = Math.floor(Math.random() * 20) + 1;
  const col = ['A', 'B', 'C', 'D'][Math.floor(Math.random() * 4)];
  return `${car}호차 ${row}${col}`;
}

export function TicketScreen({
  departure,
  arrival,
  baseDurationMinutes,
  delayMinutes,
  onBack,
  onConfirm,
  busy,
}: Props) {
  const totalMinutes = baseDurationMinutes + delayMinutes;
  const now = new Date();
  const arriveAt = new Date(now.getTime() + totalMinutes * 60 * 1000);
  const fmt = (d: Date) =>
    d.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  const seat = randomSeat();

  return (
    <div className="mx-auto w-full max-w-xl px-4">
      <div className="relative overflow-hidden rounded-3xl bg-white shadow-xl dark:bg-gray-800">
        {/* 상단 헤더 */}
        <div className="bg-[#2AC1BC] p-6 text-white">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-widest opacity-80">
              FocusTrain
            </span>
            <span className="text-xs font-bold opacity-80">
              {now.toLocaleDateString('ko-KR')}
            </span>
          </div>
          <p className="mt-1 text-sm font-bold">집중 승차권</p>
        </div>

        {/* 출발 → 도착 */}
        <div className="px-8 pb-5 pt-8">
          <div className="flex items-end justify-between">
            <div>
              <p className="text-xs font-bold uppercase text-gray-400 dark:text-gray-500">
                FROM
              </p>
              <p className="text-4xl font-bold text-gray-800 dark:text-gray-100">
                {departure.name}
              </p>
              <p className="mt-1 font-mono text-sm text-gray-400 dark:text-gray-500">
                출발 {fmt(now)}
              </p>
            </div>
            <div className="mx-4 flex flex-1 items-center">
              <div className="h-px flex-1 border-t-2 border-dashed border-gray-200 dark:border-gray-700" />
              <span className="px-2 text-2xl">🚄</span>
              <div className="h-px flex-1 border-t-2 border-dashed border-gray-200 dark:border-gray-700" />
            </div>
            <div className="text-right">
              <p className="text-xs font-bold uppercase text-gray-400 dark:text-gray-500">
                TO
              </p>
              <p className="text-4xl font-bold text-gray-800 dark:text-gray-100">{arrival.name}</p>
              <p className="mt-1 font-mono text-sm text-gray-400 dark:text-gray-500">
                도착 {fmt(arriveAt)}
              </p>
            </div>
          </div>
        </div>

        {/* 펀치 구멍 + 점선 */}
        <div className="relative my-2">
          <div className="absolute -left-3 top-1/2 h-6 w-6 -translate-y-1/2 rounded-full bg-[var(--background)]" />
          <div className="absolute -right-3 top-1/2 h-6 w-6 -translate-y-1/2 rounded-full bg-[var(--background)]" />
          <div className="mx-6 border-t-2 border-dashed border-gray-200 dark:border-gray-700" />
        </div>

        {/* 상세 */}
        <div className="grid grid-cols-3 gap-3 p-8 text-center">
          <div>
            <p className="text-xs font-bold uppercase text-gray-400 dark:text-gray-500">
              기본 시간
            </p>
            <p className="mt-1.5 text-lg font-bold text-gray-700 dark:text-gray-200">
              {baseDurationMinutes}분
            </p>
          </div>
          <div className="border-x border-gray-100 dark:border-gray-700">
            <p className="text-xs font-bold uppercase text-gray-400 dark:text-gray-500">
              지연
            </p>
            <p className="mt-1.5 text-lg font-bold text-gray-700 dark:text-gray-200">
              +{delayMinutes}분
            </p>
          </div>
          <div>
            <p className="text-xs font-bold uppercase text-gray-400 dark:text-gray-500">
              총 몰입
            </p>
            <p className="mt-1.5 text-lg font-bold text-[#2AC1BC]">
              {totalMinutes}분
            </p>
          </div>
        </div>

        <div className="border-t border-gray-100 p-8 dark:border-gray-700">
          <div className="flex items-center justify-between text-sm">
            <span className="font-bold uppercase text-gray-400 dark:text-gray-500">좌석</span>
            <span className="font-mono font-bold text-gray-700 dark:text-gray-200">{seat}</span>
          </div>
          <div className="mt-3 flex items-center justify-between text-sm">
            <span className="font-bold uppercase text-gray-400 dark:text-gray-500">예약 번호</span>
            <span className="font-mono font-bold text-gray-700 dark:text-gray-200">
              TF-{now.getTime().toString().slice(-8)}
            </span>
          </div>
        </div>

        {/* 가짜 바코드 장식 */}
        <div className="flex h-12 gap-px bg-white px-8 pb-3 dark:bg-gray-800">
          {Array.from({ length: 40 }).map((_, i) => (
            <div
              key={i}
              className="bg-gray-800 dark:bg-gray-300"
              style={{
                width: `${Math.random() * 3 + 1}px`,
                opacity: Math.random() * 0.5 + 0.4,
              }}
            />
          ))}
        </div>
      </div>

      {/* 액션 */}
      <div className="mt-6 flex gap-3">
        <button
          onClick={onBack}
          disabled={busy}
          className="flex-1 rounded-2xl border border-gray-200 bg-white py-4 font-bold text-gray-700 hover:bg-gray-50 disabled:opacity-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200 dark:hover:bg-gray-700"
        >
          ← 뒤로 가기
        </button>
        <button
          onClick={onConfirm}
          disabled={busy}
          className="flex-1 rounded-2xl bg-[#2AC1BC] py-4 font-bold text-white shadow-sm hover:opacity-90 disabled:opacity-50"
        >
          {busy ? '탑승 중...' : '탑승하기 🚄'}
        </button>
      </div>
    </div>
  );
}
