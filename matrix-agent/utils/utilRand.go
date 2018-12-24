package utils

import (
	"math/rand"
	"time"
)

func init() {
	rand.Seed(time.Now().UnixNano())
}
func GetRandInt(min, max int64) int {
	if min >= max {
		return int(max)
	}
	return int(rand.Int63n(max-min) + min)
}
