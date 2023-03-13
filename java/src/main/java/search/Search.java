package search;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class Search {

    private static final String HOST = "localhost";
    private static final int PORT = 9200;
    private static final String INDEX_NAME = "market";
    private static final int MAX = 15;

    public static void main(String[] args) {
        HttpHost http = new HttpHost(HOST, PORT, "http");
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(http));

        try {
            // Scroll API
            //ScrollSearch scroll = new ScrollSearch();
            //scroll.executeScrollQuery(client, INDEX_NAME);

            // Scroll API + Slice
            //ScrollSliceSearch scrollSlice = new ScrollSliceSearch();
            //scrollSlice.executeScrollSliceQuery(client, INDEX_NAME, 0, MAX);

            // Search After
            //AfterSearch afterSearch = new AfterSearch();
            //afterSearch.executeAfterQuery(client, INDEX_NAME);

            // Search After + PIT

            // Search After + PIT + Slice


        } catch (Exception e) {

        }
    }
}
