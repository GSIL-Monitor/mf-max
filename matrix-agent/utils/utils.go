package utils

import (
	"errors"
	"strconv"
	"strings"
	"time"
)

const DefaultDateFmt = "2006-01-02"
const DefaultDateTimeFmt = "2006-01-02 15:04:05"

func IsNilTime(t time.Time) bool {
	return t.Format(DefaultDateTimeFmt) == "0001-01-01 00:00:00"
}

func Join(l []int, sep string) (string, error) {
	if l == nil {
		return "", errors.New("无数据")
	}
	var sl []string
	for _, v := range l {
		sl = append(sl, strconv.Itoa(v))
	}
	s := strings.Join(sl, sep)
	return s, nil
}
