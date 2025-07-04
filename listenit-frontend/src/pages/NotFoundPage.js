// src/pages/NotFoundPage.js
import React from 'react';
import { useNavigate } from 'react-router-dom';
import './NotFoundPage.css'; // 404 페이지 스타일 (곧 생성될 예정)

function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <div className="not-found-container">
      <h1 className="not-found-title">404</h1>
      <p className="not-found-message">페이지를 찾을 수 없습니다.</p>
      <button className="go-home-button" onClick={() => navigate('/')}>
        홈으로 돌아가기
      </button>
    </div>
  );
}

export default NotFoundPage;
