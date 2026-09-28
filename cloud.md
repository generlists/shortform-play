# Cloud

## 1. Scope

ShortForm Play Android에서 사용하는 Cloud 및 외부 데이터 소스의 구조와 작업 규칙을 기록한다.

소스코드로 확인할 수 있는 구현 상세는 이 문서에 중복해서 기록하지 않는다.


## 2. Cloud Services

### Firebase Storage

콘텐츠 관련 JSON 데이터의 source로 사용한다.

- 초기 콘텐츠 데이터
- 콘텐츠 데이터 갱신
- Firebase Storage에 직접 접근하는 기능이 존재할 수 있음

### FastAPI

Backend API로 사용한다.

- 사용자 인증
- 검색
- 서버에서 처리해야 하는 기능
- Android에서 직접 처리하지 않는 Backend 기능

### PostgreSQL

FastAPI Backend의 영속 데이터 저장소.

Android에서 직접 접근하지 않는다.

### Redis

FastAPI Backend의 캐시 및 임시 데이터 처리에 사용한다.

Android에서 직접 접근하지 않는다.

---

## 3. Data Source

Cloud 데이터는 하나의 source로 통일되어 있지 않다.

기능에 따라 다음을 사용한다.

```text
Firebase Storage
    └─ 콘텐츠 / JSON

FastAPI
    ├─ 인증
    ├─ 검색
    └─ Backend 처리
```

어떤 source를 사용할지는 신규 구현 전에 해당 기능의 기존 구현을 확인한다.


## 4. Android Integration

Cloud 접근은 Android의 Data Layer를 기준으로 구성한다.

일반적인 흐름:

```text
UI
 ↓
ViewModel
 ↓
Repository
 ↓
Data Source
 ├─ Firebase
 └─ FastAPI
```

실제 프로젝트의 구현 구조가 위 흐름과 다른 경우에는 현재 소스의 기존 패턴을 우선한다.


## 5. Development Rules

### 기존 구조 우선

새로운 Cloud 기능을 추가할 때 기존 Repository, DataSource, API 호출 및 Firebase 접근 방식을 먼저 확인한다.

새로운 구조를 만드는 것보다 기존 구조를 확장하는 것을 우선한다.

### Data Source 선택

기능에 따라 Firebase Storage와 FastAPI를 구분해서 사용한다.

기존에 Firebase를 사용하는 데이터를 불필요하게 API로 이전하거나, 기존 API 데이터를 Firebase로 변경하지 않는다.

### API Contract

기존 FastAPI API의 request / response contract를 임의로 변경하지 않는다.

API 변경이 필요한 경우 기존 Android 사용처와 호환성을 먼저 확인한다.

### Firebase Data

Firebase Storage의 기존 JSON 구조를 임의로 변경하지 않는다.

JSON 구조가 변경되는 경우 해당 데이터를 사용하는 모든 Android 코드를 확인한다.

### Server-side Data

PostgreSQL과 Redis는 Android에서 직접 접근하지 않는다.

필요한 데이터는 FastAPI를 통해 접근한다.

---

## 6. Exceptions / Special Cases

일반적인 Android Data Layer 구조와 다른 Cloud 연동이나 예외사항은 이 영역에 추가한다.

예:

```text
- 특정 콘텐츠 데이터는 FastAPI가 아닌 Firebase Storage를 직접 사용
- 특정 API는 일반적인 Repository 흐름과 다른 처리 필요
- 특정 데이터는 서버 DB에 저장하지 않고 Firebase source를 유지
```

실제 예외가 발견되면 해당 기능과 이유를 여기에 기록한다.

---

## 7. Change Guidelines

Cloud 관련 코드를 추가하거나 수정할 때:

1. 기존 데이터 흐름을 확인한다.
2. 기존 DataSource / Repository 구현을 확인한다.
3. 동일한 패턴으로 구현할 수 있는지 먼저 검토한다.
4. Firebase와 FastAPI 중 기존 기능에서 사용하는 source를 확인한다.
5. API 또는 Firebase 데이터 구조 변경 여부를 확인한다.
6. 구조적인 변경이나 새로운 예외가 발생하면 이 문서를 업데이트한다.

---

## 8. Documentation Principle

이 문서는 Cloud 구현 전체를 설명하는 문서가 아니다.

다음 내용은 원칙적으로 기록하지 않는다.

- 클래스별 구현 설명
- 전체 API 목록
- Firebase SDK 사용법
- AWS 인프라 상세
- PostgreSQL / Redis 내부 구조
- 코드에서 쉽게 확인할 수 있는 내용

대신 다음 내용을 기록한다.

- 프로젝트 고유의 Cloud 구조
- 일반적인 구조와 다른 예외
- Cloud 데이터 source 선택 기준
- API / Firebase 데이터 변경 시 주의사항
- 개발 과정에서 발견된 중요한 결정사항
