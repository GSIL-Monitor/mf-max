package utils

import (
	"os"
	"archive/zip"
	"path/filepath"
	"io"
	"fmt"
	"errors"
)

//解压
func Unzip(archive, target string) error {
	reader, err := zip.OpenReader(archive) //打开zip文件
	if err != nil {
		Logger().Warn("zip.OpenReader:%v fail:%v", archive, err)
		return err
	}

	if err := os.MkdirAll(target, 0755); err != nil {
		Logger().Warn("os.MkdirAll:%v fail:%v", target, err)
		return err
	}

	for _, file := range reader.File {
		path := filepath.Join(target, file.Name)
		if file.FileInfo().IsDir() {
			os.MkdirAll(path, file.Mode())
			continue
		}

		fileReader, err := file.Open()
		if err != nil {
			Logger().Warn("file.Open fail:%v", err)
			return err
		}
		defer fileReader.Close()

		targetFile, err := os.OpenFile(path, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, file.Mode())
		if err != nil {
			Logger().Warn("os.OpenFile:%v fail:%v", path, err)
			return err
		}
		defer targetFile.Close()

		if _, err := io.Copy(targetFile, fileReader); err != nil {
			Logger().Warn("io.Copy:%v fail:%v", targetFile, err)
			return err
		}
	}

	return nil
}

//判断该文件是否是zip文件
func IsZip(zipPath string) bool {
	len := len(zipPath)
	if zip := zipPath[len-3 : len]; zip == "zip" {
		return true
	}

	return false
}

func GetAppNameFromZipFile(zipFile string) (string, error) {
	len := len(zipFile)
	if len-4 <= 0 {
		errInfo := fmt.Sprintf("zipFile:%v不是完整的zip文件", zipFile)
		return "", errors.New(errInfo)
	}
	return zipFile[:len-4], nil
}

//判断该文件是否是tar.gz文件
func IsTargz(targzPath string) bool {
	len := len(targzPath)
	if zip := targzPath[len-7 : len]; zip == ".tar.gz" {
		return true
	}

	return false
}
