package enum

import "matrix-agent/model"

//type BuildType string
type BuildTypeHandle interface {
	HandleBetaType(param *model.StartApp) error
	HandleReleaseType(param *model.StartApp) error
	String() string
}
type (
	BeatType string
	TestType string
	DevType string
	ReleaseType string
	ProdType string
	AutoTestType string
	StressType string
	PresstestType string
)

const (
	Beta      BeatType      = "beta"
	Test      TestType      = "test"
	Dev       DevType       = "dev"
	Release   ReleaseType   = "release"
	Prod      ProdType      = "prod"
	AutoTest  AutoTestType  = "autotest"
	Stress    StressType    = "stress"
	Presstest PresstestType = "presstest"
)

//func (p BuildType) String() string {
//	switch p {
//	case Beta:
//		return "beta"
//	case Test:
//		return "test"
//	case Dev:
//		return "dev"
//	case Release:
//		return "release"
//	case Prod:
//		return "prod"
//	default:
//		return ""
//	}
//}
func (beta BeatType) String() string {
	return "beta"
}
func (test TestType) String() string {
	return "test"
}
func (dev DevType) String() string {
	return "dev"
}
func (release ReleaseType) String() string {
	return "release"
}
func (prod ProdType) String() string {
	return "prod"
}
func (autotest AutoTestType) String() string {
	return "autotest"
}
func (stress StressType) String() string {
	return "stress"
}
func (presstest PresstestType) String() string {
	return "presstest"
}
