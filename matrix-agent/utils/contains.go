package utils

import "strings"

//
func JudgeLastIsContain(str, seq string) bool {
	len := len(str)
	index := strings.LastIndex(str, seq)
	if index == (len - 1) {
		return true
	}
	return false
}
