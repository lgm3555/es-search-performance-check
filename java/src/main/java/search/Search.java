package search;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import search.after.pit.AfterPitSearch;
import search.after.pit.slice.AfterPitSliceSearch;

public class Search {

    private static final String HOST = "localhost";
    private static final int PORT = 9200;
    private static final String INDEX_NAME = "prod";
    private static final int MAX = 2;

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
            //AfterPitSearch afterPitSearch = new AfterPitSearch();
            //afterPitSearch.executeAfterPitQuery(client, INDEX_NAME);

            // Search After + PIT + Slice
            AfterPitSliceSearch afterPitSliceSearch = new AfterPitSliceSearch();
            afterPitSliceSearch.executeAfterPitSliceQuery(client, INDEX_NAME, 0, MAX);

        } catch (Exception e) {
            System.out.println("e = " + e);
        }
    }
}
