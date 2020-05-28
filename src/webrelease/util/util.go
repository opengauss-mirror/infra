package util

import (
	"strconv"
	"time"
)

func ArrContains(item string, arr []string) bool {
	for i:=0;i<len(arr);i++ {
		if arr[i] == item {
			return true
		}
	}
	return false
}

/**
 * return yyyymmddhhmmss string
 */
func GetCurrentDate() string {
	now := time.Now()
	year := now.Year()
	month := now.Month()
	day := now.Day()
	hours := now.Hour()
	minutes := now.Minute()
	seconds := now.Second()

	return toString(year) + toString(int(month)) + toString(day) + toString(hours) + toString(minutes) + toString(seconds)
}
func toString(num int) string {
	if num < 10 {
		return "0" + strconv.Itoa(num)
	} else {
		return strconv.Itoa(num)
	}
}