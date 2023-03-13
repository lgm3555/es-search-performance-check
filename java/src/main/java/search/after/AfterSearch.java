package search.after;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;

public class AfterSearch {

    private static final int PAGE_SIZE = 10000;

    public void executeAfterQuery(RestHighLevelClient client, String index) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(PAGE_SIZE);
        searchSourceBuilder.sort("_doc");

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        while (true) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits.length == 0) {
                break;
            }

            Object[] sortValues = hitDataCheck(hits);
            searchSourceBuilder.searchAfter(sortValues);
            searchRequest.source(searchSourceBuilder);

            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        }
    }

    private Object[] hitDataCheck(SearchHit[] hits) {
        System.out.println("searchHits = " + hits.length);

        for (SearchHit hit : hits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            //TODO process the search hit (write to file)
        }

        return hits[hits.length - 1].getSortValues();
    }
}
