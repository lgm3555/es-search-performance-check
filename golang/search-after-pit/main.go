package main

import (
	"context"
	"fmt"
	"search/performance/setting"
	"search/performance/util"
	"time"

	esclient "github.com/olivere/elastic/v7"
)

func main() {
	setting.LoadConfig("../config/config.yml")
	setting.InitEsClient()
	elasticsearchClient := setting.EsClient

	startTime := time.Now()
	fmt.Println(startTime)

	index := setting.GetTargetIndex()
	size := 10000

	var searchResults []*esclient.SearchHit
	var sortValue []interface{}
	sum := 0

	// Point In Time Id
	searchPit, err := elasticsearchClient.OpenPointInTime(index).
		KeepAlive("1m").
		Do(context.Background())
	if err != nil {
		fmt.Println("searchPit error", err.Error())
	}

	searchPitId := searchPit.Id

	for {
		searchResult, err := elasticsearchClient.Search().
			Size(size).
			SortBy(esclient.NewFieldSort("_doc")).
			SearchAfter(sortValue...).
			PointInTime(esclient.NewPointInTimeWithKeepAlive(searchPitId, "1m")).
			Do(context.Background())
		if err != nil {
			fmt.Println("search query error", err.Error())
			break
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
		sortValue = searchResult.Hits.Hits[hitSize-1].Sort
	}

	util.WriteSearchEs(searchResults)

	elapsedTime := time.Since(startTime)
	fmt.Println("Execution time: ", elapsedTime.Seconds(), sum, time.Now())
}
