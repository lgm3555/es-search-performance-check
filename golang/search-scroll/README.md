# search-scroll

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