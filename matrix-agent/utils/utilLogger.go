package utils

import (
	"matrix-agent/logger"
)

var lg *logManager

type logManager struct {
	infoLogger *logger.FileLogger
}

func init() {
	lg = newLogger()
}

func newLogger() (l *logManager) {
	l = &logManager{
		infoLogger: logger.NewDailyLogger("/data/logs/matrix-agent", "matrix-agent", "", 0, 0),
	}
	l.infoLogger.SetLogLevel(logger.INFO)
	return l
}

func Logger() *logManager {
	return lg
}

func (l *logManager) ParamInfo(prefix string, p interface{}) {
	l.infoLogger.I(4, "%v%#v", prefix, p)
}

func (l *logManager) ParamWarn(prefix string, p interface{}) {
	l.infoLogger.W(4, "%v%#v", prefix, p)
}

func (l *logManager) ParamError(prefix string, p interface{}) {
	l.infoLogger.E(4, "%v%#v", prefix, p)
}

func (l *logManager) Info(format string, v ...interface{}) {
	l.infoLogger.I(4, format, v...)
}

func (l *logManager) Warn(format string, v ...interface{}) {
	l.infoLogger.W(4, format, v...)
}

func (l *logManager) Error(format string, v ...interface{}) {
	l.infoLogger.E(4, "[Exception] "+format, v...)
}
