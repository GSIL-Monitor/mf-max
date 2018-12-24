package utils

import (
	"net"
	"os"
)

func GetLocalIpBak() string {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		Logger().Info("error %v", err)
		os.Exit(1)
	}
	for _, address := range addrs {
		// 检查ip地址判断是否回环地址
		if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				Logger().Info("Local Ip is %v", ipnet.IP.String())
				return ipnet.IP.String()
			}
		}
	}
	return ""
}
func GetLocalIp() string {
	inter, err := net.InterfaceByName("eth0")
	if err != nil {
		Logger().Info("无法获取信息: %v", err)
		return ""
	}
	addrs, err := inter.Addrs()
	if err != nil {
		Logger().Info("eth0 inter.Addrs() %v", err)
		return ""
	}
	// 获取IP地址
	for _, addr := range addrs {
		if ip, ok := addr.(*net.IPNet); ok && !ip.IP.IsLoopback() {
			if ip.IP.To4() != nil {
				Logger().Info("Local Ip is %v", ip.IP.String())
				return ip.IP.String()

			}
		}
	}
	return ""
}
func GetEnv(key string) string {
	value := os.Getenv(key)
	Logger().Info("value = %v", value)
	return value
}

// LocalIPs return all non-loopback IPv4 addresses
func GetLocalIPv4s() ([]string, error) {
	var ips []string
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return ips, err
	}
	for _, address := range addrs {
		if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() && ipnet.IP.To4() != nil {
			ips = append(ips, ipnet.IP.String())
		}
	}
	return ips, nil
}

func GetLocalIpv4() string {
	netInterfaces, err := net.Interfaces()
	if err != nil {
		Logger().Info("net.Interfaces failed %v", err)
		os.Exit(1)
	}
	for i := 0; i < len(netInterfaces); i++ {
		if (netInterfaces[i].Flags & net.FlagUp) != 0 {
			addrs, _ := netInterfaces[i].Addrs()
			for _, address := range addrs {
				if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
					if ipnet.IP.To4() != nil {
						Logger().Info("Local ipnet Ip is %v", ipnet.IP.String())
						return ipnet.IP.String()
					}
				}
			}
		}
	}
	return ""
}
