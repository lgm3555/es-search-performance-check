package search.after.pit;

import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.PointInTimeBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;

public class AfterPitSearch {

    private static final int PAGE_SIZE = 10000;
    private static final TimeValue PIT_TIMEOUT = TimeValue.timeValueMinutes(2);

    public void executeAfterPitQuery(RestHighLevelClient client, String index) throws IOException {
        String pitId = openPitId(client, index);

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(PAGE_SIZE);
        searchSourceBuilder.sort("_doc");
        searchSourceBuilder.pointInTimeBuilder(new PointInTimeBuilder(pitId).setKeepAlive(PIT_TIMEOUT));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        while (true) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits.length == 0) {
                break;
            }

            hitDataCheck(hits);

            searchSourceBuilder.searchAfter(hits[hits.length - 1].getSortValues());
            searchRequest.source(searchSourceBuilder);

            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        }

        closePitId(client, pitId);
    }

    private String openPitId(RestHighLevelClient client, String index) throws IOException {
        OpenPointInTimeRequest openPointInTimeRequest = new OpenPointInTimeRequest(index);
        openPointInTimeRequest.keepAlive(PIT_TIMEOUT);

        OpenPointInTimeResponse openPointInTimeResponse = client.openPointInTime(openPointInTimeRequest, RequestOptions.DEFAULT);
        return openPointInTimeResponse.getPointInTimeId();
    }

    private Boolean closePitId(RestHighLevelClient client, String pitId) throws IOException {
        ClosePointInTimeRequest closeRequest = new ClosePointInTimeRequest(pitId);
        ClearScrollResponse closeResponse = client.closePointInTime(closeRequest, RequestOptions.DEFAULT);
        return closeResponse.isSucceeded();
    }

    private void hitDataCheck(SearchHit[] hits) {
        System.out.println("searchHits = " + hits.length);

        for (SearchHit hit : hits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            //TODO process the search hit (write to file)
        }
    }
}
