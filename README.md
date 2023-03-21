## es-search-performance-check


Java, Golang을 사용하여 Elasticsearch Pagination Search Example

- Scroll API

---

scroll API는 검색 결과를 묶어서 한 번에 가져오면서, 검색 결과를 스크롤 하는 기능을 제공합니다.

이 방식은 검색 결과를 기억하고 있어야 해서 메모리 부하가 생길 수 있습니다.

```
1. 첫 조회
POST index/_search?scroll=1m
{
    "query": {
        "match_all": {}
    }
}

2. id 조회
POST /_search/scroll
{
    "scroll": "1m",
    "scroll_id": "FGluY2x1ZGVfY29udGV4dF...."
}
```

- Scroll API + slice

---

slice는 Scroll의 병렬 처리라고 생각하시면 됩니다.

Scroll Search와 PIT 에서만 사용 가능하며 `인덱스의 샤드와 동일한 수의 슬라이스를 선택해야 성능이 좋습니다.`

Scroll Search와 PIT에서만 사용 가능, `[slice]` can only be used with `[scroll]` or `[point-in-time]` requests

```
1. 첫 조회
POST index/_search?scroll=1m
{
    "query": {
        "match_all": {}
    },
    "slice": {
        "id": 0,
        "max": 2
    }
}
POST index/_search?scroll=1m
{
    "query": {
        "match_all": {}
    },
    "slice": {
        "id": 1,
        "max": 2
    }
}

2. id 조회
POST /_search/scroll
{
    "scroll": "1m",
    "scroll_id": "FGluY2x1ZGVfY29udGV4dF...."
}
2-1. slice id 조회
POST /_search/scroll
{
    "scroll": "1m",
    "scroll_id": "FGluY2x1ZGVfY29udGV4dF...."
}
```

- Search After

---

scroll API는 검색 결과를 묶어서 한 번에 가져오면서, 검색 결과를 스크롤 하는 기능을 제공합니다.

이 방식은 검색 결과를 기억하고 있어야 해서 메모리 부하가 생길 수 있습니다.

Search After는 Scroll API 방식과 달리 live coursor를 제공하면서 stateless이기 때문에 메모리 부하가 적습니다.

위와 같은 이유로 ES 공식 문서에서도 Scroll API 보다 Search After 사용 권장을 하고 있습니다.

`정렬이 필요 없는 경우 _doc으로 정렬 시 성능이 가장 빠릅니다.`


```
1. 첫 조회
GET index/_search
{
    "size": 10000,
    "sort": ["_doc"]
}

2. 마지막 sort 값으로 조회
GET index/_search
{
    "size": 10000,
    "search_after": [9999],
    "sort": ["_doc"]
}
```

- Search After + PIT

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

- Search After + PIT + slice

---

slice는 Scroll의 병렬 처리라고 생각하시면 됩니다.

Scroll Search와 PIT 에서만 사용 가능하며 `인덱스의 샤드와 동일한 수의 슬라이스를 선택해야 성능이 좋습니다.`

Scroll Search와 PIT에서만 사용 가능, `[slice]` can only be used with `[scroll]` or `[point-in-time]` requests

```
1. search-after + pit + slice 적용
GET _search
{
  "size": 10000,
  "slice": {
    "id": 0,
    "max": 10
  },
  "pit": {
    "id" : "s9C1Aw....",
    "keep_alive": "1m"
  },
  "search_after": [9999],
  "sort": ["_doc"]
}
```
