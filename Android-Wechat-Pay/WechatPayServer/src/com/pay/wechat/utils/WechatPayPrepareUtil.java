package com.pay.wechat.utils;

import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jdom.JDOMException;

import com.pay.wechat.config.WechatpayConfig;


/**
 * 生成预支付订单号
 * 
 * @author Roy Zeng
 * 
 */
public class WechatPayPrepareUtil {
	/**
	 * 生成预支付订单
	 * 
	 * @return
	 */
	public Map<String, String> submitXmlGetPrepayId(String xml) {
		// 创建HttpClientBuilder
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		// HttpClient
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		CloseableHttpResponse httpResponse;
		HttpPost httpPost = new HttpPost(WechatpayConfig.PREORDERURL);
		StringEntity entity;
		Map<String, String> map = null;
		try {
			entity = new StringEntity(xml, "utf-8");
			httpPost.setEntity(entity);
			// post请求
			httpResponse = closeableHttpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				// 打印响应内容
				String result = EntityUtils.toString(httpEntity, "UTF-8");
				System.out.println("响应内容 :" + result);
				// 过滤
				result = result.replaceAll("<![CDATA[|]]>", "");
				try {
					map = XMLUtil.doXMLParse(result);
				} catch (JDOMException e) {
					e.printStackTrace();
				}
			}
			// 释放资源
			closeableHttpClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
}
