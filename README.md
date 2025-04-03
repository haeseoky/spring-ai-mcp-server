# Spring AI MCP Server

스프링부트와 AI를 활용한 엑셀, PPT 생성 서버입니다.

## 주요 기능

- AI를 활용한 엑셀 문서 자동 생성
- AI를 활용한 PPT 프레젠테이션 자동 생성
- RESTful API를 통한 비동기 문서 생성 요청 처리

## 기술 스택

- Java 17
- Spring Boot 3.2.3
- Spring AI 0.8.0 (OpenAI 활용)
- Apache POI 5.2.5 (엑셀, PPT 파일 생성)

## 요구 사항

- Java 17 이상
- OpenAI API 키

## 실행 방법

1. 환경 변수 설정: `OPENAI_API_KEY=your_api_key`
2. 애플리케이션 빌드:
   ```bash
   ./gradlew clean build
   ```
3. 애플리케이션 실행:
   ```bash
   java -jar build/libs/spring-ai-mcp-server-0.0.1-SNAPSHOT.jar
   ```

## API 사용 예시

### 1. 엑셀 문서 생성 요청

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "2024년 분기별 매출 보고서",
    "content": "2024년 1분기에서 3분기까지의 매출 데이터를 포함한 보고서를 생성합니다. 제품 카테고리별 매출과 총합을 표시하고, 분기별 증감률도 표시해주세요.",
    "documentType": "EXCEL",
    "sections": ["매출 개요", "제품별 실적", "지역별 실적"]
  }'
```

### 2. PowerPoint 프레젠테이션 생성 요청

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "AI 기술 동향 2025",
    "content": "2025년 현재의 AI 기술 동향을 소개하는 프레젠테이션입니다. 생성형 AI 발전, 대규모 언어 모델, AI 윤리 및 규제, 산업별 AI 응용 사례 등을 다룹니다.",
    "documentType": "POWERPOINT",
    "sections": ["서론", "생성형 AI 동향", "대규모 언어 모델 발전", "산업별 AI 응용", "윤리 및 규제", "결론"]
  }'
```

### 3. 문서 생성 상태 확인

```bash
curl -X GET http://localhost:8080/api/documents/{documentId}
```

### 4. 생성된 문서 다운로드

- 엑셀 파일:
  ```bash
  curl -X GET http://localhost:8080/api/documents/excel/download/{fileName}
  ```

- PowerPoint 파일:
  ```bash
  curl -X GET http://localhost:8080/api/documents/ppt/download/{fileName}
  ```

## 설정 옵션

`application.yml`에서 다양한 설정을 조정할 수 있습니다:

```yaml
app:
  document:
    temp-dir: ${java.io.tmpdir}/spring-ai-mcp-server  # 임시 파일 저장 경로
```

## 향후 개발 계획

- Markdown 문서 생성 기능 추가
- Confluence 페이지 자동 생성 기능 추가
- 다양한 문서 템플릿 지원
- 이미지 및 차트 자동 생성 기능

## 라이센스

MIT License
