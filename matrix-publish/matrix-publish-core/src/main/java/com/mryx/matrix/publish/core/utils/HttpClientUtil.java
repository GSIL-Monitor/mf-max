package com.mryx.matrix.publish.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author dinglu
 * @date 2018/9/12
 */
public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static MultiThreadedHttpConnectionManager httpClientManager = new MultiThreadedHttpConnectionManager();

    public HttpClientUtil() {
    }

    public static HttpClient getHttpClient(){

        return new HttpClient(httpClientManager);
    }

    public static String postJson(String url, Object json) {
        PostMethod post = new PostMethod(url);
        try {
            StringRequestEntity e = null;
            if (json instanceof JSONObject) {
                e = new StringRequestEntity(((JSONObject) json).toJSONString(), "application/json", "UTF-8");
            } else {
                if (!(json instanceof JSONArray)) {
                    logger.error("not JSON data post");
                    String param1 = "";
                    return param1;
                }

                e = new StringRequestEntity(((JSONArray) json).toJSONString(), "application/json", "UTF-8");
            }
            post.setRequestEntity(e);
            HttpMethodParams param = post.getParams();
            param.setContentCharset("UTF-8");
            int result = getHttpClient().executeMethod(post);
            logger.info("executeMethod.result={}",result);

            String responseResult = post.getResponseBodyAsString();
            String var7 = responseResult;
            return var7;
        } catch (Exception var11) {
            logger.error("executeMethod.error=",var11);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return null;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("projectId",0);
        String res = postJson("http://localhost:18090/projectTask/getByProjectId",jsonObject);
        System.out.println(JSON.parseObject(res));
    }
}
