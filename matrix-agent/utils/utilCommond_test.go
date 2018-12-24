package utils

import (
	"testing"
	"os/exec"
	"fmt"
	"bytes"
)

func Test1(t *testing.T) {
	ExecCommand("java -version")
}
func Test2(t *testing.T) {
	cmd := exec.Command("java -version")
	//buff, err := cmd.Output()
	//if err != nil {
	//	fmt.Print(err.Error())
	//}
	//fmt.Println(string(buff))
	var out bytes.Buffer
	var stderr bytes.Buffer
	cmd.Stdout = &out
	cmd.Stderr = &stderr
	err := cmd.Run()
	if err != nil {
		fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
		return
	}
	fmt.Println("Result: " + out.String())

}