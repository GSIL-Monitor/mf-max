package utils

import (
	"testing"
	"fmt"
)

func TestStart(t *testing.T) {
	startCmdSh := fmt.Sprintf("%v/%v.sh", "/go/gowork/src/matrix-agent", "matrix-agent.jar")
	Logger().Info("startCmdSh:%v", startCmdSh)
	f, err := CreateFile(startCmdSh)
	if err != nil {
		Logger().Warn("create file:%v fail", startCmdSh, err)
		return
	}
	startCmd := "nohup1 /go/gowork/src/matrix-agent/matrix-agent1 -v beta  > /dev/null 2>&1 &"
	//cnt := "#!/bin/bash\nsource /etc/profile\n" + startCmd + "\n"
	cnt := "#!/bin/bash\n" + startCmd + "\n"
	Logger().Info("startCmdSh cnt:%v", cnt)
	_, err = f.WriteString(cnt)
	if err != nil {
		Logger().Warn("f.WriteString:%v fail", startCmdSh, err)
		return
	}
	_, err = ExecCommand(startCmdSh)

}
