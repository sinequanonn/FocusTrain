'use client';

import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks/useAuth';
import { signOut } from '@/lib/firebase/auth';
import { getMe, MeResponse } from '@/lib/api/auth';
import { getStations, Station } from '@/lib/api/stations';
import { getDuration } from '@/lib/api/routes';
import {
  createSession,
  pauseSession,
  resumeSession,
  completeSession,
  abortSession,
  getActiveSession,
  FocusSessionDetailResponse,
} from '@/lib/api/sessions';
import { ApiError } from '@/lib/api/client';
import { GuideModal } from '@/components/ui/GuideModal';
import { BookingScreen } from '@/components/booking/BookingScreen';
import { TicketScreen } from '@/components/booking/TicketScreen';
import { FocusScreen } from '@/components/focus/FocusScreen';

const GUIDE_SEEN_KEY = 'trainfocus.guide.seen';

type Screen = 'booking' | 'ticket' | 'focus';

export default function HomePage() {
  const router = useRouter();
  const { user, loading } = useAuth();

  const [me, setMe] = useState<MeResponse | null>(null);
  const [stations, setStations] = useState<Station[]>([]);

  // 예매 폼
  const [departureId, setDepartureId] = useState<string>('');
  const [arrivalId, setArrivalId] = useState<string>('');
  const [delayMinutes, setDelayMinutes] = useState<number>(0);
  const [previewMinutes, setPreviewMinutes] = useState<number | null>(null);

  // 세션
  const [session, setSession] = useState<FocusSessionDetailResponse | null>(
    null
  );
  const [accumulatedSeconds, setAccumulatedSeconds] = useState(0);

  // 화면 단계
  const [screen, setScreen] = useState<Screen>('booking');

  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [guideOpen, setGuideOpen] = useState(false);
  const tickRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // 첫 방문 가이드
  useEffect(() => {
    if (typeof window === 'undefined') return;
    const seen = window.localStorage.getItem(GUIDE_SEEN_KEY);
    if (!seen) {
      setGuideOpen(true);
      window.localStorage.setItem(GUIDE_SEEN_KEY, '1');
    }
  }, []);

  useEffect(() => {
    if (!loading && !user) router.replace('/login');
  }, [user, loading, router]);

  useEffect(() => {
    if (!user) return;
    (async () => {
      try {
        const [meRes, stationsRes, activeRes] = await Promise.all([
          getMe(),
          getStations(),
          getActiveSession(),
        ]);
        setMe(meRes);
        setStations(stationsRes.stations);
        if (activeRes.hasActiveSession && activeRes.session) {
          setSession(activeRes.session);
          setAccumulatedSeconds(activeRes.session.accumulatedSeconds);
          setScreen('focus');
        }
      } catch (e) {
        handleError(e);
      }
    })();
  }, [user]);

  // 예매 미리보기 (출발/도착 둘 다 있으면)
  useEffect(() => {
    if (!departureId || !arrivalId || departureId === arrivalId) {
      setPreviewMinutes(null);
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const res = await getDuration(Number(departureId), Number(arrivalId));
        if (!cancelled) setPreviewMinutes(res.durationMinutes);
      } catch {
        if (!cancelled) setPreviewMinutes(null);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [departureId, arrivalId]);

  // 로컬 타이머
  useEffect(() => {
    if (tickRef.current) clearInterval(tickRef.current);
    if (session?.status === 'RUNNING') {
      tickRef.current = setInterval(() => {
        setAccumulatedSeconds((prev) => prev + 1);
      }, 1000);
    }
    return () => {
      if (tickRef.current) clearInterval(tickRef.current);
    };
  }, [session?.status]);

  function handleError(e: unknown) {
    if (e instanceof ApiError) {
      setError(`[${e.status}] ${e.errorCode}: ${e.message}`);
    } else if (e instanceof Error) {
      setError(e.message);
    } else {
      setError('알 수 없는 오류');
    }
  }

  async function handleLogout() {
    await signOut();
    router.replace('/login');
  }

  function handleStationClick(stationId: string) {
    if (screen !== 'booking') return;
    if (!departureId) {
      setDepartureId(stationId);
    } else if (!arrivalId && stationId !== departureId) {
      setArrivalId(stationId);
    } else {
      setDepartureId(stationId);
      setArrivalId('');
    }
  }

  // 1단계: 예매 완료! → 표 화면 (백엔드 호출 없음)
  function handleBookingSubmit() {
    if (!departureId || !arrivalId || departureId === arrivalId) return;
    if (previewMinutes === null) return;
    setError(null);
    setScreen('ticket');
  }

  // 2단계: 탑승하기 → 백엔드 세션 생성 → 집중 화면
  async function handleBoardTrain() {
    setError(null);
    setBusy(true);
    try {
      await createSession({
        departureStationId: Number(departureId),
        arrivalStationId: Number(arrivalId),
        delayMinutes: delayMinutes || 0,
      });
      const active = await getActiveSession();
      if (active.hasActiveSession && active.session) {
        setSession(active.session);
        setAccumulatedSeconds(active.session.accumulatedSeconds);
        setScreen('focus');
      }
    } catch (e) {
      handleError(e);
    } finally {
      setBusy(false);
    }
  }

  async function handlePause() {
    if (!session) return;
    setBusy(true);
    try {
      const res = await pauseSession(session.sessionId);
      setSession({ ...session, status: res.status });
      setAccumulatedSeconds(res.accumulatedSeconds);
    } catch (e) {
      handleError(e);
    } finally {
      setBusy(false);
    }
  }

  async function handleResume() {
    if (!session) return;
    setBusy(true);
    try {
      const res = await resumeSession(session.sessionId);
      setSession({ ...session, status: res.status });
      setAccumulatedSeconds(res.accumulatedSeconds);
    } catch (e) {
      handleError(e);
    } finally {
      setBusy(false);
    }
  }

  async function handleComplete() {
    if (!session) return;
    setBusy(true);
    try {
      await completeSession(session.sessionId);
      resetToBooking();
    } catch (e) {
      handleError(e);
    } finally {
      setBusy(false);
    }
  }

  async function handleAbort() {
    if (!session) return;
    if (!confirm('정말 종료할까요? 진행 기록은 보존됩니다.')) return;
    setBusy(true);
    try {
      await abortSession(session.sessionId);
      resetToBooking();
    } catch (e) {
      handleError(e);
    } finally {
      setBusy(false);
    }
  }

  function resetToBooking() {
    setSession(null);
    setAccumulatedSeconds(0);
    setDepartureId('');
    setArrivalId('');
    setDelayMinutes(0);
    setScreen('booking');
  }

  if (loading || !user) {
    return <main className="p-8 text-gray-500 dark:text-gray-400">로딩 중...</main>;
  }

  const isRunning = session?.status === 'RUNNING';
  const isFocusScreen = screen === 'focus';

  return (
    <main className="mx-auto max-w-6xl p-6 md:p-10">
      {/* 헤더 */}
      <header className="mb-8 flex items-end justify-between">
        <div className="flex items-end gap-3">
          <div>
            <h1 className="text-3xl md:text-4xl font-bold tracking-tight">
              {isFocusScreen ? (
                <span>
                  <span className="text-[#2AC1BC]">Focus</span>{' '}
                  <span className="text-gray-800 dark:text-gray-100">Train</span>
                </span>
              ) : (
                <button
                  onClick={() => router.push('/')}
                  className="hover:opacity-70 transition-opacity"
                >
                  <span className="text-[#2AC1BC]">Focus</span>{' '}
                  <span className="text-gray-800 dark:text-gray-100">Train</span>
                </button>
              )}
            </h1>
            {me && !isFocusScreen && (
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                {me.nickname} · {me.email}
              </p>
            )}
          </div>
          <button
            onClick={() => setGuideOpen(true)}
            disabled={isRunning}
            className="mb-1 flex h-7 w-7 items-center justify-center rounded-full border border-gray-200 bg-white text-sm font-bold text-gray-400 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] disabled:cursor-not-allowed disabled:opacity-40 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-500"
            title={isRunning ? '운행 중에는 사용할 수 없습니다' : '사용법 보기'}
            aria-label="사용법 보기"
          >
            ?
          </button>
        </div>

        {!isFocusScreen && (
          <div className="flex items-center gap-2">
            {isRunning ? (
              <span
                className="cursor-not-allowed rounded-lg border border-gray-200 bg-gray-100 px-3 py-1.5 text-xs font-medium text-gray-300 dark:border-gray-700 dark:bg-gray-700 dark:text-gray-600"
                title="운행 중에는 이동할 수 없습니다"
              >
                내 프로필
              </span>
            ) : (
              <Link
                href="/profile"
                className="rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700"
              >
                내 프로필
              </Link>
            )}
            {isRunning ? (
              <span
                className="cursor-not-allowed rounded-lg border border-gray-200 bg-gray-100 px-3 py-1.5 text-xs font-medium text-gray-300 dark:border-gray-700 dark:bg-gray-700 dark:text-gray-600"
                title="운행 중에는 이동할 수 없습니다"
              >
                이동 기록
              </span>
            ) : (
              <Link
                href="/history"
                className="rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700"
              >
                이동 기록
              </Link>
            )}
            <button
              onClick={handleLogout}
              disabled={isRunning}
              className="rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 disabled:cursor-not-allowed disabled:bg-gray-100 disabled:text-gray-300 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700 dark:disabled:bg-gray-700 dark:disabled:text-gray-600"
              title={isRunning ? '운행 중에는 로그아웃할 수 없습니다' : ''}
            >
              로그아웃
            </button>
          </div>
        )}
      </header>

      {error && (
        <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
          {error}
        </div>
      )}

      {screen === 'booking' && (
        <BookingScreen
          stations={stations}
          departureId={departureId}
          arrivalId={arrivalId}
          delayMinutes={delayMinutes}
          previewMinutes={previewMinutes}
          onDepartureChange={setDepartureId}
          onArrivalChange={setArrivalId}
          onDelayChange={setDelayMinutes}
          onStationClick={handleStationClick}
          onSubmit={handleBookingSubmit}
          busy={busy}
        />
      )}

      {screen === 'ticket' &&
        previewMinutes !== null &&
        departureId &&
        arrivalId && (
          <TicketScreen
            departure={stations.find((s) => String(s.id) === departureId)!}
            arrival={stations.find((s) => String(s.id) === arrivalId)!}
            baseDurationMinutes={previewMinutes}
            delayMinutes={delayMinutes}
            onBack={() => setScreen('booking')}
            onConfirm={handleBoardTrain}
            busy={busy}
          />
        )}

      {screen === 'focus' && session && (
        <FocusScreen
          stations={stations}
          departure={session.departure}
          arrival={session.arrival}
          totalTargetSeconds={session.totalTargetSeconds}
          accumulatedSeconds={accumulatedSeconds}
          status={session.status === 'RUNNING' ? 'RUNNING' : 'PAUSED'}
          busy={busy}
          onPause={handlePause}
          onResume={handleResume}
          onComplete={handleComplete}
          onAbort={handleAbort}
        />
      )}

      <GuideModal open={guideOpen} onClose={() => setGuideOpen(false)} />
    </main>
  );
}
