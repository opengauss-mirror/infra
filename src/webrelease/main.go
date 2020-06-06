package main

import (
	"flag"
	"github.com/golang/glog"
	"webrelease/webhook"
)

func main() {

	defer glog.Flush()
	flag.Parse()
	glog.Warning("Start WebRelease Main!")

	webhook.Run()
}
