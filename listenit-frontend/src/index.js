import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './styles/index.css'; // 전역 CSS 또는 기본 스타일 (필요시)
import reportWebVitals from './reportWebVitals'; // 웹 성능 측정을 위한 import (선택 사항)

// React 앱을 마운트할 DOM 요소를 찾습니다.
// 이 ID는 public/index.html 또는 Spring Boot가 서빙하는 HTML 파일에 존재해야 합니다.
const container = document.getElementById('root');

if (!container) {
  // 요소를 찾지 못했을 때 콘솔에 오류 메시지 출력
  console.error('React 앱을 마운트할 #react-board-root 요소를 찾을 수 없습니다. HTML 파일에 <div id="react-board-root"></div>가 있는지 확인하세요.');
} else {
  const root = ReactDOM.createRoot(container);

  root.render(
    <React.StrictMode>
      {/* React Router 사용을 위해 App 컴포넌트를 BrowserRouter로 감싸야 합니다.
        Spring Boot가 React 앱의 index.html을 특정 경로(예: /main)에서 서빙한다면,
        BrowserRouter의 basename 속성을 해당 경로로 설정해야 합니다.
        예: <BrowserRouter basename="/main">
        현재 App.js가 React Router를 사용한다면, 이 설정이 필요합니다.
      */}
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </React.StrictMode>
  );
}

// 웹 성능 측정을 위한 함수 호출 (선택 사항)
reportWebVitals();
