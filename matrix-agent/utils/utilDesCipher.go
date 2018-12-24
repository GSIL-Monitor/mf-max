package utils

import (
	"bytes"
	"crypto/cipher"
	"crypto/des"
	"encoding/base64"
	"log"
)

var (
	key = []byte{49, 50, 50, 52, 52, 55, 55, 56}
	iv  = []byte{18, 52, 86, 120, 144, 171, 205, 239}
)

func DesBase64Encrypt(originData string) (base64Result string, err error) {

	var block cipher.Block

	if block, err = des.NewCipher(key); err != nil {
		log.Println(err)
		return
	}
	encrypt := cipher.NewCBCEncrypter(block, iv)
	source := PKCS5Padding([]byte(originData), block.BlockSize())

	dst := make([]byte, len(source))
	encrypt.CryptBlocks(dst, source)
	base64Result = base64.StdEncoding.EncodeToString(dst)
	return
}

func DesBase64Decrypt(encryptData string) (originData string, err error) {
	var block cipher.Block

	if block, err = des.NewCipher(key); err != nil {
		log.Println(err)
		return
	}
	encrypt := cipher.NewCBCDecrypter(block, iv)
	source, err := base64.StdEncoding.DecodeString(encryptData)
	if err != nil {
		log.Println(err)
		return
	}

	dst := make([]byte, len(source))
	encrypt.CryptBlocks(dst, source)
	originData = string(PKCS5Unpadding(dst))
	return
}

func PKCS5Padding(cipherText []byte, blockSize int) []byte {
	padding := blockSize - len(cipherText)%blockSize
	padText := bytes.Repeat([]byte{byte(padding)}, padding)
	return append(cipherText, padText...)
}

func PKCS5Unpadding(origData []byte) []byte {
	length := len(origData)
	unPadding := int(origData[length-1])
	return origData[:(length - unPadding)]
}
