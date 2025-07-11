<h1 align="center" style="font-size: 3em;">🎵 ListenIt MAGAZINE</h1>

<h3 align="center"><em>Vol.1 - The Sound of Tomorrow: 당신의 음악, 당신의 방식으로.</em></h3>



<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/ListenIt%20Cover.png" alt="ListenIt Cover" width="300px" height="450px" >
</p>



<p align="center">

  <strong>💡 개인화된 음악 경험과 견고한 보안을 제공하는 React + Spring Boot 기반 풀스택 음악 플랫폼</strong><br>

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

ListenIt 프로젝트는 6개월간의 개발 교육 과정 중, **음악 스트리밍 서비스에 대한 흥미와 '직접 구현해 볼 수 있을까?' 하는 궁금증**에서 시작되었습니다. 평소 즐겨 사용하던 '지니 뮤직'과 같은 플랫폼을 접하며, **서비스를 직접 설계하고 구현해보고자 하는 목표**를 세웠습니다.<br><br>



단순히 기능을 나열하기보다, **학습했던 다양한 기술 스택을 실제 프로젝트에 적용해보는 도전**을 하고 싶었습니다. 초기에는 백엔드 중심으로 **JPA, OAuth2, JWT 인증 시스템** 등을 구현하며 기능을 확장해나갔습니다. 프로젝트 중반, React를 접하면서 사용자 경험 측면에서 더 나은 선택이라고 판단했고, **프론트엔드를 React, 백엔드를 Spring Boot(STS)로 분리하여 연동하는 아키텍처**를 시도했습니다. 이에 따라 메인 과 로그인 기능을 포함한 프론트엔드 부분을 React로 다시 구현하며, 백엔드와 RESTful API로 유기적으로 연동하는 과정을 통해 **분산 시스템 설계의 실제적인 경험**을 얻을 수 있었습니다.<br><br>



**Apache Solr**의 도입은 서비스의 핵심 기능을 고도화하기 위한 선택이었습니다. 마침 교육 과정에서 Solr를 이용한 색인 및 검색 구현을 다루었을 때, 이를 개인 프로젝트에 적용해 보면 좋겠다고 생각했습니다. Solr가 다양한 파일 타입의 메타데이터를 효율적으로 색인하고 검색할 수 있다는 점에 착안하여, **방대한 음악 데이터(가사, 아티스트, 앨범, 노래)를 빠르고 정확하게 검색**하는 기능을 구현했습니다. 나아가 Solr의 확장성을 고려하여, 사용자나 아티스트 검색 시 관련 앨범, 노래, 플레이리스트까지 불러올 수 있는 가능성 또한 구상해보았습니다.<br><br>



ListenIt은 **비교적 짧은 기간 안에 개인이 주도적으로 기획하고 구현한 풀스택 프로젝트**입니다. 이 과정을 통해 저는 **새로운 기술을 학습하고 실제 프로젝트에 적용하는 주도성, 그리고 문제 해결을 위해 끊임없이 고민하고 시도하는 개발자로서의 태도**를 다질 수 있었습니다.

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



## 🚀 핵심 구현 기능: 프로젝트를 통해 만난 기술적 도전과 해결의 여정

### 1. 로그인 화면과 5회 실패시 계정 잠금

**구현기능설명**

사용자 인증 및 계정 관리: 아이디/비밀번호 폼 로그인과 Google, Kakao, Naver 소셜 로그인을 통합 구현하여 사용자 편의성을 높였습니다. 백엔드에서 JWT 토큰을 발행하고 프론트엔드(localStorage)에서 관리하는 토큰 기반 인증 방식을 통해 서버의 확장성을 확보하고 무상태(Stateless) 세션을 구현했습니다.

* **핵심 기술**: Spring Security, JWT, OAuth2 Client, React
* **주요 성과**: 사용자 친화적인 다양한 로그인 옵션 제공 및 서버 부담을 줄이는 확장성 높은 인증 시스템 구축.
* 계정 보안 관리: 무단 접근 방어를 위해 비밀번호 5회 실패 시 계정을 자동 잠금하는 기능을 구현했습니다. 또한, 관리자 권한으로 특정 계정을 비활성화하거나 정지 처리할 수 있는 기능을 제공하여 서비스의 안정성과 보안 관리 역량을 강화했습니다.

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A1%9C%EA%B7%B8%EC%9D%B8.png" alt="로그인 / 계정 잠금" width="400" />
</p>

**기능 설명 (상세):**

React에서 로그인 요청 시, Spring Boot 백엔드에서 JWT 토큰을 발급합니다. 이 토큰은 HTTP 헤더에 저장되어 API 호출 시 인증 처리에 사용됩니다. OAuth2 로그인 연동으로 구글, 네이버, 카카오 계정 인증을 지원합니다. 로그인 실패 횟수를 누적 관리하고 특정 횟수(5회) 초과 시 계정을 자동으로 잠그는 로직을 구현했습니다. 이는 무단 접근 시도를 선제적으로 방어하는 중요한 보안 장치입니다. 사용자 비밀번호는 PasswordEncoder를 활용한 솔팅(Salting) 및 반복 해싱으로 안전하게 암호화하여 저장하며, 비밀번호 유효성 검사 및 중복 체크 로직을 적용하여 처음부터 강력한 계정 생성을 유도했습니다. 폼 로그인, 소셜 로그인, JWT를 하나의 Spring Security 설정에 유기적으로 통합하는 복잡한 필터 체인 구성, HttpOnly 쿠키를 이용한 토큰 관리 보안 강화 등 세밀한 기술적 고민과 해결이 뒤따랐습니다.

---

### 2. 음악 상세페이지에서 음악 재생과 플레이리스트 및 음악 업로드

**구현기능설명**

음악 콘텐츠 관리 및 재생: 효율적인 음악 파일 관리 (관리자 전용): 관리자가 MP3 파일을 업로드할 때, 대용량 파일을 데이터베이스에 직접 저장하는 대신 서버의 특정 물리 경로에 저장하고 해당 URL만 DB에 관리하도록 설계했습니다. 이 방식은 DB 부하를 줄여 시스템 성능을 높이고, 파일 관리 유연성(수정/삭제 포함) 및 유지보수/확장을 용이하게 합니다.
* **핵심 기술**: 파일 시스템 제어, 데이터베이스 연동 전략
* **주요 성과**: 대용량 미디어 파일의 효율적이고 확장 가능한 저장 및 관리 시스템 구축.
* 안정적인 음악 스트리밍 재생: 업로드된 MP3 음악 파일을 웹 환경에서 스트리밍 방식으로 끊김 없이 재생하도록 구현했습니다. 로그인한 사용자에게만 재생 권한을 부여해 콘텐츠를 보호하며, DB에 저장된 URL/경로를 통해 물리적 파일을 안전하게 가져와 제공합니다.
* **핵심 기술**: 미디어 스트리밍 기술, 권한 처리
* **주요 성과**: 웹 보안 및 기술적 문제 해결을 통한 안정적인 사용자 음악 감상 환경 제공.
* 직관적인 플레이리스트: 개별 음악 상세 페이지에서 플레이리스트에 음악을 손쉽게 추가하거나 새로운 플레이리스트를 즉시 생성할 수 있습니다. 생성된 플레이리스트는 음악 추가/삭제, 순서 변경, 제목 변경 등 세부 관리가 가능하며, 상세 내용을 조회할 수 있습니다.
* **핵심 기술**: 백엔드 데이터 모델링, 사용자 인터랙션 UX 설계
* **주요 성과**: 사용자 취향을 반영한 개인화된 음악 관리 경험 및 직관적인 인터페이스 제공.

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%8C%EC%95%85%EC%83%81%EC%84%B8.jpg" alt="음악 상세 / 플레이리스트 추가" width="400" />
</p>

**기능 설명 (상세):**

MP3와 같은 대용량 미디어 파일을 데이터베이스에 직접 저장할 경우 발생할 수 있는 DB 성능 저하, 백업/복구의 어려움, 비용 문제를 해결하기 위해, 파일을 서버의 특정 물리적 경로에 저장하고 해당 URL/경로만을 DB에 관리하는 전략을 채택했습니다. 이는 시스템의 확장성과 유지보수성을 크게 향상시켰습니다. 웹 환경에서 음악을 끊김 없이 스트리밍하기 위해 파일 경로 문제, Content Security Policy (CSP) 위반, JavaScript 오류 등 다양한 기술적 문제를 해결했습니다. 특히, 서버에 저장된 물리적 파일 경로를 안전하게 웹에서 접근 가능하도록 설정하고, HTTP Content-Type 및 Content-Length 헤더를 정확히 설정하여 안정적인 재생 환경을 구축했습니다. 사용자가 복잡한 과정 없이 자신만의 음악 컬렉션을 만들고 관리할 수 있도록, 백엔드 데이터 모델링부터 프론트엔드 인터랙션까지 사용자 경험(UX)을 최우선으로 고려하여 직관적인 플레이리스트 기능을 설계했습니다.

---

### 3. 리액트 화면과 연동된 메인 화면

**구현기능설명**

React-Spring 통합 및 UI/UX 강화: RESTful API 기반 프론트엔드-백엔드 연동: React 기반의 UI를 Spring Boot 백엔드와 RESTful API 통신을 통해 성공적으로 연동했습니다. 특히, 로그인 요청 시 백엔드의 인증 API와 연동하여 토큰 기반 인증을 구현함으로써 서버 부담을 줄이고 서비스 확장성을 확보했습니다.
* **핵심 기술**: React, Spring Boot, RESTful API, JWT
* **주요 성과**: 프론트엔드와 백엔드의 안정적인 분리 및 유연한 통합으로 개발 효율성 및 유지보수성 향상.
* 사용자 친화적인 메인 페이지 및 동적 화면 구현: 사용자 경험을 최적화하기 위해 드롭다운 메뉴와 핵심 콘텐츠 블록을 구성했습니다. 메인 페이지뿐만 아니라 커뮤니티 게시판 (CRUD, 댓글, 평점, 좋아요/싫어요 포함) 등 복잡한 동적 인터랙션이 필요한 모든 화면을 React 컴포넌트 기반으로 설계하여 빠른 로딩 속도와 반응성을 확보했습니다.
* **핵심 기술**: React 컴포넌트, 상태 관리, RESTful API 연동
* **주요 성과**: 재사용 가능한 UI 컴포넌트 구축을 통한 개발 생산성 증대 및 사용자 친화적이고 동적인 서비스 경험 제공.

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A6%AC%EC%95%A1%ED%8A%B8%EC%97%B0%EB%8F%99%EB%90%9C%ED%99%94%EB%A9%B4.png" alt="메인 페이지" width="400" />
</p>

**기능 설명 (상세):**

React 기반의 사용자 인터페이스는 Spring Boot 백엔드와 RESTful API 통신을 통해 성공적으로 연동되었습니다. 프론트엔드에서 사용자 로그인 요청 시 백엔드의 인증 API와 연동하여 정상적인 로그인 처리 및 토큰 기반 인증이 이루어지도록 구현했습니다. 이 연동을 통해 서버의 부담을 줄이고, 무상태(Stateless) 아키텍처를 통해 서비스 확장성을 확보하면서 사용자에게 매끄러운 인증 경험을 제공합니다. 메인 페이지를 포함한 모든 사용자 인터페이스는 React 컴포넌트 기반으로 설계되어 빠른 로딩 속도와 반응성을 확보했으며, 재사용 가능한 UI 컴포넌트 구축을 통해 개발 생산성을 증대했습니다.

---

### 4. 비밀번호 보안

**구현기능설명**

강력한 비밀번호 암호화: 사용자 비밀번호는 Spring Security의 PasswordEncoder를 활용하여 데이터베이스에 복원 불가능한 암호화된 형태로 저장됩니다. 이는 각기 다른 '솔트(Salt)' 값을 자동 생성하여 비밀번호와 섞고, BCrypt 알고리즘으로 수천 번의 해싱을 반복함으로써 무지개 테이블 및 브루트 포스 공격으로부터 사용자 정보를 철저히 보호합니다.
* **핵심 기술**: Spring Security PasswordEncoder (BCrypt), Salting, One-way Hashing
* **주요 성과**: 사용자 비밀번호 유출 위험을 원천 차단하고 최고 수준의 보안을 확보.

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202025-07-09%20101921.png" alt="데이터베이스에 저장된 암호화된 비밀번호" width="700" height="400" />
  <br>
  <em>(이미지 설명: 비밀번호 암호화 처리 과정 예시)</em>
</p>

**기능 설명 (상세):**

사용자가 회원가입 시 입력한 비밀번호는 데이터베이스에 절대 평문으로 저장되지 않습니다. 대신, Spring Security의 `PasswordEncoder` 인터페이스를 활용하여 복원이 불가능한 암호화된 형태로 변환되어 저장됩니다. 이 과정에서 각기 다른 '솔트(Salt)' 값을 자동으로 생성하여 비밀번호와 섞고, `BCrypt` 알고리즘으로 수천 번의 해싱을 반복합니다. 이는 해커의 '무지개 테이블(Rainbow Table)' 공격 및 '브루트 포스(Brute Force) 공격'으로부터 사용자 정보를 철저히 보호하여, ListenIt 서비스의 보안 수준을 최고로 유지하는 핵심적인 방어 체계입니다.

---

### 5. 이메일 인증 시스템

**구현기능설명**

안전한 비밀번호 재설정: 비밀번호를 잊은 사용자를 위해 이메일 인증 기반의 재설정 기능을 구현했습니다. 사용자가 이메일을 입력하면 일회용 인증 코드를 발송하고, 코드 검증 성공 시에만 새로운 비밀번호를 설정할 수 있도록 했습니다.
* **핵심 기술**: 이메일 발송 시스템 연동, 인증 코드 생성/검증 로직
* **주요 성과**: 인증 코드의 유효 시간 제한 및 무작위성을 통해 무단 접근 시도를 방지하고, 사용자 편의성과 보안성을 모두 고려한 계정 복구 경로 제공.

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%B4%EB%A9%94%EC%9D%BC.png" alt="비밀번호 재설정 이메일" width="400" />
</p>

**기능 설명 (상세):**

비밀번호를 잊어버린 사용자를 위해 이메일 인증 기반의 재설정 기능을 구현했습니다. 이 과정은 보안에 매우 민감합니다. 사용자 이메일로 발송되는 인증 코드의 '일회성', '유효 시간 제한', 그리고 '무작위성'을 철저히 보장하는 로직을 구현하여 무단 접근 시도를 원천적으로 차단했습니다. 백엔드에서 인증 코드를 생성하고, 외부 이메일 발송 시스템(Spring Mail 등)과 안정적으로 연동하며, 사용자 입력 코드를 정확하게 검증하는 복합적인 흐름을 안정적으로 구현했습니다.

---

### 6. Solr 검색 시스템

**구현기능설명**

Solr 연동 및 고성능 검색: Apache Solr 기반 초고속 검색: 방대한 음악 콘텐츠를 효율적으로 검색하기 위해 Apache Solr (v9.8.1)를 직접 설치하고 구성했습니다. 데이터베이스의 음악 정보를 Solr에 실시간으로 색인(indexing)하여 DB 탐색 비효율을 제거하고 찰나의 순간에 검색 결과를 제공합니다.
* **핵심 기술**: Apache Solr, Full-Text Search, Indexing
* **주요 성과**: 대용량 데이터 환경에서 서비스의 검색 성능을 혁신적으로 향상시키고, 가사/앨범 이미지 등 풍부한 검색 결과를 제공하여 사용자 만족도를 높였습니다.

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%80%EC%83%89.png" alt="Solr 검색 화면" width="400" />
</p>

**기능 설명 (상세):**

관계형 데이터베이스만으로는 방대한 음악 데이터에 대한 전문 검색(Full-Text Search) 및 실시간 검색 성능에 한계가 있었습니다. 이 문제를 해결하기 위해 Apache Solr를 직접 서버에 설치하고, 데이터베이스의 음악 정보를 Solr에 실시간으로 색인(indexing)하는 시스템을 구축했습니다. 음악 제목, 아티스트, 가사, 앨범 등 여러 필드에서 동시에 검색이 가능하도록 Solr의 `edismax` 파서와 필드 가중치(`qf`)를 설정하여 사용자에게 가장 관련성 높은 검색 결과를 제공하도록 최적화했습니다. Solr의 유연한 스키마와 코어 관리를 통해, 향후 사용자 플레이리스트 검색, 아티스트 정보 검색 등으로 기능을 확장할 수 있는 기반을 마련했습니다.

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



## 📸 주요  



<table>

<tr>

<td align="center">

<strong>🔐 로그인 / 계정 잠금</strong><br>

<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A1%9C%EA%B7%B8%EC%9D%B8.png" width="400">

</td>

<td align="center">

<strong>🎶 음악 상세 / 플레이리스트 추가</strong><br>

<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%8C%EC%95%85%EC%83%81%EC%84%B8.jpg" width="400">

</td>

<td align="center">

<strong>📩 Solr 검색 </strong><br>

<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%80%EC%83%89.png" width="400">

</td>

</tr>

<tr>

<td align="center">

<strong>🎨 메인 페이지</strong><br>

<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A6%AC%EC%95%A1%ED%8A%B8%EC%97%B0%EB%8F%99%EB%90%9C%ED%99%94%EB%A9%B4.png" width="400">

</td>

<td align="center">

<strong>🗣 커뮤니티 게시판</strong><br>

<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%8C%EC%8B%9C%ED%8C%90.png" width="400">

</td>

<td align="center">

<strong>📩 비밀번호 재설정 이메일</strong><br>

<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%B4%EB%A9%94%EC%9D%BC.png" width="400">

</td>

</tr>

</table>

---
<p align="center">

  ⓒ 2025 ListenIt Project by <strong>박 성 훈</strong> &nbsp;|&nbsp; Powered by Java ☕ + Spring 🌿 + React ⚛️<br>

  <em>“당신의 음악, 당신의 방식으로.”</em>

</p>
