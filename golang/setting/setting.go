package setting

import (
	"fmt"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"os"
)

type Config struct {
	EsURI 	   		 []string `yaml:"esURI"`
	EsUserName   	 string   `yaml:"esUserName"`
	EsPassword 	 	 string   `yaml:"esPassword"`
	TargetIndex      string   `yaml:"targetIndex"`
	DumpFilePath     string   `yaml:"dumpFilePath"`
	WriteDumpYN      string   `yaml:"writeDumpYN"`
	Worker           int      `yaml:"worker"`
	SortField        []string `yaml:"sortField"`
}

var Setting = make(map[string]map[string]Config)
var settingEnv Config

func GetEnv() string {
	return os.Getenv("DEV_ADOPTED_ENV")
}

func GetTargetIndex() string {
	return settingEnv.TargetIndex
}

func GetDumpFilePath() string {
	return settingEnv.DumpFilePath
}

func WriteDumpYN() string {
	return settingEnv.WriteDumpYN
}

func Worker() int {
	return settingEnv.Worker
}

func SortField() []string {
	return settingEnv.SortField
}

func LoadConfig(filePath string) error {
	if _, sErr := os.Stat(filePath); sErr == nil {
		if raw, err := ioutil.ReadFile(filePath); err == nil {
			yaml.Unmarshal(raw, &Setting)
			settingEnv = Setting["DEV_ADOPTED_ENV"][GetEnv()]
			fmt.Printf("설정 파일 로딩 완료 %+v\n", settingEnv)
		} else {
			return err
		}
	} else {
		return sErr
	}
	return nil
}
