package service

import (
	"net/http"
	"net"
	"os"
	"syscall"
	"os/signal"
	"matrix-agent/utils"
	"os/exec"
	"sync"
	"errors"
)

type Sig struct {
	Server   http.Server
	Listener net.Listener
}

var (
	sigChan              chan os.Signal
	runningServerReg     sync.RWMutex
	runningServersForked bool
	//isChild              bool
)

func init() {
	sigChan = make(chan os.Signal)
	runningServerReg = sync.RWMutex{}
	runningServersForked = false
}
func NewSig() *Sig {
	return &Sig{}
}

func (s *Sig) HandleSignals() {
	defer func() {
		if err := recover(); err != nil {
			utils.Logger().Error("handleSignals异常:%v", err)
		}
	}()
	var sig os.Signal
	signal.Notify(
		sigChan,
		syscall.SIGHUP, syscall.SIGTSTP,
	)
	pid := syscall.Getpid()
	for {
		sig = <-sigChan
		utils.Logger().Info("pid:%v received sig:%v", pid, sig)
		switch sig {
		case syscall.SIGHUP:
			utils.Logger().Info("pid:%v received SIGHUP start forking", pid)
			err := s.fork()
			if err != nil {
				utils.Logger().Warn("fork err:%v", err)
			}
		case syscall.SIGTSTP:
			utils.Logger().Info("pid:%v received SIGTSTP start forking", pid)
			s.shutdown()
		default:
			utils.Logger().Info("received sig:%v not care", sig)
		}

	}
}
func (s *Sig) fork() (err error) {
	runningServerReg.Lock()
	defer runningServerReg.Unlock()
	// only one server isntance should fork!
	if runningServersForked {
		return errors.New("another process already forked. Ignoring this one.")
	}
	runningServersForked = true
	utils.Logger().Info("restart: forked start....")
	tl := s.Listener.(*net.TCPListener)
	fl, _ := tl.File()
	path := os.Args[0]
	var args []string
	if len(os.Args) > 1 {
		for _, arg := range os.Args[1:] {
			if arg == "-continue" {
				break
			}
			args = append(args, arg)
		}
	}
	args = append(args, "-continue")
	utils.Logger().Info("path：%v args:%v", path, args)
	cmd := exec.Command(path, args...)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	cmd.ExtraFiles = []*os.File{fl}
	err = cmd.Start()
	if err != nil {
		utils.Logger().Error("restart: failed to launch, error: %v", err)
	}
	return
}

func (s *Sig) shutdown() {
	utils.Logger().Info("shutdown listener :%v\n", s.Listener)
	err := s.Listener.Close()
	if err != nil {
		utils.Logger().Warn("pid:%v listener close err:%v", syscall.Getpid(), err)
	} else {
		utils.Logger().Info("pid:%v %v listener closed", syscall.Getpid(), s.Server.Addr)
	}
	os.Exit(1)
}
