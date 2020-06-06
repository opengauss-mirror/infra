package webhook

import (
	"encoding/json"
	"gitee.com/openeuler/go-gitee/gitee"
	"github.com/golang/glog"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"net/http"
	"strings"
	"webrelease/util"
)

const PushType string = "Push Hook"
const WebHookUrl string = "/webhook/build_image"
const WebServerPort string = "8080"

var projectConfigs PConfig

type ImageConfig struct {
	Url     string   `yaml:"git_url"`
	Branch  []string `yaml:"branch"`
	Name    string   `yaml:"name"`
	Version string   `yaml:"version"`
	Group   string   `yaml:"image_group"`
}

type PConfig struct {
	ImageConfig []ImageConfig `yaml:"imageConfig"`
	Region      string        `yaml:"region"`
	AK          string        `yaml:"AK"`
	SK          string        `yaml:"SK"`
	WebhookSec  string        `yaml:"webhookSec"`
}

func Run() {

	projectConfigs = getConf()

	http.HandleFunc(WebHookUrl, webhookFunc)
	err := http.ListenAndServe(":"+WebServerPort, nil)
	if err != nil {
		glog.Error("Server Started Failed. ", err)
	} else {
		glog.Error("Server Started Success")
	}
}

func getConf() PConfig {
	gitWatch := PConfig{}
	yamlFile, err := ioutil.ReadFile("config/config.yaml")
	if err != nil {
		glog.Error("Read config File failed. ", err)
		return gitWatch
	}

	err = yaml.Unmarshal(yamlFile, &gitWatch)
	if err != nil {
		glog.Error("Unmarshal yarmFile Failed. ", err)
		return gitWatch
	}
	glog.Info("Read config file success")
	return gitWatch
}

func webhookFunc(w http.ResponseWriter, r *http.Request) {

	defer glog.Flush()

	//Read webhook http request body
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		glog.Error("Read request body failed:", err)
		return
	}

	webhookSec := r.Header.Get("X-Gitee-Token")
	if webhookSec != projectConfigs.WebhookSec {
		glog.Error("Webhook Secrets is not correct.")
		return
	}

	var pushEvent gitee.PushEvent
	formatErr := json.Unmarshal(body, &pushEvent)
	if formatErr != nil {
		glog.Error("Error to fromat pushEvent:", formatErr)
		return
	}

	var branch string

	ref := *pushEvent.Ref
	refArr := strings.Split(ref, "/")
	if len(refArr) > 0 {
		branch = refArr[len(refArr)-1]
	}

	projectHook := pushEvent.Repository
	gitUrl := projectHook.Url

	messageType := gitee.WebHookType(r)
	if messageType != PushType {
		glog.Error("Webhook event type is not Push Hook.", messageType)
		return
	}

	imgConf := getImageConf(gitUrl)
	var branchList = imgConf.Branch
	if !util.ArrContains(branch, branchList) {
		glog.Errorf("Webhook giturl or branch not in config list. gitUrl is %s and branch is %s", gitUrl, branch)
		return
	}
	glog.Warningf("Start build images. gitUrl is %s and git branch is %s", gitUrl, branch)

	buildImage := BuildHandle{
		Url:     gitUrl,
		Branch:  branch,
		Name:    imgConf.Name,
		Version: imgConf.Version,
		Group:   imgConf.Group,
		Region:  projectConfigs.Region,
		AK:      projectConfigs.AK,
		SK:      projectConfigs.SK,
	}
	ReleaseImage(buildImage)
}

func getImageConf(gitUrl string) ImageConfig {
	var conf ImageConfig
	for index := range projectConfigs.ImageConfig {
		if projectConfigs.ImageConfig[index].Url == gitUrl {
			conf = projectConfigs.ImageConfig[index]
			break
		}
	}
	return conf
}
