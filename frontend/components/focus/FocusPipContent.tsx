'use client';

/**
 * Picture-in-Picture 창에 표시되는 진행률 + 간단 컨트롤 컴포넌트.
 * - PiP 창에는 메인 document 의 stylesheet 가 자동 적용 안 되므로 inline style 만 사용.
 * - 단일 trackline 에 진행률 색 + 기차 점 통합 (별도 bar 없이 중복 제거).
 * - 하차 / 재승차 버튼 포함 — 메인 창 안 보일 때 직접 제어 가능.
 * - 상단에 청록 헤더 띠 → 브라우저 chrome(URL 바) 과 시각적으로 통합된 느낌
 */

interface Props {
  departureName: string;
  arrivalName: string;
  /** 0~1 */
  progress: number;
  isRunning: boolean;
  busy: boolean;
  onPause: () => void;
  onResume: () => void;
}

export function FocusPipContent({
  departureName,
  arrivalName,
  progress,
  isRunning,
  busy,
  onPause,
  onResume,
}: Props) {
  const pct = Math.round(progress * 100);
  const clamped = Math.max(0, Math.min(100, pct));

  const buttonBase: React.CSSProperties = {
    border: 'none',
    outline: 'none',
    cursor: busy ? 'not-allowed' : 'pointer',
    fontWeight: 700,
    fontSize: '13px',
    padding: '8px 16px',
    borderRadius: '9999px',
    transition: 'opacity 0.15s',
    opacity: busy ? 0.5 : 1,
    whiteSpace: 'nowrap',
  };

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        boxSizing: 'border-box',
        backgroundColor: isRunning ? '#ffffff' : '#f3f4f6',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      {/* 청록 헤더 띠 — URL 바와 시각적 연속성 */}
      <div
        style={{
          backgroundColor: '#2AC1BC',
          color: '#ffffff',
          padding: '6px 16px',
          fontSize: '11px',
          fontWeight: 700,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          letterSpacing: '0.02em',
        }}
      >
        <span>🚄 FocusTrain</span>
        <span style={{ opacity: 0.9 }}>
          {isRunning ? '운행 중' : '하차 중'}
        </span>
      </div>

      {/* 본문 */}
      <div
        style={{
          flex: 1,
          padding: '14px 20px',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          gap: '14px',
        }}
      >
        {/* 출발 ━━━●━━━ 도착  (라인 자체에 진행률 색 채움) */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span
            style={{
              fontSize: '13px',
              fontWeight: 700,
              color: '#374151',
              whiteSpace: 'nowrap',
            }}
          >
            {departureName}
          </span>

          <div
            style={{
              flex: 1,
              position: 'relative',
              height: '6px',
              backgroundColor: '#e5e7eb',
              borderRadius: '9999px',
            }}
          >
            <div
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                height: '100%',
                width: `${clamped}%`,
                backgroundColor: '#2AC1BC',
                borderRadius: '9999px',
                transition: 'width 0.5s ease',
              }}
            />
            <div
              style={{
                position: 'absolute',
                left: `${clamped}%`,
                top: '50%',
                transform: 'translate(-50%, -50%)',
                width: '26px',
                height: '26px',
                borderRadius: '9999px',
                backgroundColor: '#ffffff',
                border: '2px solid #2AC1BC',
                boxShadow: '0 1px 4px rgba(0,0,0,0.28)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '13px',
                transition: 'left 0.5s ease',
              }}
            >
              🚄
            </div>
          </div>

          <span
            style={{
              fontSize: '13px',
              fontWeight: 700,
              color: '#374151',
              whiteSpace: 'nowrap',
            }}
          >
            {arrivalName}
          </span>
        </div>

        {/* 퍼센티지 + 하차/재승차 */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '12px',
          }}
        >
          <span
            style={{
              fontSize: '28px',
              fontWeight: 800,
              color: '#2AC1BC',
              fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace',
              lineHeight: 1,
            }}
          >
            {clamped}%
          </span>

          {isRunning ? (
            <button
              onClick={onPause}
              disabled={busy}
              style={{
                ...buttonBase,
                backgroundColor: '#ffffff',
                color: '#374151',
                border: '1px solid #d1d5db',
              }}
              title="하차 (일시정지)"
            >
              ⏸ 하차
            </button>
          ) : (
            <button
              onClick={onResume}
              disabled={busy}
              style={{
                ...buttonBase,
                backgroundColor: '#2AC1BC',
                color: '#ffffff',
              }}
              title="재승차"
            >
              ▶ 재승차
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
