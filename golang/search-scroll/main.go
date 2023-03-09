package main

import (
	"context"
	"fmt"
	esclient "github.com/olivere/elastic/v7"
	"io"
	"search/performance/setting"
	"search/performance/util"
	"time"
)

var searchResults []*esclient.SearchHit
var elapsedTime time.Duration
var startTime time.Time

func main() {
	setting.LoadConfig("../config/config.yml")
	setting.InitEsClient()
	unlinkClient := setting.EsClient

	startTime = time.Now()
	fmt.Println(startTime)

	defer func() {
		elapsedTime = time.Since(startTime)
		fmt.Println("Execution time: ", elapsedTime.Seconds(), len(searchResults), time.Now())
	}()

	index := setting.GetTargetIndex()
	size := 10000
	sum := 0

	scrollQuery := unlinkClient.Scroll(index).Size(size)

	for {
		searchResult, err := scrollQuery.Do(context.Background())
		if err == io.EOF {
			unlinkClient.ClearScroll(index).ScrollId(searchResult.ScrollId).Do(context.Background())
			fmt.Println("Delete Scroll Id")
			break
		}
		if err != nil {
			fmt.Println("Failed to retrieve data from scroll:", err)
			return
		}

		hitSize := len(searchResult.Hits.Hits)
		if hitSize == 0 {
			break
		}

		sum += hitSize
		if sum%100000 == 0 {
			elapsedTime := time.Since(startTime)
			fmt.Println("sum = ", elapsedTime.Seconds(), sum, time.Now())
		}
		//searchResults = append(searchResults, searchResult.Hits.Hits...)
	}

	util.WriteSearchEs(searchResults)
}

func printProgress(sum int) {
	if sum%100000 == 0 {
		elapsedTime := time.Since(startTime)
		fmt.Println("sum = ", elapsedTime.Seconds(), sum, time.Now())
	}
}
