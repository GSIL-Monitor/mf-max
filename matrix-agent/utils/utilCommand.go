package utils

import (
	"bufio"
	"io"
	"os/exec"
	"fmt"
	"bytes"
	"errors"
	"io/ioutil"
)

func ExecCommand2(command string) (string, error) {
	Logger().Info("shell exec cmd:%v", command)
	var outputStr string
	cmd := exec.Command("/bin/bash", "-c", command)
	stdout, err := cmd.StdoutPipe()

	if err != nil {
		Logger().Warn("cmd.StdoutPipe fail:%v", err)
		//return false, err
		return "", err
	}
	w := bytes.NewBuffer(nil) //捕捉命令执行错误信息
	cmd.Stderr = w
	cmd.Start()

	reader := bufio.NewReader(stdout)

	//实时循环读取输出流中的一行内容
	for {
		line, err2 := reader.ReadString('\n')
		if err2 != nil || io.EOF == err2 {
			break
		}
		outputStr = fmt.Sprintf("%v\n%v", outputStr, line)

		Logger().Info("shell log:%v", line)
	}

	cmd.Wait()
	if w.String() != "" {
		errInfo := fmt.Sprintf("exec cmd:%v 执行失败：%v", command, w.String())
		Logger().Warn("exec cmd:%v 执行失败：%v", command, w.String())
		return outputStr, errors.New(errInfo)
	}
	Logger().Info("shell exec cmd:%s finished  ", command)

	return outputStr, nil
}

func ExecCommandAsync(command string) error {
	Logger().Info("Async Shell cmd:%v start execute........", command)
	cmd := exec.Command("/bin/bash", "-c", command)
	err := cmd.Start()
	Logger().Info("Async Shell cmd running........", command)
	return err
}

//有cmd执行错误捕获
func ExecCommand(command string) (string, error) {
	Logger().Info("shell exec cmd:%v", command)
	cmd := exec.Command("/bin/bash", "-c", command)
	stdout, err := cmd.StdoutPipe()

	if err != nil {
		Logger().Warn("cmd.StdoutPipe fail:%v", err)
		return "", err
	}
	w := bytes.NewBuffer(nil) //捕捉命令执行错误信息
	cmd.Stderr = w
	cmd.Start()
	content, err := ioutil.ReadAll(stdout)
	if err != nil {
		Logger().Error("ioutil.ReadAll error %+v", err)
		return "", err
	}
	cmd.Wait()
	if w.String() != "" {
		errInfo := fmt.Sprintf("exec cmd:%v 执行失败：%v", command, w.String())
		Logger().Warn("exec cmd:%v 执行失败：%v", command, w.String())
		return "", errors.New(errInfo)
	}
	Logger().Info("shell exec cmd:%v finished  ", command)

	return string(content), nil

}
func ExecCommandNoErr(command string) (string, error) {
	Logger().Info("shell exec cmd:%v", command)
	cmd := exec.Command("/bin/bash", "-c", command)
	stdout, err := cmd.StdoutPipe()

	if err != nil {
		Logger().Warn("cmd.StdoutPipe fail:%v", err)
		return "", err
	}

	cmd.Start()
	content, err := ioutil.ReadAll(stdout)
	if err != nil {
		Logger().Error("ioutil.ReadAll error %+v", err)
		return "", err
	}
	cmd.Wait()
	Logger().Info("shell exec cmd:%s finished  ", command)
	return string(content), nil

}
