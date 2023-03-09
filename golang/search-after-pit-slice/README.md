# search-after + pit + slice

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