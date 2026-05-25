'use client';

import Link from 'next/link';
import { FormEvent, useEffect, useState } from 'react';
import { ApiError } from '@/lib/api/client';
import {
  Station,
  StationRequest,
  createStation,
  getStations,
  updateStation,
} from '@/lib/api/stations';

const LAT_MIN = 33;
const LAT_MAX = 39;
const LNG_MIN = 124;
const LNG_MAX = 131;

type FormState = {
  name: string;
  latitude: string;
  longitude: string;
};

const EMPTY_FORM: FormState = { name: '', latitude: '', longitude: '' };

export default function AdminStationsPage() {
  const [stations, setStations] = useState<Station[]>([]);
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState<string | null>(null);

  const [createForm, setCreateForm] = useState<FormState>(EMPTY_FORM);
  const [createError, setCreateError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState<FormState>(EMPTY_FORM);
  const [editError, setEditError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const refetch = async () => {
    setLoading(true);
    setListError(null);
    try {
      const res = await getStations();
      setStations(res.stations);
    } catch (e) {
      setListError(toErrorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refetch();
  }, []);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setCreateError(null);
    const parsed = parseForm(createForm);
    if (!parsed.ok) {
      setCreateError(parsed.message);
      return;
    }
    setCreating(true);
    try {
      await createStation(parsed.value);
      setCreateForm(EMPTY_FORM);
      await refetch();
    } catch (e) {
      setCreateError(toErrorMessage(e));
    } finally {
      setCreating(false);
    }
  };

  const startEdit = (station: Station) => {
    setEditingId(station.id);
    setEditForm({
      name: station.name,
      latitude: String(station.latitude),
      longitude: String(station.longitude),
    });
    setEditError(null);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditForm(EMPTY_FORM);
    setEditError(null);
  };

  const handleUpdate = async (stationId: number) => {
    setEditError(null);
    const parsed = parseForm(editForm);
    if (!parsed.ok) {
      setEditError(parsed.message);
      return;
    }
    setSaving(true);
    try {
      await updateStation(stationId, parsed.value);
      cancelEdit();
      await refetch();
    } catch (e) {
      setEditError(toErrorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  return (
    <main className="mx-auto max-w-3xl p-6 md:p-10">
      <Link
        href="/admin"
        className="mb-2 inline-block text-xs text-gray-400 hover:text-[#2AC1BC] dark:text-gray-500"
      >
        ← 관리자 홈
      </Link>
      <h1 className="mb-6 text-2xl md:text-3xl font-bold tracking-tight">
        <span className="text-[#2AC1BC]">역</span>{' '}
        <span className="text-gray-800 dark:text-gray-100">관리</span>
      </h1>

      <section className="mb-6 rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        <h2 className="mb-3 text-sm font-bold uppercase text-gray-400 dark:text-gray-500">
          새 역 등록
        </h2>
        <form onSubmit={handleCreate} className="space-y-3">
          <FormFields
            value={createForm}
            onChange={setCreateForm}
            disabled={creating}
            idPrefix="create"
          />
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
        <p className="mt-3 text-xs text-gray-400 dark:text-gray-500">
          좌표 범위: 위도 {LAT_MIN}~{LAT_MAX}, 경도 {LNG_MIN}~{LNG_MAX} (한반도)
        </p>
      </section>

      <section className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800">
        <h2 className="mb-3 text-sm font-bold uppercase text-gray-400 dark:text-gray-500">
          역 목록 {stations.length > 0 && `(${stations.length})`}
        </h2>
        {loading && (
          <p className="text-sm text-gray-400 dark:text-gray-500">불러오는 중...</p>
        )}
        {listError && (
          <p className="text-sm font-bold text-red-600 dark:text-red-400">{listError}</p>
        )}
        {!loading && !listError && stations.length === 0 && (
          <p className="text-sm text-gray-400 dark:text-gray-500">등록된 역이 없습니다.</p>
        )}
        {!loading && !listError && stations.length > 0 && (
          <ul className="divide-y divide-gray-100 dark:divide-gray-700">
            {stations.map((station) =>
              editingId === station.id ? (
                <li key={station.id} className="space-y-3 py-4">
                  <FormFields
                    value={editForm}
                    onChange={setEditForm}
                    disabled={saving}
                    idPrefix={`edit-${station.id}`}
                  />
                  {editError && (
                    <p className="text-sm font-bold text-red-600 dark:text-red-400">{editError}</p>
                  )}
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => handleUpdate(station.id)}
                      disabled={saving}
                      className="flex-1 rounded-2xl bg-[#2AC1BC] py-2 text-sm font-bold text-white shadow-sm transition hover:opacity-90 disabled:opacity-50"
                    >
                      {saving ? '저장 중...' : '저장'}
                    </button>
                    <button
                      type="button"
                      onClick={cancelEdit}
                      disabled={saving}
                      className="flex-1 rounded-2xl border border-gray-200 py-2 text-sm font-bold text-gray-600 transition hover:bg-gray-50 disabled:opacity-50 dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-700"
                    >
                      취소
                    </button>
                  </div>
                </li>
              ) : (
                <li key={station.id} className="flex items-center justify-between py-3">
                  <div>
                    <p className="text-sm font-bold text-gray-800 dark:text-gray-100">
                      {station.name}
                    </p>
                    <p className="text-xs text-gray-400 dark:text-gray-500">
                      {station.latitude}, {station.longitude}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => startEdit(station)}
                    className="rounded-xl border border-gray-200 px-3 py-1 text-xs font-bold text-gray-600 transition hover:border-[#2AC1BC] hover:text-[#2AC1BC] dark:border-gray-600 dark:text-gray-300"
                  >
                    수정
                  </button>
                </li>
              )
            )}
          </ul>
        )}
      </section>
    </main>
  );
}

function FormFields({
  value,
  onChange,
  disabled,
  idPrefix,
}: {
  value: FormState;
  onChange: (v: FormState) => void;
  disabled: boolean;
  idPrefix: string;
}) {
  return (
    <div className="space-y-3">
      <div>
        <label
          htmlFor={`${idPrefix}-name`}
          className="mb-1 block text-xs font-bold text-gray-500 dark:text-gray-400"
        >
          역 이름
        </label>
        <input
          id={`${idPrefix}-name`}
          type="text"
          maxLength={50}
          value={value.name}
          onChange={(e) => onChange({ ...value, name: e.target.value })}
          disabled={disabled}
          className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm text-gray-800 outline-none transition focus:border-[#2AC1BC] disabled:opacity-50 dark:border-gray-600 dark:bg-gray-900 dark:text-gray-100"
          required
        />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label
            htmlFor={`${idPrefix}-lat`}
            className="mb-1 block text-xs font-bold text-gray-500 dark:text-gray-400"
          >
            위도
          </label>
          <input
            id={`${idPrefix}-lat`}
            type="number"
            step="0.0000001"
            value={value.latitude}
            onChange={(e) => onChange({ ...value, latitude: e.target.value })}
            disabled={disabled}
            className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm text-gray-800 outline-none transition focus:border-[#2AC1BC] disabled:opacity-50 dark:border-gray-600 dark:bg-gray-900 dark:text-gray-100"
            required
          />
        </div>
        <div>
          <label
            htmlFor={`${idPrefix}-lng`}
            className="mb-1 block text-xs font-bold text-gray-500 dark:text-gray-400"
          >
            경도
          </label>
          <input
            id={`${idPrefix}-lng`}
            type="number"
            step="0.0000001"
            value={value.longitude}
            onChange={(e) => onChange({ ...value, longitude: e.target.value })}
            disabled={disabled}
            className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm text-gray-800 outline-none transition focus:border-[#2AC1BC] disabled:opacity-50 dark:border-gray-600 dark:bg-gray-900 dark:text-gray-100"
            required
          />
        </div>
      </div>
    </div>
  );
}

type ParseResult =
  | { ok: true; value: StationRequest }
  | { ok: false; message: string };

function parseForm(form: FormState): ParseResult {
  const name = form.name.trim();
  if (!name) return { ok: false, message: '역 이름을 입력해주세요.' };
  if (name.length > 50) return { ok: false, message: '역 이름은 50자 이내여야 합니다.' };

  const latitude = Number(form.latitude);
  const longitude = Number(form.longitude);
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    return { ok: false, message: '좌표는 숫자여야 합니다.' };
  }
  if (latitude < LAT_MIN || latitude > LAT_MAX) {
    return { ok: false, message: `위도는 ${LAT_MIN}~${LAT_MAX} 사이여야 합니다.` };
  }
  if (longitude < LNG_MIN || longitude > LNG_MAX) {
    return { ok: false, message: `경도는 ${LNG_MIN}~${LNG_MAX} 사이여야 합니다.` };
  }
  return { ok: true, value: { name, latitude, longitude } };
}

function toErrorMessage(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.errorCode === 'STATION_NAME_DUPLICATE') return '이미 등록된 역 이름입니다.';
    if (e.errorCode === 'STATION_COORDINATE_OUT_OF_RANGE') return '좌표가 한반도 범위를 벗어났습니다.';
    if (e.errorCode === 'AUTH_FORBIDDEN_ADMIN_ONLY') return '관리자 권한이 필요합니다.';
    return `[${e.status}] ${e.errorCode}: ${e.message}`;
  }
  if (e instanceof Error) return e.message;
  return '알 수 없는 오류가 발생했습니다.';
}
