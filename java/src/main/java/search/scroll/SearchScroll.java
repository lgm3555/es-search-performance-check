package search.scroll;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;

public class SearchScroll {

    private static final String INDEX_NAME = "index";
    private static final TimeValue SCROLL_TIME = TimeValue.timeValueMinutes(1);
    private static final int PAGE_SIZE = 10000;

    public static void main(String[] args) {
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);

        try {
            executeScrollQuery(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void executeScrollQuery(RestHighLevelClient client) throws IOException {
        Scroll scroll = new Scroll(SCROLL_TIME);

        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.scroll(scroll);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //searchSourceBuilder.query(QueryBuilders.matchQuery("productName", "화이트보드"));
        searchSourceBuilder.size(PAGE_SIZE);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        writeFile(searchResponse.getHits());

        String scrollId = searchResponse.getScrollId();
        
        try {
            while (true) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(SCROLL_TIME);

                SearchResponse scrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
                if (!writeFile(scrollResponse.getHits())) {
                    break;
                }

                scrollId = scrollResponse.getScrollId();
            }
        } finally {
            // clear the scroll
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        }
    }

    public static boolean writeFile(SearchHits hits) {
        SearchHit[] searchHits = hits.getHits();

        if (searchHits == null || searchHits.length == 0) {
            return false;
        }

        // process the search hits
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            //TODO 처리 (파일 덤프)
        }

        return true;
    }

}