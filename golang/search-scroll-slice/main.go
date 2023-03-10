package main

import (
	"context"
	"fmt"
	"io"
	"search/performance/setting"
	"search/performance/util"
	"sync"
	"time"

	esclient "github.com/olivere/elastic/v7"
)

var (
	wg            sync.WaitGroup
	searchResults []*esclient.SearchHit
	startTime     time.Time
)

func main() {
	setting.LoadConfig("../config/config.yml")
	setting.InitEsClient()
	elasticsearchClient := setting.EsClient

	startTime = time.Now()
	fmt.Println(startTime)

	index := setting.GetTargetIndex()
	size := 10000
	sum := 0

	worker := setting.Worker()
	wg.Add(worker)

	for i := 0; i < worker; i++ {
		sliceQuery := esclient.NewSliceQuery().Id(i).Max(worker)
		go fetchData(index, size, sliceQuery, &sum, elasticsearchClient)
	}

	wg.Wait()

	util.WriteSearchEs(searchResults)

	elapsedTime := time.Since(startTime)
	fmt.Println("Execution time: ", elapsedTime.Seconds(), sum, time.Now())
}

func fetchData(index string, size int, sliceQuery *esclient.SliceQuery, sum *int, client *esclient.Client) {
	defer wg.Done()

	sliceAllQuery := client.Scroll(index).
		Slice(sliceQuery).
		Size(size)

	for {
		searchResult, err := sliceAllQuery.Do(context.Background())
		if err == io.EOF {
			break
		}
		if err != nil {
			fmt.Println(err.Error())
			break
		}

		*sum += len(searchResult.Hits.Hits)
		printProgress(*sum)
		//searchResults = append(searchResults, searchResult.Hits.Hits...)
	}
}

func printProgress(sum int) {
	if sum%100000 == 0 {
		elapsedTime := time.Since(startTime)
		fmt.Println("sum = ", elapsedTime.Seconds(), sum, time.Now())
	}
}
