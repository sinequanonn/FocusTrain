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
    <div className="mx-auto max-w-md">
      <div className="relative overflow-hidden rounded-3xl bg-white shadow-xl">
        {/* 상단 헤더 */}
        <div className="bg-[#2AC1BC] p-5 text-white">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-widest opacity-80">
              Train Focus
            </span>
            <span className="text-xs font-bold opacity-80">
              {now.toLocaleDateString('ko-KR')}
            </span>
          </div>
          <p className="mt-1 text-sm font-bold">집중 승차권</p>
        </div>

        {/* 출발 → 도착 */}
        <div className="px-6 pb-4 pt-6">
          <div className="flex items-end justify-between">
            <div>
              <p className="text-[10px] font-bold uppercase text-gray-400">
                FROM
              </p>
              <p className="text-3xl font-bold text-gray-800">
                {departure.name}
              </p>
              <p className="mt-1 font-mono text-xs text-gray-400">
                출발 {fmt(now)}
              </p>
            </div>
            <div className="mx-3 flex flex-1 items-center">
              <div className="h-px flex-1 border-t-2 border-dashed border-gray-200" />
              <span className="px-2 text-lg">🚄</span>
              <div className="h-px flex-1 border-t-2 border-dashed border-gray-200" />
            </div>
            <div className="text-right">
              <p className="text-[10px] font-bold uppercase text-gray-400">
                TO
              </p>
              <p className="text-3xl font-bold text-gray-800">{arrival.name}</p>
              <p className="mt-1 font-mono text-xs text-gray-400">
                도착 {fmt(arriveAt)}
              </p>
            </div>
          </div>
        </div>

        {/* 펀치 구멍 + 점선 */}
        <div className="relative my-2">
          <div className="absolute -left-3 top-1/2 h-6 w-6 -translate-y-1/2 rounded-full bg-[var(--background)]" />
          <div className="absolute -right-3 top-1/2 h-6 w-6 -translate-y-1/2 rounded-full bg-[var(--background)]" />
          <div className="mx-6 border-t-2 border-dashed border-gray-200" />
        </div>

        {/* 상세 */}
        <div className="grid grid-cols-3 gap-3 p-6 text-center">
          <div>
            <p className="text-[10px] font-bold uppercase text-gray-400">
              기본 시간
            </p>
            <p className="mt-1 text-base font-bold text-gray-700">
              {baseDurationMinutes}분
            </p>
          </div>
          <div className="border-x border-gray-100">
            <p className="text-[10px] font-bold uppercase text-gray-400">
              지연
            </p>
            <p className="mt-1 text-base font-bold text-gray-700">
              +{delayMinutes}분
            </p>
          </div>
          <div>
            <p className="text-[10px] font-bold uppercase text-gray-400">
              총 몰입
            </p>
            <p className="mt-1 text-base font-bold text-[#2AC1BC]">
              {totalMinutes}분
            </p>
          </div>
        </div>

        <div className="border-t border-gray-100 p-6">
          <div className="flex items-center justify-between text-xs">
            <span className="font-bold uppercase text-gray-400">좌석</span>
            <span className="font-mono font-bold text-gray-700">{seat}</span>
          </div>
          <div className="mt-3 flex items-center justify-between text-xs">
            <span className="font-bold uppercase text-gray-400">예약 번호</span>
            <span className="font-mono font-bold text-gray-700">
              TF-{now.getTime().toString().slice(-8)}
            </span>
          </div>
        </div>

        {/* 가짜 바코드 장식 */}
        <div className="flex h-10 gap-px bg-white px-6 pb-2">
          {Array.from({ length: 40 }).map((_, i) => (
            <div
              key={i}
              className="bg-gray-800"
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
          className="flex-1 rounded-2xl border border-gray-200 bg-white py-4 font-bold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
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
