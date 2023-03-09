package util

import (
	"bufio"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"search/performance/setting"

	esclient "github.com/olivere/elastic/v7"
)

func Writer() (w *bufio.Writer) {
	dump, dumpErr := os.Create(setting.GetDumpFilePath())
	if dumpErr != nil {
		fmt.Println(dumpErr.Error())
	}
	w = bufio.NewWriter(dump)
	return
}

func WriteSearchEs(re []*esclient.SearchHit) {
	writeYn := setting.WriteDumpYN()
	if writeYn == "Y" {
		w := Writer()
		for _, element := range re {
			w.WriteString(string(element.Source) + "====\n")
		}
		w.Flush()
	}
}

func WriteSearchEsV2(re []interface{}) {
	writeYn := setting.WriteDumpYN()
	if writeYn == "Y" {
		w := Writer()
		for _, element := range re {
			b, err := json.Marshal(element)
			if err != nil {
				log.Printf("Error marshaling data: %v\n", err)
				continue
			}
			w.WriteString(string(b) + "====\n")
		}
		w.Flush()
	}
}
