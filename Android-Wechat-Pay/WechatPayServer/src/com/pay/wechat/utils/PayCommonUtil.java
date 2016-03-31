package com.pay.wechat.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class PayCommonUtil {

	public static String CreateNoncestr(int length) {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String res = "";
		for (int i = 0; i < length; i++) {
			Random rd = new Random();
			res += chars.indexOf(rd.nextInt(chars.length() - 1));
		}
		return res;
	}

	public static String CreateNoncestr() {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String res = "";
		for (int i = 0; i < 16; i++) {
			Random rd = new Random();
			res += chars.charAt(rd.nextInt(chars.length() - 1));
		}
		return res;
	}

	/**
	 * @Description：sign签名
	 * @param characterEncoding
	 *            编码格式
	 * @param parameters
	 *            请求参数
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String createSign(String characterEncoding,
			SortedMap<String, String> parameters) {
		StringBuffer sb = new StringBuffer();
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + com.pay.wechat.config.WechatpayConfig.API_KEY);
		System.out.println("微信第一次签名信息：" + sb.toString());
		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding)
				.toUpperCase();
		return sign;
	}

	/**
	 * @Description：将请求参数转换为xml格式的string
	 * @param parameters
	 *            请求参数
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getRequestXml(SortedMap<Object, Object> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k)
					|| "sign".equalsIgnoreCase(k)) {
				sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
			} else {
				sb.append("<" + k + ">" + v + "</" + k + ">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}

	/**
	 * @Description：返回给微信的参数
	 * @param return_code
	 *            返回编码
	 * @param return_msg
	 *            返回信息
	 * @return
	 */
	public static String setXML(String return_code, String return_msg) {
		return "<xml><return_code><![CDATA[" + return_code
				+ "]]></return_code><return_msg><![CDATA[" + return_msg
				+ "]]></return_msg></xml>";
	}

	/**
	 * 发送xml
	 * 
	 * @param response
	 * @param content
	 */
	public static HttpServletResponse responseContent(
			HttpServletResponse response, String content) {
		try {
			response.setContentType("text/xml");
			// 把xml字符串写入响应
			byte[] xmlData = content.getBytes();

			response.setContentLength(xmlData.length);

			ServletOutputStream os = response.getOutputStream();
			os.write(xmlData);

			os.flush();
			os.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	public static String getValueByTagName(Document doc, String tagName) {
		if (doc == null || null == tagName || tagName.length() == 0) {
			return "";
		}
		NodeList pl = doc.getElementsByTagName(tagName);
		if (pl != null && pl.getLength() > 0) {
			return pl.item(0).getTextContent();
		}
		return "";
	}

	// //XML转字符串 原样取出
	// public static String getXmlString(Document doc){
	// TransformerFactory tf = TransformerFactory.newInstance();
	// try {
	// Transformer t = tf.newTransformer();
	// t.setOutputProperty(OutputKeys.ENCODING,"UTF-8");//解决中文问题，试过用GBK不行
	// t.setOutputProperty(OutputKeys.METHOD, "html");
	// t.setOutputProperty(OutputKeys.VERSION, "4.0");
	// t.setOutputProperty(OutputKeys.INDENT, "no");
	// ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// t.transform(new DOMSource(doc), new StreamResult(bos));
	// return bos.toString();
	// } catch (TransformerConfigurationException e) {
	// e.printStackTrace();
	// } catch (TransformerException e) {
	// e.printStackTrace();
	// }
	// return "";
	// }
}
