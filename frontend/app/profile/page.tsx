'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks/useAuth';
import { getMe, MeResponse } from '@/lib/api/auth';
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
      // TODO: 닉네임 수정 API 연결 (현재 미구현)
      await new Promise((r) => setTimeout(r, 400)); // 로딩 흉내
      setOriginalNickname(nickname);
      setSavedMessage('닉네임이 저장되었습니다. (API 연결 전 — 화면에만 반영)');
    } catch (e) {
      if (e instanceof Error) setError(e.message);
    } finally {
      setSaving(false);
    }
  }

  function handleReset() {
    setNickname(originalNickname);
    setSavedMessage(null);
  }

  if (loading || !user) {
    return <main className="p-8 text-gray-500">로딩 중...</main>;
  }

  const dirty = nickname !== originalNickname;
  const valid = nickname.trim().length >= 1 && nickname.trim().length <= 50;

  return (
    <main className="mx-auto max-w-2xl p-6 md:p-10">
      <Link
        href="/"
        className="mb-2 inline-block text-xs text-gray-400 hover:text-[#2AC1BC]"
      >
        ← 메인으로
      </Link>
      <h1 className="mb-6 text-2xl md:text-3xl font-bold tracking-tight">
        <span className="text-[#2AC1BC]">내</span>{' '}
        <span className="text-gray-800">프로필</span>
      </h1>

      {error && (
        <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {error}
        </div>
      )}
      {savedMessage && (
        <div className="mb-6 rounded-xl border border-[#2AC1BC]/30 bg-[#EBFBFA] p-3 text-sm text-[#2AC1BC]">
          {savedMessage}
        </div>
      )}

      <section className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm">
        {!me ? (
          <p className="text-sm text-gray-400">불러오는 중...</p>
        ) : (
          <div className="space-y-6">
            <div className="flex items-center gap-4 border-b border-gray-100 pb-6">
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-[#EBFBFA] text-2xl">
                🚄
              </div>
              <div>
                <p className="text-xs font-bold uppercase text-gray-400">
                  user #{me.userId}
                </p>
                <p className="text-base font-bold text-gray-800">{me.email}</p>
                <p className="text-[10px] text-gray-400">
                  가입일 {formatDate(me.createdAt)}
                </p>
              </div>
            </div>

            <div>
              <label className="mb-1 block text-xs font-bold uppercase text-gray-400">
                닉네임
              </label>
              <input
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                maxLength={50}
                placeholder="1~50자"
                className="w-full rounded-lg border border-gray-200 bg-gray-50 p-3 outline-none focus:border-[#2AC1BC]"
              />
              <p className="mt-1 text-[10px] text-gray-400">
                다른 사용자에게 표시되지 않으며, 본인 프로필에만 노출됩니다.
              </p>
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleReset}
                disabled={!dirty || saving}
                className="flex-1 rounded-2xl border border-gray-200 bg-white py-3 font-bold text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
              >
                되돌리기
              </button>
              <button
                onClick={handleSave}
                disabled={!dirty || !valid || saving}
                className="flex-1 rounded-2xl bg-[#2AC1BC] py-3 font-bold text-white hover:opacity-90 disabled:cursor-not-allowed disabled:bg-gray-200 disabled:text-gray-400"
              >
                {saving ? '저장 중...' : '저장'}
              </button>
            </div>

            <div className="rounded-xl border border-dashed border-gray-200 bg-gray-50 p-3 text-[11px] text-gray-400">
              ⚠️ 닉네임 수정 API는 아직 연결되지 않았습니다. 현재는 화면에만
              반영됩니다.
            </div>
          </div>
        )}
      </section>
    </main>
  );
}
