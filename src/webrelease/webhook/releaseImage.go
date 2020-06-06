package webhook

import (
	"bytes"
	"github.com/golang/glog"
	"os/exec"
	"webrelease/util"
)

func ReleaseImage(buildHadle BuildHandle) {
	execBuildCmd(buildHadle)
}

func execBuildCmd(buildHadle BuildHandle) {

	var cmd *exec.Cmd
	buildShell := "build.sh"

	timeStr := util.GetCurrentDate()
	gitUrl := buildHadle.Url
	gitBranch := buildHadle.Branch
	name := buildHadle.Name
	gitDir := name + "_" + timeStr
	version := buildHadle.Version
	group := buildHadle.Group
	tag := version + "." + timeStr
	region := buildHadle.Region
	AK := buildHadle.AK
	SK := buildHadle.SK

	var out bytes.Buffer

	glog.Infof("Execute Param: GitUrl %s, Branch %s, tmpDir %s, ImageName %s, ImageTag %s"+
		"Load Region %s, Swr Group %s", gitUrl, gitBranch, gitDir, name, tag, region, group)

	cmd = exec.Command("bash", buildShell, gitUrl, gitBranch, gitDir, name, tag, region, group, AK, SK)

	cmd.Stdout = &out

	err := cmd.Start()
	if err != nil {
		glog.Error("CMD Start Error:", err)
	}

	err1 := cmd.Wait()
	if err != nil {
		glog.Error("CMD Wait Error:", err1)
	}
}
