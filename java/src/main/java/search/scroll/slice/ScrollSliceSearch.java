package search.scroll.slice;

import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.slice.SliceBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScrollSliceSearch {

    private static final int PAGE_SIZE = 1000;
    private static final TimeValue SCROLL_TIMEOUT = TimeValue.timeValueMinutes(1);

    public void executeScrollSliceQuery(RestHighLevelClient client, String index, int id, int max) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(max);

        for (int i = 0; i < max; i++) {
            int innerI = i;
            executorService.execute(() -> {
                SearchRequest searchRequest = buildSearchRequest(index, id, max);
                try {
                    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                    hitDataCheck(searchResponse.getHits(), innerI);

                    String scrollId = searchResponse.getScrollId();

                    try {
                        while (true) {
                            SearchScrollRequest scrollRequest = buildScrollRequest(scrollId);
                            SearchResponse scrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);

                            if (!hitDataCheck(scrollResponse.getHits(), innerI)) {
                                break;
                            }

                            scrollId = scrollResponse.getScrollId();
                        }
                    } finally {
                        clearScroll(client, scrollId);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
    }

    private SearchRequest buildSearchRequest(String index, int id, int max) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(PAGE_SIZE);
        searchSourceBuilder.slice(new SliceBuilder(0, 2));
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(SCROLL_TIMEOUT);
        return searchRequest;
    }

    private SearchScrollRequest buildScrollRequest(String scrollId) {
        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
        scrollRequest.scroll(SCROLL_TIMEOUT);
        return scrollRequest;
    }

    private void clearScroll(RestHighLevelClient client, String scrollId) throws IOException {
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
    }

    private boolean hitDataCheck(SearchHits hits, int i) {
        SearchHit[] searchHits = hits.getHits();

        if (searchHits == null || searchHits.length == 0) {
            return false;
        }

        System.out.println("i, searchHits = " + i + " " + searchHits.length);

        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            //TODO process the search hit (write to file)
        }

        return true;
    }
}