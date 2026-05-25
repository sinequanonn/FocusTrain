'use client';

import Link from 'next/link';
import { FormEvent, useCallback, useEffect, useState } from 'react';
import { ApiError } from '@/lib/api/client';
import {
  RouteRequest,
  RouteResponse,
  createRoutes,
  deleteRoute,
  getAdminRoutes,
  updateRouteDuration,
} from '@/lib/api/routes';
import { Station, getStations } from '@/lib/api/stations';

const PAGE_SIZE = 50;
const SEARCH_DEBOUNCE_MS = 300;

type CreateForm = {
  departureStationId: string;
  arrivalStationId: string;
  durationMinutes: string;
  bidirectional: boolean;
};

const EMPTY_CREATE_FORM: CreateForm = {
  departureStationId: '',
  arrivalStationId: '',
  durationMinutes: '',
  bidirectional: false,
};

export default function AdminRoutesPage() {
  const [stations, setStations] = useState<Station[]>([]);

  const [routes, setRoutes] = useState<RouteResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState<string | null>(null);

  const [searchInput, setSearchInput] = useState('');
  const [keyword, setKeyword] = useState('');

  const [createForm, setCreateForm] = useState<CreateForm>(EMPTY_CREATE_FORM);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editDuration, setEditDuration] = useState('');
  const [saving, setSaving] = useState(false);
  const [editError, setEditError] = useState<string | null>(null);

  const [deletingId, setDeletingId] = useState<number | null>(null);

  const fetchPage = useCallback(
    async (nextPage: number, nextKeyword: string) => {
      setLoading(true);
      setListError(null);
      try {
        const res = await getAdminRoutes({
          q: nextKeyword,
          page: nextPage,
          size: PAGE_SIZE,
        });
        setRoutes(res.routes);
        setPage(res.page);
        setTotalPages(res.totalPages);
        setTotalElements(res.totalElements);
      } catch (e) {
        setListError(toErrorMessage(e));
      } finally {
        setLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    (async () => {
      try {
        const res = await getStations();
        setStations(res.stations);
      } catch {
        // 등록 폼 select 비어 있을 수 있으나 목록 조회와 분리
      }
    })();
  }, []);

  useEffect(() => {
    fetchPage(0, keyword);
  }, [keyword, fetchPage]);

  useEffect(() => {
    const handle = setTimeout(() => {
      setKeyword(searchInput);
    }, SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(handle);
  }, [searchInput]);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setCreateError(null);
    const depId = Number(createForm.departureStationId);
    const arrId = Number(createForm.arrivalStationId);
    const duration = Number(createForm.durationMinutes);
    if (!Number.isFinite(depId) || !Number.isFinite(arrId) || depId === 0 || arrId === 0) {
      setCreateError('출발역과 도착역을 선택해주세요.');
      return;
    }
    if (depId === arrId) {
      setCreateError('출발역과 도착역이 같을 수 없습니다.');
      return;
    }
    if (!Number.isFinite(duration) || duration <= 0) {
      setCreateError('소요 시간은 양의 정수여야 합니다.');
      return;
    }
    const request: RouteRequest = {
      departureStationId: depId,
      arrivalStationId: arrId,
      durationMinutes: duration,
      bidirectional: createForm.bidirectional,
    };
    setCreating(true);
    try {
      await createRoutes(request);
      setCreateForm(EMPTY_CREATE_FORM);
      await fetchPage(page, keyword);
    } catch (e) {
      setCreateError(toErrorMessage(e));
    } finally {
      setCreating(false);
    }
  };

  const startEdit = (route: RouteResponse) => {
    setEditingId(route.id);
    setEditDuration(String(route.durationMinutes));
    setEditError(null);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditDuration('');
    setEditError(null);
  };

  const handleUpdate = async (routeId: number) => {
    setEditError(null);
    const duration = Number(editDuration);
    if (!Number.isFinite(duration) || duration <= 0) {
      setEditError('소요 시간은 양의 정수여야 합니다.');
      return;
    }
    setSaving(true);
    try {
      await updateRouteDuration(routeId, duration);
      cancelEdit();
      await fetchPage(page, keyword);
    } catch (e) {
      setEditError(toErrorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (route: RouteResponse) => {
    const ok = window.confirm(
      `${route.departureStationName} → ${route.arrivalStationName} 노선을 삭제할까요?`
    );
    if (!ok) return;
    setDeletingId(route.id);
    try {
      await deleteRoute(route.id);
      const nextPage = routes.length === 1 && page > 0 ? page - 1 : page;
      await fetchPage(nextPage, keyword);
    } catch (e) {
      setListError(toErrorMessage(e));
    } finally {
      setDeletingId(null);
    }
  };

  const goPrev = () => {
    if (page > 0) fetchPage(page - 1, keyword);
  };
  const goNext = () => {
    if (page < totalPages - 1) fetchPage(page + 1, keyword);
  };

  return (
    <main className="mx-auto max-w-4xl p-6 md:p-10">
      <Link
        href="/admin"
        className="mb-2 inline-block text-xs text-gray-400 hover:text-[#2AC1BC] dark:text-gray-500"
      >
        ← 관리자 홈
      </Link>
      <h1 className="mb-6 text-2xl md:text-3xl font-bold tracking-tight">
        <span className="text-[#2AC1BC]">노선</span>{' '}
        <span className="text-gray-800 dark:text-gray-100">관리</span>
      </h1>

      <section className="mb-6 rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        <h2 className="mb-3 text-sm font-bold uppercase text-gray-400 dark:text-gray-500">
          새 노선 등록
        </h2>
        <form onSubmit={handleCreate} className="space-y-3">
          <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div>
              <label
                htmlFor="create-dep"
                className="mb-1 block text-xs font-bold text-gray-500 dark:text-gray-400"
              >
                출발역
              </label>
              <select
                id="create-dep"
                value={createForm.departureStationId}
                onChange={(e) =>
                  setCreateForm({ ...createForm, departureStationId: e.target.value })
                }
                disabled={creating}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm text-gray-800 outline-none transition focus:border-[#2AC1BC] disabled:opacity-50 dark:border-gray-600 dark:bg-gray-900 dark:text-gray-100"
                required
              >
                <option value="">선택</option>
                {stations.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label
                htmlFor="create-arr"
                className="mb-1 block text-xs font-bold text-gray-500 dark:text-gray-400"
              >
                도착역
              </label>
              <select
                id="create-arr"
                value={createForm.arrivalStationId}
                onChange={(e) =>
                  setCreateForm({ ...createForm, arrivalStationId: e.target.value })
                }
                disabled={creating}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm text-gray-800 outline-none transition focus:border-[#2AC1BC] disabled:opacity-50 dark:border-gray-600 dark:bg-gray-900 dark:text-gray-100"
                required
              >
                <option value="">선택</option>
                {stations.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label
              htmlFor="create-duration"
              className="mb-1 block text-xs font-bold text-gray-500 dark:text-gray-400"
            >
              소요 시간 (분)
            </label>
            <input
              id="create-duration"
              type="number"
              min={1}
              value={createForm.durationMinutes}
              onChange={(e) =>
                setCreateForm({ ...createForm, durationMinutes: e.target.value })
              }
              disabled={creating}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm text-gray-800 outline-none transition focus:border-[#2AC1BC] disabled:opacity-50 dark:border-gray-600 dark:bg-gray-900 dark:text-gray-100"
              required
            />
          </div>
          <label className="flex cursor-pointer items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
            <input
              type="checkbox"
              checked={createForm.bidirectional}
              onChange={(e) =>
                setCreateForm({ ...createForm, bidirectional: e.target.checked })
              }
              disabled={creating}
              className="h-4 w-4 rounded border-gray-300 text-[#2AC1BC] focus:ring-[#2AC1BC]"
            />
            <span>양방향 등록 (A→B, B→A 동시 생성)</span>
          </label>
          {createError && (
            <p className="text-sm font-bold text-red-600 dark:text-red-400">{createError}</p>
          )}
          <button
            type="submit"
            disabled={creating}
            className="w-full rounded-2xl bg-[#2AC1BC] py-3 text-sm font-bold text-white shadow-sm transition hover:opacity-90 disabled:opacity-50"
          >
            {creating ? '등록 중...' : '등록'}
          </button>
        </form>
      </section>

      <section className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        <div className="mb-3 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <h2 className="text-sm font-bold uppercase text-gray-400 dark:text-gray-500">
            노선 목록 {totalElements > 0 && `(총 ${totalElements})`}
          </h2>
          <input
            type="search"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="출발역 또는 도착역 이름 검색"
            className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm text-gray-800 outline-none transition focus:border-[#2AC1BC] dark:border-gray-600 dark:bg-gray-900 dark:text-gray-100 md:w-72"
          />
        </div>
        {loading && (
          <p className="text-sm text-gray-400 dark:text-gray-500">불러오는 중...</p>
        )}
        {listError && (
          <p className="text-sm font-bold text-red-600 dark:text-red-400">{listError}</p>
        )}
        {!loading && !listError && routes.length === 0 && (
          <p className="text-sm text-gray-400 dark:text-gray-500">
            {keyword ? '검색 결과가 없습니다.' : '등록된 노선이 없습니다.'}
          </p>
        )}
        {!loading && !listError && routes.length > 0 && (
          <ul className="divide-y divide-gray-100 dark:divide-gray-700">
            {routes.map((route) => (
              <li key={route.id} className="py-3">
                {editingId === route.id ? (
                  <div className="space-y-2">
                    <p className="text-sm font-bold text-gray-800 dark:text-gray-100">
                      {route.departureStationName}{' '}
                      <span className="text-gray-400">→</span>{' '}
                      {route.arrivalStationName}
                    </p>
                    <div className="flex items-center gap-2">
                      <input
                        type="number"
                        min={1}
                        value={editDuration}
                        onChange={(e) => setEditDuration(e.target.value)}
                        disabled={saving}
                        className="w-24 rounded-xl border border-gray-200 bg-white px-3 py-1 text-sm text-gray-800 outline-none transition focus:border-[#2AC1BC] disabled:opacity-50 dark:border-gray-600 dark:bg-gray-900 dark:text-gray-100"
                      />
                      <span className="text-xs text-gray-400 dark:text-gray-500">분</span>
                      <button
                        type="button"
                        onClick={() => handleUpdate(route.id)}
                        disabled={saving}
                        className="ml-auto rounded-xl bg-[#2AC1BC] px-3 py-1 text-xs font-bold text-white shadow-sm transition hover:opacity-90 disabled:opacity-50"
                      >
                        {saving ? '저장 중...' : '저장'}
                      </button>
                      <button
                        type="button"
                        onClick={cancelEdit}
                        disabled={saving}
                        className="rounded-xl border border-gray-200 px-3 py-1 text-xs font-bold text-gray-600 transition hover:bg-gray-50 disabled:opacity-50 dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-700"
                      >
                        취소
                      </button>
                    </div>
                    {editError && (
                      <p className="text-sm font-bold text-red-600 dark:text-red-400">
                        {editError}
                      </p>
                    )}
                  </div>
                ) : (
                  <div className="flex items-center justify-between gap-3">
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-bold text-gray-800 dark:text-gray-100">
                        {route.departureStationName}{' '}
                        <span className="text-gray-400">→</span>{' '}
                        {route.arrivalStationName}
                      </p>
                      <p className="text-xs text-gray-400 dark:text-gray-500">
                        {route.durationMinutes}분
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => startEdit(route)}
                      className="rounded-xl border border-gray-200 px-3 py-1 text-xs font-bold text-gray-600 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] dark:border-gray-600 dark:text-gray-300"
                    >
                      수정
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(route)}
                      disabled={deletingId === route.id}
                      className="rounded-xl border border-gray-200 px-3 py-1 text-xs font-bold text-gray-600 transition hover:border-red-500 hover:text-red-500 disabled:opacity-50 dark:border-gray-600 dark:text-gray-300"
                    >
                      {deletingId === route.id ? '삭제 중...' : '삭제'}
                    </button>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}

        {totalPages > 1 && (
          <div className="mt-4 flex items-center justify-between text-sm">
            <button
              type="button"
              onClick={goPrev}
              disabled={page === 0 || loading}
              className="rounded-xl border border-gray-200 px-3 py-1 font-bold text-gray-600 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] disabled:opacity-30 dark:border-gray-600 dark:text-gray-300"
            >
              ← 이전
            </button>
            <span className="text-xs text-gray-400 dark:text-gray-500">
              {page + 1} / {totalPages}
            </span>
            <button
              type="button"
              onClick={goNext}
              disabled={page >= totalPages - 1 || loading}
              className="rounded-xl border border-gray-200 px-3 py-1 font-bold text-gray-600 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] disabled:opacity-30 dark:border-gray-600 dark:text-gray-300"
            >
              다음 →
            </button>
          </div>
        )}
      </section>
    </main>
  );
}

function toErrorMessage(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.errorCode === 'ROUTE_DUPLICATE') return '이미 등록된 노선입니다.';
    if (e.errorCode === 'ROUTE_SAME_STATION') return '출발역과 도착역이 같을 수 없습니다.';
    if (e.errorCode === 'ROUTE_DURATION_NOT_POSITIVE')
      return '소요 시간은 양의 정수여야 합니다.';
    if (e.errorCode === 'ROUTE_NOT_FOUND') return '노선을 찾을 수 없습니다.';
    if (e.errorCode === 'STATION_NOT_FOUND') return '선택한 역을 찾을 수 없습니다.';
    if (e.errorCode === 'AUTH_FORBIDDEN_ADMIN_ONLY') return '관리자 권한이 필요합니다.';
    return `[${e.status}] ${e.errorCode}: ${e.message}`;
  }
  if (e instanceof Error) return e.message;
  return '알 수 없는 오류가 발생했습니다.';
}
