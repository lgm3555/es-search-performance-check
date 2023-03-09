# search-after

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