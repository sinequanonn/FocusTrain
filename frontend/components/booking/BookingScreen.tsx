'use client';

import { Station } from '@/lib/api/stations';
import { KoreaMap } from '@/components/map/KoreaMap';

interface Props {
  stations: Station[];
  departureId: string;
  arrivalId: string;
  delayMinutes: number;
  previewMinutes: number | null;
  onDepartureChange: (id: string) => void;
  onArrivalChange: (id: string) => void;
  onDelayChange: (m: number) => void;
  onStationClick: (id: string) => void;
  onSubmit: () => void;
  busy: boolean;
}

export function BookingScreen({
  stations,
  departureId,
  arrivalId,
  delayMinutes,
  previewMinutes,
  onDepartureChange,
  onArrivalChange,
  onDelayChange,
  onStationClick,
  onSubmit,
  busy,
}: Props) {
  const canSubmit =
    !busy && !!departureId && !!arrivalId && departureId !== arrivalId;

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <div className="space-y-6 lg:col-span-1">
        <section className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm">
          <h2 className="mb-5 flex items-center gap-2 text-lg font-bold">
            <span className="rounded bg-[#2AC1BC] px-1.5 py-0.5 text-sm text-white">
              🎫
            </span>{' '}
            승차권 예매
          </h2>

          <div className="space-y-4">
            <div>
              <label className="mb-1 block text-xs font-bold uppercase text-gray-400">
                출발역
              </label>
              <select
                value={departureId}
                onChange={(e) => onDepartureChange(e.target.value)}
                className="w-full rounded-lg border border-gray-200 bg-gray-50 p-3 outline-none focus:border-[#2AC1BC]"
              >
                <option value="">출발역 선택</option>
                {stations.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-xs font-bold uppercase text-gray-400">
                도착역
              </label>
              <select
                value={arrivalId}
                onChange={(e) => onArrivalChange(e.target.value)}
                className="w-full rounded-lg border border-gray-200 bg-gray-50 p-3 outline-none focus:border-[#2AC1BC]"
              >
                <option value="">도착역 선택</option>
                {stations.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex items-center justify-between rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3">
              <div>
                <p className="text-[10px] font-bold uppercase text-gray-400">
                  Focus Delay
                </p>
                <p className="text-xs font-bold text-gray-600">
                  열차 지연 시간
                </p>
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="number"
                  min={0}
                  step={10}
                  value={delayMinutes}
                  onChange={(e) =>
                    onDelayChange(Math.max(0, Number(e.target.value) || 0))
                  }
                  className="w-20 rounded-lg border border-gray-200 bg-white p-2 text-center font-bold text-sm outline-none focus:border-[#2AC1BC]"
                />
                <span className="text-xs font-bold text-gray-400">분</span>
              </div>
            </div>

            {previewMinutes !== null && (
              <div className="rounded-2xl border border-[#2AC1BC]/20 bg-[#EBFBFA] p-4 text-center">
                <p className="text-xs text-gray-500">총 몰입 시간</p>
                <p className="text-2xl font-bold text-[#2AC1BC]">
                  {previewMinutes + delayMinutes}분
                </p>
                <p className="mt-1 text-[10px] text-gray-400">
                  기본 {previewMinutes}분 + 지연 {delayMinutes}분
                </p>
              </div>
            )}

            <button
              onClick={onSubmit}
              disabled={!canSubmit}
              className="w-full rounded-2xl bg-[#2AC1BC] py-4 text-base font-bold text-white shadow-sm hover:opacity-90 disabled:cursor-not-allowed disabled:bg-gray-200 disabled:text-gray-400"
            >
              예매 완료! 🎫
            </button>
          </div>
        </section>
      </div>

      <div className="lg:col-span-2">
        <KoreaMap
          stations={stations}
          departureId={departureId}
          arrivalId={arrivalId}
          onStationClick={onStationClick}
          progress={0}
          active={false}
        />
      </div>
    </div>
  );
}
