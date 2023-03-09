package setting

import (
	"fmt"
	"time"

	"github.com/elastic/go-elasticsearch/v8"
	esclient "github.com/olivere/elastic/v7"
)

var EsClient *esclient.Client
var EsClientErr error

var EsClientV2 *elasticsearch.Client
var EsClientErrV2 error

func InitEsClient() {
	esUrls := settingEnv.EsURI
	esUsername := settingEnv.EsUserName
	esPassword := settingEnv.EsPassword

	EsClient, EsClientErr = esclient.NewClient(
		esclient.SetURL(esUrls...),
		esclient.SetBasicAuth(esUsername, esPassword),
		esclient.SetGzip(true),
		esclient.SetSniff(false),
		esclient.SetHealthcheckInterval(10*time.Second),
		esclient.SetMaxRetries(5))
	if EsClientErr != nil {
		// Handle error
		fmt.Println(EsClientErr)
	}
}

func InitEsClientV2() {
	esUrls := settingEnv.EsURI
	esUsername := settingEnv.EsUserName
	esPassword := settingEnv.EsPassword

	cfg := elasticsearch.Config{
		Addresses: esUrls,
		Username:  esUsername,
		Password:  esPassword,
	}

	EsClientV2, EsClientErrV2 = elasticsearch.NewClient(cfg)
	if EsClientErrV2 != nil {
		fmt.Println(EsClientErrV2)
	}
}
