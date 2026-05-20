'use client';

import { useState } from 'react';

interface Step {
  icon: string;
  title: string;
  description: React.ReactNode;
}

const STEPS: Step[] = [
  {
    icon: '🎫',
    title: '승차권 예매',
    description: (
      <>
        지도에서 역을 클릭하거나 좌측 예매창에서{' '}
        <b>출발역과 도착역</b>을 선택하세요.{' '}
        <b>구간별 실제 KTX 이동 시간</b>에{' '}
        <b>추가 지연 시간(Focus Delay)</b>을 더해 집중 시간을 자유롭게 늘릴 수
        있어요.
      </>
    ),
  },
  {
    icon: '🚂',
    title: '집중 운행 시작',
    description: (
      <>
        운행이 시작되면 한반도 지도 위로 기차가 출발해{' '}
        <b>목표 시간만큼 도착역까지 이동</b>합니다. 운행 중에는 경로 변경,
        로그아웃, 다른 페이지 이동이 잠겨요.
      </>
    ),
  },
  {
    icon: '⏸',
    title: '하차 · 도착 · 종료',
    description: (
      <>
        잠시 쉬어가야 할 땐 <b>하차(일시정지)</b>를 누르세요. 하차 상태에서만{' '}
        <b>도착(완료)</b>이나 <b>운행 종료(중단)</b>가 가능합니다. 목표 시간에
        도달해야 도착 처리할 수 있어요.
      </>
    ),
  },
];

interface Props {
  open: boolean;
  onClose: () => void;
}

export function GuideModal({ open, onClose }: Props) {
  const [step, setStep] = useState(0);

  if (!open) return null;

  const current = STEPS[step];
  const isLast = step === STEPS.length - 1;

  function handleClose() {
    setStep(0);
    onClose();
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm"
      onClick={handleClose}
    >
      <div
        className="relative w-full max-w-md rounded-3xl bg-white p-8 shadow-xl dark:bg-gray-800"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          onClick={handleClose}
          className="absolute right-6 top-6 text-2xl text-gray-300 transition hover:text-gray-500 dark:text-gray-600 dark:hover:text-gray-400"
          aria-label="닫기"
        >
          ✕
        </button>

        <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-2xl bg-[#EBFBFA] text-2xl dark:bg-[#14302f]">
          {current.icon}
        </div>

        <h3 className="mb-3 text-2xl font-bold dark:text-gray-100">{current.title}</h3>
        <p className="mb-8 text-sm leading-relaxed text-gray-500 dark:text-gray-400">
          {current.description}
        </p>

        {isLast ? (
          <button
            onClick={handleClose}
            className="w-full rounded-xl bg-[#2AC1BC] py-4 font-bold text-white transition hover:opacity-90"
          >
            시작하기
          </button>
        ) : (
          <button
            onClick={() => setStep((s) => s + 1)}
            className="w-full rounded-xl bg-[#2AC1BC] py-4 font-bold text-white transition hover:opacity-90"
          >
            다음으로
          </button>
        )}

        <div className="mt-6 flex justify-center gap-2">
          {STEPS.map((_, i) => (
            <button
              key={i}
              onClick={() => setStep(i)}
              className={`h-2 rounded-full transition-all ${
                i === step ? 'w-6 bg-[#2AC1BC]' : 'w-2 bg-gray-200 dark:bg-gray-600'
              }`}
              aria-label={`${i + 1}단계로 이동`}
            />
          ))}
        </div>
      </div>
    </div>
  );
}
