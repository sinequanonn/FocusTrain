'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks/useAuth';
import { getMe, updateNickname, MeResponse } from '@/lib/api/auth';
import { ApiError } from '@/lib/api/client';

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

export default function ProfilePage() {
  const router = useRouter();
  const { user, loading } = useAuth();

  const [me, setMe] = useState<MeResponse | null>(null);
  const [nickname, setNickname] = useState('');
  const [originalNickname, setOriginalNickname] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [savedMessage, setSavedMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!loading && !user) router.replace('/login');
  }, [user, loading, router]);

  useEffect(() => {
    if (!user) return;
    (async () => {
      try {
        const res = await getMe();
        setMe(res);
        setNickname(res.nickname);
        setOriginalNickname(res.nickname);
      } catch (e) {
        if (e instanceof ApiError) {
          setError(`[${e.status}] ${e.errorCode}: ${e.message}`);
        } else if (e instanceof Error) {
          setError(e.message);
        }
      }
    })();
  }, [user]);

  async function handleSave() {
    setError(null);
    setSavedMessage(null);
    setSaving(true);
    try {
      const updated = await updateNickname(nickname.trim());
      setMe(updated);
      setNickname(updated.nickname);
      setOriginalNickname(updated.nickname);
      setSavedMessage('닉네임이 저장되었습니다.');
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.message);
      } else if (e instanceof Error) {
        setError(e.message);
      }
    } finally {
      setSaving(false);
    }
  }

  function handleReset() {
    setNickname(originalNickname);
    setSavedMessage(null);
  }

  if (loading || !user) {
    return <main className="p-8 text-gray-500 dark:text-gray-400">로딩 중...</main>;
  }

  const NICKNAME_PATTERN = /^[a-zA-Z0-9가-힣_-]+$/;
  const dirty = nickname !== originalNickname;
  const valid =
    nickname.trim().length >= 2 &&
    nickname.trim().length <= 20 &&
    NICKNAME_PATTERN.test(nickname.trim());

  return (
    <main className="mx-auto max-w-2xl p-6 md:p-10">
      <Link
        href="/"
        className="mb-2 inline-block text-xs text-gray-400 hover:text-[#2AC1BC] dark:text-gray-500"
      >
        ← 메인으로
      </Link>
      <h1 className="mb-6 text-2xl md:text-3xl font-bold tracking-tight">
        <span className="text-[#2AC1BC]">내</span>{' '}
        <span className="text-gray-800 dark:text-gray-100">프로필</span>
      </h1>

      {error && (
        <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
          {error}
        </div>
      )}
      {savedMessage && (
        <div className="mb-6 rounded-xl border border-[#2AC1BC]/30 bg-[#EBFBFA] p-3 text-sm text-[#2AC1BC] dark:bg-[#14302f]">
          {savedMessage}
        </div>
      )}

      <section className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        {!me ? (
          <p className="text-sm text-gray-400 dark:text-gray-500">불러오는 중...</p>
        ) : (
          <div className="space-y-6">
            <div className="flex items-center gap-4 border-b border-gray-100 pb-6 dark:border-gray-700">
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-[#EBFBFA] text-2xl dark:bg-[#14302f]">
                🚄
              </div>
              <div>
                <p className="text-base font-bold text-gray-800 dark:text-gray-100">{me.email}</p>
                <p className="text-[10px] text-gray-400 dark:text-gray-500">
                  가입일 {formatDate(me.createdAt)}
                </p>
              </div>
            </div>

            <div>
              <label className="mb-1 block text-xs font-bold uppercase text-gray-400 dark:text-gray-500">
                닉네임
              </label>
              <input
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                maxLength={20}
                placeholder="2~20자 · 한글, 영문, 숫자, _, -"
                className="w-full rounded-lg border border-gray-200 bg-gray-50 p-3 outline-none focus:border-[#2AC1BC] dark:border-gray-700 dark:bg-gray-700 dark:text-gray-100"
              />
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleReset}
                disabled={!dirty || saving}
                className="flex-1 rounded-2xl border border-gray-200 bg-white py-3 font-bold text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200 dark:hover:bg-gray-700"
              >
                되돌리기
              </button>
              <button
                onClick={handleSave}
                disabled={!dirty || !valid || saving}
                className="flex-1 rounded-2xl bg-[#2AC1BC] py-3 font-bold text-white hover:opacity-90 disabled:cursor-not-allowed disabled:bg-gray-200 disabled:text-gray-400 dark:disabled:bg-gray-700 dark:disabled:text-gray-500"
              >
                {saving ? '저장 중...' : '저장'}
              </button>
            </div>

          </div>
        )}
      </section>
    </main>
  );
}
