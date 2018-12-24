package utils

import (
	"bytes"
	"encoding/json"
	"errors"
	"io/ioutil"
	"mime/multipart"
	"net/http"
	"strconv"
	"time"
	"net"
	"fmt"
)

func Post(url string, data interface{}) ([]byte, error) {
	contentType := "application/json;charset=utf-8"
	var res *http.Response
	buf, err := json.Marshal(data)
	if err != nil {
		Logger().Error("Marshal failed,err:%+v,data:%+v", err, data)
		return nil, err
	}
	Logger().Info("url: %+v, buf: %v", url, string(buf))
	body := bytes.NewBuffer([]byte(buf))
	res, err = http.Post(url, contentType, body)
	if err != nil {
		Logger().Error("post failed,err:%+v,data:%+v", err, data)
		return nil, err
	}

	if res.StatusCode != http.StatusOK {
		Logger().Error("post failed,err:%+v,data:%+v", err, data)
		return nil, errors.New("POST请求失败，statusCode： " + strconv.Itoa(res.StatusCode))
	}

	resBody, err := ioutil.ReadAll(res.Body)
	defer res.Body.Close()

	if res.StatusCode != http.StatusOK {
		Logger().Error("post failed,statusCode:%+v, data:%+v, response: %v", res.StatusCode, data, string(resBody[:]))
		return nil, errors.New("响应码异常")
	}
	if err != nil {
		Logger().Error("Post ReadAll failed,err:%+v,url:%+v,data:%+v", err, url, data)
		return nil, err
	}
	return resBody, nil
}
func Put(url string, data interface{}) ([]byte, error) {
	var res *http.Response
	buf, err := json.Marshal(data)
	if err != nil {
		Logger().Error("Marshal failed,err:%+v,data:%+v", err, data)
		return nil, err
	}
	Logger().Info("url: %+v, buf: %v", url, string(buf))
	body := bytes.NewBuffer([]byte(buf))
	req, _ := http.NewRequest("PUT", url, body)
	req.Header.Add("Content-Type", "application/json")
	res, err = http.DefaultClient.Do(req)
	if err != nil {
		Logger().Error("put failed,err:%+v,data:%+v", err, data)
		return nil, err
	}
	defer res.Body.Close()
	if res.StatusCode != http.StatusOK {
		Logger().Error("put failed,err:%+v,data:%+v", err, data)
		return nil, errors.New("PUT请求失败，statusCode： " + strconv.Itoa(res.StatusCode))
	}
	resBody, err := ioutil.ReadAll(res.Body)
	if err != nil {
		Logger().Error("Put ReadAll failed,err:%+v,url:%+v,data:%+v", err, url, data)
		return nil, err
	}
	return resBody, nil
}
func PostFormData(url string, data map[string]string) ([]byte, error) {
	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)
	for key, val := range data {
		_ = writer.WriteField(key, val)
	}
	writer.Close()
	var res *http.Response
	Logger().Info("url: %+v, buf: %v, %+v", url, data)

	res, err := http.Post(url, writer.FormDataContentType(), body)

	if err != nil {
		Logger().Error("postFormData failed,err:%+v,data:%+v", err, data)
		return nil, err
	}

	resBody, err := ioutil.ReadAll(res.Body)
	defer res.Body.Close()

	if res.StatusCode != http.StatusOK {
		Logger().Error("post failed,statusCode:%+v, data:%+v, response: %v", res.StatusCode, data, string(resBody[:]))
		return nil, errors.New("响应码异常")
	}
	if err != nil {
		Logger().Error("Post ReadAll failed,err:%+v,url:%+v,data:%+v", err, url, data)
		return nil, err
	}
	return resBody, nil
}

/*
func Get(url string) ([]byte, error) {

	Logger().Info("get url: %+v ", url)
	timeout := time.Duration(50 * time.Second)
	client := http.Client{
		Timeout: timeout,}
	res, err := client.Get(url)
	//res, err := http.Get(url)
	if err != nil {
		Logger().Error("get failed,err:%+v ", err)
		return nil, err
	}
	resBody, err := ioutil.ReadAll(res.Body)
	defer res.Body.Close()
	if err != nil {
		Logger().Error("Get ReadAll failed,err:%+v,url:%+v", err, url)
		return nil, err
	}
	return resBody, nil
}
*/

func Get(url string) ([]byte, error) {
	Logger().Info("get url: %+v ", url)
	connectTimeout := 6 * time.Second //实际连接时间3s
	readWriteTimeout := 20 * time.Second

	client := http.Client{
		Transport: &http.Transport{
			Dial: TimeoutDialer(connectTimeout, readWriteTimeout),
		},
	}

	//timeout := time.Duration(50 * time.Second)
	//client := http.Client{
	//	Timeout: timeout,}
	res, err := client.Get(url)
	//res, err := http.Get(url)
	if err != nil {
		Logger().Error("get failed,err:%+v ", err)
		return nil, err
	}
	statusCode := res.StatusCode
	if statusCode != 200 {
		errInfo := fmt.Sprintf("get url:%v fail statusCode:%v", url, statusCode)
		Logger().Warn(errInfo)
		return nil, errors.New(errInfo)
	}
	resBody, err := ioutil.ReadAll(res.Body)
	defer res.Body.Close()
	if err != nil {
		Logger().Error("Get ReadAll failed,err:%+v,url:%+v", err, url)
		return nil, err
	}
	return resBody, nil
}

func TimeoutDialer(cTimeout time.Duration, rwTimeout time.Duration) func(net, addr string) (c net.Conn, err error) {
	return func(netw, addr string) (net.Conn, error) {
		conn, err := net.DialTimeout(netw, addr, cTimeout)
		if err != nil {
			return nil, err
		}
		conn.SetDeadline(time.Now().Add(rwTimeout))
		return conn, nil
	}
}
