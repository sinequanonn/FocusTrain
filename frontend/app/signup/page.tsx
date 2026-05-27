'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks/useAuth';
import { signOut } from '@/lib/firebase/auth';
import { getMe, signUp } from '@/lib/api/auth';
import { ApiError } from '@/lib/api/client';

const NICKNAME_MIN = 2;
const NICKNAME_MAX = 20;

export default function SignupPage() {
  const router = useRouter();
  const { user, loading } = useAuth();

  const [nickname, setNickname] = useState('');
  const [checking, setChecking] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!loading && !user) router.replace('/login');
  }, [user, loading, router]);

  useEffect(() => {
    if (!user) return;
    (async () => {
      try {
        await getMe();
        router.replace('/');
      } catch (e) {
        if (e instanceof ApiError && e.errorCode === 'USER_NOT_FOUND') {
          if (user.displayName) setNickname(user.displayName);
          setChecking(false);
          return;
        }
        handleError(e);
        setChecking(false);
      }
    })();
  }, [user, router]);

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

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);

    const trimmed = nickname.trim();
    if (trimmed.length < NICKNAME_MIN || trimmed.length > NICKNAME_MAX) {
      setError(`닉네임은 ${NICKNAME_MIN}~${NICKNAME_MAX}자여야 합니다.`);
      return;
    }

    setSubmitting(true);
    try {
      await signUp(trimmed);
      router.replace('/');
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.errorCode === 'USER_ALREADY_REGISTERED') {
          router.replace('/');
          return;
        }
        if (err.errorCode === 'USER_NICKNAME_DUPLICATE') {
          setError('이미 사용 중인 닉네임이에요!');
          return;
        }
        if (err.errorCode === 'COMMON_VALIDATION_FAILED') {
          setError(`닉네임은 ${NICKNAME_MIN}~${NICKNAME_MAX}만 가능해요!`);
          return;
        }
      }
      handleError(err);
    } finally {
      setSubmitting(false);
    }
  }

  if (loading || !user || checking) {
    return (
      <main className="flex min-h-screen items-center justify-center p-8 text-gray-500 dark:text-gray-400">
        로딩 중...
      </main>
    );
  }

  const trimmedLength = nickname.trim().length;
  const validLength = trimmedLength >= NICKNAME_MIN && trimmedLength <= NICKNAME_MAX;

  return (
    <main className="relative flex min-h-screen flex-col overflow-hidden bg-gradient-to-b from-[#EBFBFA] via-[#F6F6F6] to-white dark:from-[#0f2625] dark:via-gray-900 dark:to-gray-950">
      <header className="pointer-events-none absolute inset-x-0 top-0 z-30 flex items-start justify-between p-4 md:p-6">
        <div className="pointer-events-auto rounded-2xl border border-gray-100 bg-white/95 px-4 py-2 shadow-md backdrop-blur dark:border-gray-700 dark:bg-gray-800/95">
          <h1 className="text-xl font-bold tracking-tight md:text-2xl">
            <span className="text-[#2AC1BC]">Focus</span>{' '}
            <span className="text-gray-800 dark:text-gray-100">Train</span>
          </h1>
          <p className="text-[10px] text-gray-500 dark:text-gray-400">
            탑승 전 — 닉네임을 정해주세요
          </p>
        </div>

        <nav className="pointer-events-auto mr-14 flex items-center gap-1 rounded-full border border-gray-200/60 bg-white/85 px-1.5 py-1 shadow-sm backdrop-blur-xl dark:border-white/10 dark:bg-gray-900/60">
          <button
            onClick={handleLogout}
            className="rounded-full px-3 py-1.5 text-xs font-medium text-gray-600 transition hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/10"
          >
            로그아웃
          </button>
        </nav>
      </header>

      <div className="relative z-10 mx-auto flex w-full max-w-md flex-1 items-center px-6 py-24">
        <section className="w-full rounded-3xl border border-white bg-white/85 p-8 shadow-xl backdrop-blur-md dark:border-gray-700 dark:bg-gray-800/85">
          <div className="mb-1 inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-[#EBFBFA] text-2xl dark:bg-[#14302f]">
            🎫
          </div>
          <h2 className="mb-2 text-2xl font-bold dark:text-gray-100">
            닉네임 설정
          </h2>
          <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">
            여정에서 사용할 닉네임을 알려주세요. <br />
            나중에 마이페이지에서 바꿀 수 있어요.
          </p>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label
                htmlFor="nickname"
                className="mb-1 block text-xs font-bold uppercase text-gray-400 dark:text-gray-500"
              >
                닉네임
              </label>
              <input
                id="nickname"
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                maxLength={NICKNAME_MAX}
                placeholder="2~20자"
                autoFocus
                disabled={submitting}
                className="w-full rounded-lg border border-gray-200 bg-gray-50 p-3 outline-none focus:border-[#2AC1BC] disabled:opacity-50 dark:border-gray-700 dark:bg-gray-700 dark:text-gray-100"
              />
              <p className="mt-1 text-right text-[11px] text-gray-400 dark:text-gray-500">
                {trimmedLength}/{NICKNAME_MAX}
              </p>
            </div>

            {error && (
              <p className="rounded-lg border border-red-200 bg-red-50 p-3 text-xs text-red-700 dark:border-red-900 dark:bg-red-950/60 dark:text-red-300">
                {error}
              </p>
            )}

            <button
              type="submit"
              disabled={!validLength || submitting}
              className="w-full rounded-2xl bg-[#2AC1BC] py-4 text-base font-bold text-white shadow-sm transition hover:opacity-90 disabled:cursor-not-allowed disabled:bg-gray-200 disabled:text-gray-400 dark:disabled:bg-gray-700 dark:disabled:text-gray-500"
            >
              {submitting ? '가입 중...' : '가입 완료하고 출발 🚄'}
            </button>
          </form>

          <div className="mt-6 border-t border-gray-100 pt-4 text-[10px] leading-relaxed text-gray-400 dark:border-gray-700">
            <p>가입 후 출발역을 골라 본격적인 여정을 시작합니다.</p>
          </div>
        </section>
      </div>

      <footer className="relative z-10 py-6 text-center text-[11px] text-gray-400">
        © {new Date().getFullYear()} FocusTrain
      </footer>
    </main>
  );
}
