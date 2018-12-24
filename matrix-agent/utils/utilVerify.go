package utils

import "regexp"

func Verify(phoneNum string) bool {
	reg := `^1[3-9][0-9]\d{8}$`
	rgx := regexp.MustCompile(reg)
	return rgx.MatchString(phoneNum)
}
