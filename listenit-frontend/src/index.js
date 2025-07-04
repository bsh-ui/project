    // src/index.js
    import React from 'react';
    import ReactDOM from 'react-dom/client';
    import { BrowserRouter } from 'react-router-dom';
    import App from './App';
    import './index.css'; // 전역 CSS 또는 기본 스타일 (필요시)

    // React 앱을 마운트할 DOM 요소를 찾습니다.
    // 이 ID는 main.html (또는 Spring Boot가 서빙하는 해당 HTML 파일)에 존재해야 합니다.
    const container = document.getElementById('react-board-root'); // ⭐ 이 ID 확인 ⭐

    if (!container) {
      console.error('React 앱을 마운트할 #react-board-root 요소를 찾을 수 없습니다.');
    } else {
      const root = ReactDOM.createRoot(container);

      root.render(
        <React.StrictMode>
          <BrowserRouter> {/* React Router 사용을 위해 App을 감싸야 함 */}
            <App />
          </BrowserRouter>
        </React.StrictMode>
      );
    }
    
//     // src/index.js
// import React from 'react';
// import ReactDOM from 'react-dom/client';
// import './index.css';
// import App from './App';
// import reportWebVitals from './reportWebVitals';
// import { BrowserRouter } from 'react-router-dom';
// const container = document.getElementById('react-board-root');
// const root = ReactDOM.createRoot(document.getElementById('react-board-root'));
// root.render(
//   <React.StrictMode>
//     {/*
//       BrowserRouter의 basename을 '/main'으로 설정합니다.
//       이는 Spring Boot가 React 앱의 엔트리 포인트 HTML (index.html)을
//       /main 경로에서 서빙하기 때문입니다.
//       이렇게 설정하면 React Router는 /main을 자신의 루트로 간주하고,
//       내부의 모든 경로는 /main 뒤에 붙게 됩니다.
//       예: App.js의 <Route path="/" />는 /main과 매치되고,
//           <Route path="/posts/:id" />는 /main/posts/:id와 매치됩니다.
//     */}
//     <BrowserRouter basename="/main">
//       <App />
//     </BrowserRouter>
//   </React.StrictMode>
// );

// reportWebVitals();
// src/index.js
// import React from 'react';
// import ReactDOM from 'react-dom/client';
// import './index.css';
// import App from './App';
// import reportWebVitals from './reportWebVitals';
// import { BrowserRouter } from 'react-router-dom';

// // React 앱을 마운트할 DOM 요소를 찾습니다.
// // public/index.html 파일의 <div id="root">와 정확히 일치해야 합니다.
// const container = document.getElementById('root'); // ⭐ ID를 'root'로 설정합니다. ⭐

// if (!container) {
//   // 요소를 찾지 못했을 때 콘솔에 오류 메시지 출력
//   console.error('React 앱을 마운트할 #root 요소를 찾을 수 없습니다. public/index.html에 <div id="root"></div>가 있는지 확인하세요.');
// } else {
//   const root = ReactDOM.createRoot(container); // 유효한 DOM 요소를 전달

//   root.render(
//     <React.StrictMode>
//       <BrowserRouter basename="/main"> {/* Spring Boot의 기본 경로에 맞춰 basename 설정 */}
//         <App />
//       </BrowserRouter>
//     </React.StrictMode>
//   );
// }

// reportWebVitals();
