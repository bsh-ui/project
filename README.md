<h1 align="center" style="font-size: 3em;">🎵 ListenIt</h1>
<h3 align="center"><em>당신의 개인화된 음악 웹 서비스</em></h3>

<p align="center">
  <img src="https://github.com/bsh-ui/project/blob/Listenlt/images/ListenIt%20Cover.png?raw=true" alt="ListenIt Cover" width="300px" height="450px" >
</p>

<p align="center">
  <strong>React + Spring Boot 기반 음악 스트리밍 & 검색 플랫폼</strong><br>
  <strong>개발 기간:</strong> 2025.06.02 ~ 2025.06.10 &nbsp;|&nbsp;
  <strong>Repository:</strong> <a href="https://github.com/bsh-ui/project">GitHub 링크</a>
</p>

<hr>

## 💡 프로젝트 개요

<p align="justify">
<strong>ListenIt</strong>은 개인화된 음악 콘텐츠 재생, 사용자 인증 및 계정 관리를 제공하는 강력한 웹 서비스입니다.<br>
React 프론트엔드와 Spring Boot 백엔드의 완벽한 통합으로 풍부하고 직관적인 사용자 경험을 선사합니다.<br>
Apache Solr 기반의 효율적인 음악 검색과 철저한 계정 보안 및 관리 기능을 통해 안정적인 서비스 운영을 목표로 합니다.
</p>

<table width="100%" cellspacing="0" cellpadding="10">
<tr>
<td width="50%" valign="top">

### 🎯 주요 개발 목표

- 사용자 인증 및 계정 관리 백엔드 구축
- Apache Solr 기반 음악 검색 기능 고도화
- 소셜 로그인 및 보안 시스템 통합
- React-Backend 안정적인 통신 설계

</td>
<td width="50%" valign="top">

### 🚀 핵심 기능 요약

- 폼 로그인 + OAuth2(Google, Naver, Kakao)
- 음악 업로드 + 스트리밍
- 플레이리스트 생성/관리
- Solr 검색 (가사/앨범 이미지 포함)
- 커뮤니티 기능 (게시글/댓글/좋아요 등)
- 관리자 공지사항 CRUD

</td>
</tr>
</table>

---

## 🛠 기술 스택

<table>
<tr>
<td><strong>📦 Backend</strong></td>
<td>
<img src="https://img.shields.io/badge/Java-17-blue" />
<img src="https://img.shields.io/badge/SpringBoot-2.7.13-green" />
<img src="https://img.shields.io/badge/MyBatis-2.3.1-orange" />
<img src="https://img.shields.io/badge/JPA-Hibernate-blue" />
<img src="https://img.shields.io/badge/SpringDataJPA-2.7.x-important" />
<img src="https://img.shields.io/badge/JWT-0.11.5-yellow" />
<img src="https://img.shields.io/badge/Lombok-%23FFA500.svg" />
<img src="https://img.shields.io/badge/Tomcat-9.0-blue" />
</td>
</tr>
<tr>
<td><strong>🎨 Frontend</strong></td>
<td>
<img src="https://img.shields.io/badge/React-18-blue" />
<img src="https://img.shields.io/badge/JavaScript-ES6-yellow" />
<img src="https://img.shields.io/badge/jQuery-3.6.0-blue" />
<img src="https://img.shields.io/badge/CSS3-%231572B6.svg" />
<img src="https://img.shields.io/badge/HTML5-%23E34F26.svg" />
</td>
</tr>
<tr>
<td><strong>🔐 Auth & API</strong></td>
<td>
<img src="https://img.shields.io/badge/OAuth2-Naver-green" />
<img src="https://img.shields.io/badge/OAuth2-Kakao-yellow" />
<img src="https://img.shields.io/badge/OAuth2-Google-blue" />
<img src="https://img.shields.io/badge/RESTful-API-red" />
<img src="https://img.shields.io/badge/Ajax-%230078D4.svg" />
</td>
</tr>
<tr>
<td><strong>🗃 DB & Tools</strong></td>
<td>
<img src="https://img.shields.io/badge/MySQL-8.0-blue" />
<img src="https://img.shields.io/badge/STS-Eclipse-green" />
<img src="https://img.shields.io/badge/VisualStudioCode-blue" />
<img src="https://img.shields.io/badge/GitHub-VersionControl-black" />
</td>
</tr>
</table>

---

## 📌 주요 기능 상세

### 🔗 React-Spring 연동

<p align="justify">
React 프론트엔드와 Spring Boot 백엔드를 RESTful API로 연동하여, 로그인 요청 시 백엔드 인증 API와 통신하고, JWT 토큰을 localStorage에 저장하여 세션을 유지합니다.
</p>

---

### 🔐 사용자 인증 및 계정 관리

- 폼 로그인 및 소셜 로그인 연동 (Google, Kakao, Naver)
- 인증 실패 시 메시지 처리 및 자동 잠금 (5회 실패 시)
- 회원가입 시 비밀번호 유효성 검사 + 중복 체크
- 프로필 조회 및 수정, 비밀번호 변경, 회원 탈퇴
- 로그인/음악 재생 기록 저장 및 조회

---

### 🎵 음악 콘텐츠 및 재생

- MP3 업로드 및 메타데이터(title, artist, album) 저장
- 웹 스트리밍 방식으로 음악 재생 (보안 문제 해결)
- 음악 상세 페이지에서 플레이리스트에 추가 가능

---

### ⏯ 플레이리스트 기능

- 새로운 플레이리스트 생성
- 음악 추가/삭제, 이름 변경, 상세 목록 관리

---

### 🔍 Solr 검색 기능

- Apache Solr 9.8.1 설치 및 음악 정보 색인
- 가사 + 앨범 이미지까지 검색 포함
- 빠르고 정밀한 전문 검색(Full-text)

---

### 🗣️ 커뮤니티 기능

- 게시글 생성/수정/삭제/조회 (CRUD)
- 댓글 작성, 좋아요/싫어요, 평점 기능 구현

---

### 📢 공지사항 기능

- 관리자 전용 공지사항 CRUD 구현

---

### 🧪 개발 및 테스트 환경

- 로그인 없이 메인에 접근 가능한 테스트 계정 지원

---

## 👨‍💻 나의 역할

- React 프론트와 Spring Boot 백엔드 연동
- JWT 기반 로그인 처리 및 OAuth2 소셜 로그인 구현
- 사용자 인증 및 프로필 관리 시스템 개발
- 음악 업로드 + 스트리밍 기능 완성
- Solr 설치 및 색인 처리 + 검색 API 구현
- 플레이리스트 생성/관리 + UI 구성
- 커뮤니티 기능 개발 (게시판, 댓글, 평점)

---

## 💭 프로젝트 회고

<p align="justify">
이번 ListenIt 프로젝트를 통해 단독으로 백엔드부터 프론트와의 연동, 보안 처리, 검색 최적화, UI 구성까지 풀스택 역량을 종합적으로 다룰 수 있었습니다.<br><br>
특히 OAuth2 로그인 통합과 JWT 인증 흐름 구현은 실무에서의 사용자 인증 시스템 이해도를 크게 높이는 계기가 되었고, Apache Solr를 통한 검색 최적화는 새로운 기술 도입 및 연동 경험을 넓혀주었습니다.<br><br>
단순한 기능 구현을 넘어 사용자 경험과 시스템 안정성을 고려한 설계 역량을 키울 수 있었던 값진 프로젝트였습니다.
</p>

---

## 📸 주요 화면 미리보기

<table>
<tr>
<td align="center">
<strong>🔐 로그인 및 계정 잠금 화면</strong><br>
<img src="[이미지 URL]" width="400">
</td>
<td align="center">
<strong>🎶 음악 상세 & 플레이리스트 추가</strong><br>
<img src="[이미지 URL]" width="400">
</td>
</tr>
<tr>
<td align="center">
<strong>🎨 메인 페이지 (React 연동)</strong><br>
<img src="[이미지 URL]" width="400">
</td>
<td align="center">
<strong>🗣 커뮤니티 게시판</strong><br>
<img src="[이미지 URL]" width="400">
</td>
  <td align="center">
<strong>📩 비밀번호 재설정 이메일</strong><br>
<img src="[이미지 URL]" width="400">
</td>

</tr>
</table>

---

<p align="center">
  ⓒ 2025 ListenIt Project by [박 성 훈] &nbsp;|&nbsp; Powered by Java ☕ + Spring 🌿 + React ⚛️<br>
  <em>“당신의 음악, 당신의 방식으로.”</em>
</p>
