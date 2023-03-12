package main

import (
	"encoding/json"
	"fmt"
	"search/performance/setting"
	"search/performance/util"
	"strings"
	"sync"
	"time"

	"github.com/elastic/go-elasticsearch/v8"
)

var (
	wg            sync.WaitGroup
	searchResults []interface{}
	startTime     time.Time
	r             map[string]interface{}
)

func main() {
	setting.LoadConfig("../config/config.yml")
	setting.InitEsClientV2()
	elasticsearchClient := setting.EsClientV2

	startTime = time.Now()
	fmt.Println(startTime)

	index := setting.GetTargetIndex()
	size := 10000
	sum := 0

	pitID, err := getPointInTimeID(elasticsearchClient, index)
	if err != nil {
		fmt.Println("Error getting Point In Time ID: ", err)
		return
	}

	worker := setting.Worker()
	wg.Add(worker)

	for i := 0; i < worker; i++ {
		go fetchSearchResults(i, worker, size, &sum, pitID, elasticsearchClient)
	}

	wg.Wait()

	util.WriteSearchEsV2(searchResults)

	elapsedTime := time.Since(startTime)
	fmt.Println("Execution time: ", elapsedTime.Seconds(), sum, time.Now())
}

func getPointInTimeID(client *elasticsearch.Client, index string) (string, error) {
	res, err := client.OpenPointInTime([]string{index}, "1m")
	if err != nil {
		return "", err
	}
	if err := json.NewDecoder(res.Body).Decode(&r); err != nil {
		return "", err
	}

	return r["id"].(string), nil
}

const baseQuery = `
	"slice" : {"id" : %d, "max" : %d},
	"pit" : {"id" : "%s", "keep_alive" : "1m"},
	"sort" : ["_doc"]
`

func fetchSearchResults(i int, worker int, size int, sum *int, pitID string, client *elasticsearch.Client) {
	defer wg.Done()

	var resultLen int
	var after []interface{}

	for {
		var b strings.Builder
		b.WriteString("{\n")
		b.WriteString(fmt.Sprintf(baseQuery, i, worker, pitID))
		if len(after) > 0 && after[0] != "" && after[0] != "null" {
			b.WriteString(", \n")
			b.WriteString(fmt.Sprintf(`	"search_after": [%f]`, after[0]))
			after = []interface{}{}
		}
		b.WriteString("\n}")

		res, err := client.Search(
			client.Search.WithSize(10000),
			client.Search.WithBody(strings.NewReader(b.String())),
		)
		if err != nil {
			fmt.Println("Error Query: ", err)
		}
		if err := json.NewDecoder(res.Body).Decode(&r); err != nil {
			fmt.Println("Error BaseQuery Parsing: ", err)
		}

		resultLen = len(r["hits"].(map[string]interface{})["hits"].([]interface{}))

		for _, sort := range r["hits"].(map[string]interface{})["hits"].([]interface{})[resultLen-1].(map[string]interface{})["sort"].([]interface{}) {
			after = append(after, sort)
		}

		*sum += resultLen
		printProgress(*sum)
		//searchResults = append(searchResults, r["hits"].(map[string]interface{})["hits"].([]interface{})...)

		if resultLen < size {
			return
		}
	}
}

func printProgress(sum int) {
	if sum%100000 == 0 {
		elapsedTime := time.Since(startTime)
		fmt.Println("sum = ", elapsedTime.Seconds(), sum, time.Now())
	}
}
