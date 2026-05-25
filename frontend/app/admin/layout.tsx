'use client';

import { ReactNode, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks/useAuth';
import { getMe } from '@/lib/api/auth';

export default function AdminLayout({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { user, loading } = useAuth();
  const [checking, setChecking] = useState(true);
  const [allowed, setAllowed] = useState(false);

  useEffect(() => {
    if (loading) return;
    if (!user) {
      router.replace('/login');
      return;
    }
    (async () => {
      try {
        const me = await getMe();
        if (me.role !== 'ADMIN') {
          router.replace('/');
          return;
        }
        setAllowed(true);
      } catch {
        router.replace('/');
      } finally {
        setChecking(false);
      }
    })();
  }, [user, loading, router]);

  if (loading || checking) {
    return <main className="p-8 text-gray-500 dark:text-gray-400">권한 확인 중...</main>;
  }
  if (!allowed) return null;
  return <>{children}</>;
}
