'use client';

import { TransformWrapper, TransformComponent } from 'react-zoom-pan-pinch';
import { Station } from '@/lib/api/stations';

// 역 이름 → SVG viewBox(400x600) 좌표
const STATION_POSITIONS: Record<string, { x: number; y: number }> = {
  서울: { x: 135, y: 130 },
  강릉: { x: 260, y: 110 },
  대전: { x: 165, y: 260 },
  대구: { x: 250, y: 350 },
  광주: { x: 110, y: 420 },
  부산: { x: 285, y: 460 },
  울산: { x: 300, y: 410 },
  목포: { x: 75, y: 480 },
  제주: { x: 160, y: 585 },
};

interface Props {
  stations: Station[];
  departureId: string;
  arrivalId: string;
  onStationClick: (stationId: string) => void;
  /** 진행률 0~1 (운행 중이면 기차 위치 표시용) */
  progress?: number;
  active?: boolean;
}

export function KoreaMap({
  stations,
  departureId,
  arrivalId,
  onStationClick,
  progress = 0,
  active = false,
}: Props) {
  const positioned = stations
    .map((s) => {
      const pos = STATION_POSITIONS[s.name];
      return pos ? { ...s, ...pos } : null;
    })
    .filter((s): s is Station & { x: number; y: number } => s !== null);

  const departure = positioned.find((s) => String(s.id) === departureId);
  const arrival = positioned.find((s) => String(s.id) === arrivalId);

  const pathD =
    departure && arrival
      ? `M ${departure.x} ${departure.y} L ${arrival.x} ${arrival.y}`
      : '';

  // 기차 위치 (선형 보간)
  const trainX =
    departure && arrival
      ? departure.x + (arrival.x - departure.x) * progress
      : null;
  const trainY =
    departure && arrival
      ? departure.y + (arrival.y - departure.y) * progress
      : null;

  // 지도 본체 (예매/집중 화면이 공통으로 사용)
  const mapSvg = (
    <svg viewBox="0 0 400 600" className="w-full max-w-[420px] drop-shadow">
      {/* 한반도 외곽 */}
      <path
        d="M145,55 C160,45 190,40 210,42 C230,45 250,55 265,70 C280,85 290,110 295,130 C300,150 315,180 325,210 C335,240 340,280 338,310 C335,340 320,380 310,410 C300,440 295,480 285,510 C275,540 240,560 210,565 C180,570 140,560 110,540 C80,520 60,480 55,440 C50,400 65,340 60,300 C55,260 50,220 52,180 C55,140 70,100 100,75 C120,60 135,60 145,55 Z"
        fill="#FFFFFF"
        stroke="#2AC1BC"
        strokeWidth={3}
        className="dark:fill-gray-800"
      />
      {/* 제주 */}
      <circle
        cx={160}
        cy={585}
        r={15}
        fill="#FFFFFF"
        stroke="#2AC1BC"
        strokeWidth={2}
        className="dark:fill-gray-800"
      />

      {/* 활성 노선 */}
      {pathD && (
        <path
          d={pathD}
          fill="none"
          stroke="#2AC1BC"
          strokeWidth={5}
          strokeLinecap="round"
          strokeDasharray={active ? undefined : 8}
        />
      )}

      {/* 역 노드 */}
      {positioned.map((s) => {
        const isDeparture = String(s.id) === departureId;
        const isArrival = String(s.id) === arrivalId;
        const isSelected = isDeparture || isArrival;
        return (
          <g
            key={s.id}
            onClick={() => !active && onStationClick(String(s.id))}
            style={{ cursor: active ? 'default' : 'pointer' }}
          >
            <circle
              cx={s.x}
              cy={s.y}
              r={isSelected ? 8 : 5}
              fill={isSelected ? '#2AC1BC' : '#CBD5E0'}
              stroke="white"
              strokeWidth={2}
              className={isSelected ? undefined : 'dark:fill-gray-600'}
            />
            <text
              x={s.x + 10}
              y={s.y + 4}
              fontSize={11}
              fontWeight={700}
              fill={isSelected ? '#2AC1BC' : '#4A5568'}
              className={isSelected ? undefined : 'dark:fill-gray-400'}
            >
              {s.name}
            </text>
          </g>
        );
      })}

      {/* 기차 (운행 중에만) */}
      {active && trainX !== null && trainY !== null && (
        <g
          transform={`translate(${trainX}, ${trainY})`}
          style={{ filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.2))' }}
        >
          <circle r={14} fill="white" stroke="#2AC1BC" strokeWidth={3} />
          <text textAnchor="middle" y={5} fontSize={16}>
            🚂
          </text>
        </g>
      )}
    </svg>
  );

  return (
    <div className="relative flex items-center justify-center overflow-hidden rounded-3xl bg-[#EBFBFA] p-5 shadow-sm dark:bg-[#14302f]">
      {/* 예매·집중 모든 화면에서 마우스/터치로 수동 확대·이동 */}
      <TransformWrapper
        initialScale={1}
        minScale={1}
        maxScale={4}
        doubleClick={{ step: 0.7 }}
        wheel={{ step: 0.08 }}
      >
        {({ resetTransform }) => (
          <>
            <TransformComponent
              wrapperClass="!w-full !cursor-grab active:!cursor-grabbing"
              contentClass="!w-full !flex !justify-center"
            >
              {mapSvg}
            </TransformComponent>
            <button
              onClick={() => resetTransform()}
              className="absolute bottom-4 right-4 z-10 rounded-full border border-gray-200 bg-white/90 px-3 py-1.5 text-xs font-bold text-gray-600 shadow-sm transition hover:bg-white dark:border-gray-700 dark:bg-gray-800/90 dark:text-gray-300 dark:hover:bg-gray-800"
            >
              ⊙ 원래대로
            </button>
          </>
        )}
      </TransformWrapper>

      {!active && (!departure || !arrival) && (
        <div className="pointer-events-none absolute top-6 rounded-full border border-[#2AC1BC]/20 bg-white/90 px-5 py-2 text-sm font-bold text-gray-700 shadow-sm dark:bg-gray-800/90 dark:text-gray-200">
          {!departure ? '출발역을 선택하세요' : '도착역을 선택하세요'}
        </div>
      )}
    </div>
  );
}
