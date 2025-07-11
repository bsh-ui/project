<h1 align="center" style="font-size: 3em;">🎵 ListenIt MAGAZINE</h1>
<h3 align="center"><em>Vol.1 - The Sound of Tomorrow</em></h3>

<p align="center">
  <img src="https://github.com/bsh-ui/project/blob/Listenlt/images/ListenIt%20Cover.png?raw=true" alt="ListenIt Cover" width="300px" height="450px" >
</p>

<p align="center">
  <strong>React + Spring Boot 기반 음악 스트리밍 & 검색 플랫폼</strong><br>
  <strong>개발 기간:</strong> 2025.06.02 ~ 2025.06.10 &nbsp;|&nbsp;
  <strong>Repository:</strong> <a href="https://github.com/bsh-ui/project">GitHub 링크</a>
</p>

---

## 🌟 Quick Summary for Reviewers (📌 핵심 요약)

| 구분 | 내용 |
|------|------|
| 🎯 **목표** | 개인화된 음악 스트리밍 + 검색 플랫폼 구현 |
| 🛠 **기술 스택** | Java 17, Spring Boot, React 18, MySQL, Apache Solr, JWT, OAuth2 |
| 🔐 **인증** | 폼 로그인 + 소셜 로그인(Google, Naver, Kakao), JWT, 계정 잠금 |
| 🎵 **기능** | 음악 업로드/재생, 플레이리스트 관리, Solr 검색 |
| 💬 **커뮤니티** | 게시판/댓글/좋아요/평점 |
| 📩 **보안기능** | 이메일 기반 비밀번호 재설정 기능 |
| 🧪 **개발 편의** | 테스트 계정, 활동 기록 저장 기능 |

---

## 💡 프로젝트 개요

<p align="justify">
<strong>ListenIt</strong>은 React 프론트엔드와 Spring Boot 백엔드의 완벽한 통합을 통해 구현한 음악 스트리밍 플랫폼입니다.<br>
Apache Solr 기반 고속 검색, 사용자 인증 보안, 커뮤니티 기능을 포함한 풀스택 서비스를 구현하였으며, 안정성 및 사용자 경험 향상에 중점을 두었습니다.
</p>

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
<img src="https://img.shields.io/badge/ApacheSolr-9.8.1-orange" />
<img src="https://img.shields.io/badge/STS-Eclipse-green" />
<img src="https://img.shields.io/badge/VisualStudioCode-blue" />
<img src="https://img.shields.io/badge/GitHub-VersionControl-black" />
</td>
</tr>
</table>

---

## 📌 주요 기능 상세

### 🔗 React-Spring 연동 및 인증

- React ↔ Spring Boot RESTful API 연동
- JWT 기반 인증 및 토큰 저장
- 폼 로그인 + OAuth2(Google, Naver, Kakao)
- 로그인 실패 5회 시 계정 잠금
- 비밀번호 유효성 검사 및 중복 체크

### 📩 비밀번호 재설정 이메일

- 사용자가 이메일을 통해 비밀번호 재설정 링크 수신
- 이메일 인증 후 새 비밀번호 입력 가능
- Spring Mail 기반 구현

### 🎵 음악 콘텐츠 및 재생

- MP3 업로드 + 메타데이터 저장 (제목, 아티스트, 앨범 등)
- 웹 스트리밍 방식으로 끊김 없는 재생
- 음악 상세 페이지에서 플레이리스트에 추가 가능

### ⏯ 플레이리스트 기능

- 새 플레이리스트 생성
- 음악 추가/삭제 및 이름 변경
- 플레이리스트 상세 보기 지원

### 🔍 Apache Solr 검색 기능

- 음악 제목, 아티스트, 가사, 앨범 이미지까지 검색
- 색인 자동화 및 고속 Full-text 검색

### 🗣️ 커뮤니티 기능

- 게시판 CRUD, 댓글 기능
- 좋아요/싫어요, 별점 평가

### 📢 공지사항 기능 (관리자)

- 공지사항 등록/수정/삭제 (관리자 전용)

### 🧪 개발 편의 기능

- 테스트용 자동 로그인 계정 제공
- 로그인/재생 활동 로그 저장 및 조회

---

## 👨‍💻 나의 역할

- 전체 프로젝트 기획 및 백엔드 설계
- 사용자 인증 시스템 (JWT, OAuth2) 구축
- 비밀번호 재설정 이메일 처리 로직 개발
- 음악 업로드/재생 및 스트리밍 시스템 구현
- Apache Solr 연동 및 검색 API 구축
- 플레이리스트 및 커뮤니티 기능 개발
- GitHub 버전 관리 및 README 문서 작성

---

## 💭 프로젝트 회고

<p align="justify">
ListenIt 프로젝트를 통해 풀스택 개발의 흐름을 직접 주도하며<br>
React 프론트엔드와 Spring Boot 백엔드 간의 완전한 통신 구조를 설계하고 보안/검색/계정관리까지 전반적인 기능을 안정적으로 구현했습니다.<br><br>
특히 OAuth2 로그인, JWT 인증, Solr 검색 엔진 연동, 이메일 기반 비밀번호 찾기 등의 실무 중심 기술을 직접 다뤄보며<br>
서비스 기획력과 시스템 설계 능력을 동시에 성장시킬 수 있었습니다.
</p>

---

## 📸 주요 화면 

<table>
<tr>
<td align="center">
<strong>🔐 로그인 / 계정 잠금</strong><br>
<img src="https://github.com/bsh-ui/project/blob/Listenlt/images/login_lock.png?raw=true" width="400">
</td>
<td align="center">
<strong>🎶 음악 상세 / 플레이리스트 추가</strong><br>
<img src="https://github.com/bsh-ui/project/blob/Listenlt/images/music_detail.png?raw=true" width="400">
</td>
  <td align="center">
<strong>📩 solr 검색 화면</strong><br>
<img src="https://github.com/bsh-ui/project/blob/Listenlt/images/검색raw=true" width="400">
</td>
</tr>
<tr>
<td align="center">
<strong>🎨 메인 페이지</strong><br>
<img src="https://github.com/bsh-ui/project/blob/Listenlt/images/main_page.png?raw=true" width="400">
</td>
<td align="center">
<strong>🗣 커뮤니티 게시판</strong><br>
<img src="https://github.com/bsh-ui/project/blob/Listenlt/images/board_page.png?raw=true" width="400">
</td>
<td align="center">
<strong>📩 비밀번호 재설정 이메일</strong><br>
<img src="https://github.com/bsh-ui/project/blob/Listenlt/images/password_reset_email.png?raw=true" width="400">
</td>
</tr>
</table>

---

<p align="center">
  ⓒ 2025 ListenIt Project by <strong>박 성 훈</strong> &nbsp;|&nbsp; Powered by Java ☕ + Spring 🌿 + React ⚛️<br>
  <em>“당신의 음악, 당신의 방식으로.”</em>
</p>
