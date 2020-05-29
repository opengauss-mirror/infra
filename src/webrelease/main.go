package main
import(
	"webrelease/webhook"
	"flag"
	"github.com/golang/glog"
)

func main() {

	defer glog.Flush()
	flag.Parse()
	glog.Warning("Start WebRelease Main!")

	webhook.Run()
}

