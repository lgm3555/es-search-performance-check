package search.scroll;

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

import java.io.IOException;
import java.util.Map;

public class ScrollSearch {

    private static final int PAGE_SIZE = 10000;
    private static final TimeValue SCROLL_TIMEOUT = TimeValue.timeValueMinutes(1);

    public void executeScrollQuery(RestHighLevelClient client, String index) throws IOException {
        SearchRequest searchRequest = buildSearchRequest(index);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        hitDataCheck(searchResponse.getHits());

        String scrollId = searchResponse.getScrollId();

        try {
            while (true) {
                SearchScrollRequest scrollRequest = buildScrollRequest(scrollId);
                SearchResponse scrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);

                if (!hitDataCheck(scrollResponse.getHits())) {
                    break;
                }

                scrollId = scrollResponse.getScrollId();
            }
        } finally {
            clearScroll(client, scrollId);
        }
    }

    private SearchRequest buildSearchRequest(String index) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //searchSourceBuilder.query(QueryBuilders.matchQuery("productName", "화이트보드"));
        searchSourceBuilder.size(PAGE_SIZE);
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

    private boolean hitDataCheck(SearchHits hits) {
        SearchHit[] searchHits = hits.getHits();

        if (searchHits == null || searchHits.length == 0) {
            return false;
        }

        System.out.println("searchHits = " + searchHits.length);

        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            //TODO process the search hit (write to file)
        }

        return true;
    }
}