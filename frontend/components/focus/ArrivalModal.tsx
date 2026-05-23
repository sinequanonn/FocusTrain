'use client';

interface Props {
  open: boolean;
  arrivalName: string;
  accumulatedSeconds: number;
  totalTargetSeconds: number;
  auto: boolean;
  onClose: () => void;
}

function formatDuration(totalSeconds: number) {
  const safe = Math.max(0, totalSeconds);
  const h = Math.floor(safe / 3600);
  const m = Math.floor((safe % 3600) / 60);
  if (h > 0) return `${h}시간 ${m}분`;
  return `${m}분`;
}

export function ArrivalModal({
  open,
  arrivalName,
  accumulatedSeconds,
  totalTargetSeconds,
  auto,
  onClose,
}: Props) {
  if (!open) return null;
  return (
    <div className="absolute inset-0 z-40 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="mx-4 w-full max-w-sm rounded-3xl bg-white p-8 shadow-2xl dark:bg-gray-800">
        <div className="text-center">
          <div className="mb-3 text-6xl" aria-hidden>
            🎉
          </div>
          <h2 className="mb-1 text-2xl font-bold text-gray-800 dark:text-gray-100">
            도착했어요!
          </h2>
          <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">
            <span className="font-bold text-[#2AC1BC]">{arrivalName}</span> 역에
            무사히 도착했어요.
          </p>

          <div className="mb-6 rounded-2xl border border-[#2AC1BC]/20 bg-[#EBFBFA] p-4 dark:bg-[#14302f]">
            <p className="text-[10px] font-bold uppercase tracking-widest text-gray-500 dark:text-gray-400">
              오늘 집중 시간
            </p>
            <p className="mt-1 text-3xl font-bold text-[#2AC1BC]">
              {formatDuration(accumulatedSeconds)}
            </p>
            <p className="mt-1 text-[10px] text-gray-400 dark:text-gray-500">
              목표 {formatDuration(totalTargetSeconds)}
              {auto && ' · 자동 도착'}
            </p>
          </div>

          <button
            onClick={onClose}
            className="w-full rounded-2xl bg-[#2AC1BC] py-4 text-base font-bold text-white shadow-sm transition hover:opacity-90"
          >
            다음 여정 준비 🎫
          </button>
        </div>
      </div>
    </div>
  );
}
