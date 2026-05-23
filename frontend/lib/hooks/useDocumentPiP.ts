'use client';

import { useCallback, useEffect, useState } from 'react';

/**
 * Document Picture-in-Picture API 훅
 * - Chrome/Edge 116+ 지원, Safari/Firefox 미지원
 * - OS 레벨로 항상 위에 떠있는 작은 창에 임의 React 컴포넌트를 렌더링 가능
 */

interface DocumentPiPOptions {
  width?: number;
  height?: number;
}

interface DocumentPictureInPicture {
  requestWindow(options?: DocumentPiPOptions): Promise<Window>;
}

declare global {
  interface Window {
    documentPictureInPicture?: DocumentPictureInPicture;
  }
}

export function useDocumentPiP() {
  const [pipWindow, setPipWindow] = useState<Window | null>(null);
  const [supported, setSupported] = useState(false);

  useEffect(() => {
    setSupported(
      typeof window !== 'undefined' && 'documentPictureInPicture' in window
    );
  }, []);

  const open = useCallback(async (options?: DocumentPiPOptions) => {
    if (!window.documentPictureInPicture) return;
    try {
      const win = await window.documentPictureInPicture.requestWindow(options);
      // PiP 창 body 기본 스타일 초기화 (브라우저 기본 margin 제거)
      win.document.body.style.margin = '0';
      win.document.body.style.padding = '0';
      win.document.body.style.fontFamily =
        'system-ui, -apple-system, "Noto Sans KR", sans-serif';
      // 사용자가 PiP 창을 닫으면 상태 동기화
      win.addEventListener('pagehide', () => setPipWindow(null), { once: true });
      setPipWindow(win);
    } catch (e) {
      console.error('Failed to open PiP window:', e);
    }
  }, []);

  const close = useCallback(() => {
    if (pipWindow) {
      pipWindow.close();
      setPipWindow(null);
    }
  }, [pipWindow]);

  // 컴포넌트 언마운트 시 PiP 창 닫기
  useEffect(() => {
    return () => {
      pipWindow?.close();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return { pipWindow, open, close, supported };
}
