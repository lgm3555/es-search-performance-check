# search-after + pit

---

PIT(POINT IN TIME) Elasticsearch 7.10 버전부터 사용 가능합니다.

인덱스의 특정 시점의 데이터 상태를 캡처하여 복원할 수 있는 기능입니다.

Search After 요청 사이에 인덱스 변경사항이 일어나면 결과 데이터가 일관되지 않을 수 있어 데이터 일관성을 맞추기 위해 PIT와 같이 사용합니다.

```
1. PIT 아이디 생성
POST index/_pit?keep_alive=1m

2. PIT 적용하여 사용
GET _search
{
  "size": 10000,
  "pit": {
    "id" : "s9C1AwEGb.....",
    "keep_alive": "1m"
  }
}
```