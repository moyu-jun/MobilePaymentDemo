package com.pay.wechat.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.JDOMException;

import com.pay.wechat.config.WechatpayConfig;
import com.pay.wechat.utils.PayCommonUtil;
import com.pay.wechat.utils.StringUtil;
import com.pay.wechat.utils.WechatPayPrepareUtil;
import com.pay.wechat.utils.XMLUtil;

/**
 * @author Kylin
 */
public class PaymentController {

	/**
	 * 统一下单，生成订单信息
	 * @return
	 */
	public String unifiedOrder(HttpServletRequest request,
			HttpServletResponse response) {
		
		String CURRENT_IP = this.getRemoteHost(request); // 客户端用户IP
		
		String result = ""; // 最终返回数据

		// 订单号
		String orderNo = request.getParameter("trade_no");
		// 订单金额
		String money = request.getParameter("total_fee");
		// 商品描述根据情况修改
		String body = request.getParameter("subject");

		try {
			body = new String(body.getBytes("iso8859-1"), "utf-8"); // 编码转换
		} catch (UnsupportedEncodingException e) {
			System.err.println("不支持的字符编码");
		}

		// 金额转化为分为单位
		float sessionmoney = Float.parseFloat(money);
		String finalmoney = String.format("%.2f", sessionmoney);
		finalmoney = finalmoney.replace(".", "");

		// 商户相关资料
		String appid = WechatpayConfig.APPID;
		String partner = WechatpayConfig.PARTNERID;

		// 商户号
		String mch_id = partner;
		// 随机字符串，不长于32位。
		String nonce_str = PayCommonUtil.CreateNoncestr();
		String nonce_str_old = nonce_str;

		// 商户订单号
		String out_trade_no = orderNo;
		int intMoney = Integer.parseInt(finalmoney);

		// 总金额以分为单位，不带小数点
		int total_fee = intMoney;
		// 订单生成的机器 IP
		String spbill_create_ip = this.getRemoteHost(request);

		// 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
		String notify_url = WechatpayConfig.WECHAT_NOTIFY_URL;
		// 交易类型 : 交易类型 trade_type 是 String(16) JSAPI 取值如下：JSAPI，NATIVE，APP
		String trade_type = WechatpayConfig.TRADE_TYPE_APP;

		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", notify_url);
		packageParams.put("total_fee", total_fee + "");
		packageParams.put("trade_type", trade_type);
		// 签名
		String sign = PayCommonUtil.createSign(WechatpayConfig.SIGN_ENCODE,
				packageParams);
		packageParams.put("sign", sign);

		StringBuilder xml = new StringBuilder();
		xml.append("<xml>");
		xml.append("<appid>" + appid + "</appid>");
		xml.append("<body><![CDATA[" + body + "]]></body>");
		xml.append("<mch_id>" + mch_id + "</mch_id>");
		xml.append("<nonce_str>" + nonce_str + "</nonce_str>");
		xml.append("<notify_url>" + notify_url + "</notify_url>");
		xml.append("<out_trade_no>" + out_trade_no + "</out_trade_no>");
		xml.append("<spbill_create_ip>" + spbill_create_ip
				+ "</spbill_create_ip>");
		xml.append("<total_fee>" + total_fee + "</total_fee>");
		xml.append("<trade_type>" + trade_type + "</trade_type>");
		xml.append("<sign>" + sign + "</sign>");
		xml.append("</xml>");
		System.out.println(CURRENT_IP + ": Currently got xml is : " + xml);

		WechatPayPrepareUtil wpUtil = new WechatPayPrepareUtil();
		String prepay_id = "";
		// 获取预支付交易号
		Map<String, String> rMap = null;
		try {
			rMap = wpUtil.submitXmlGetPrepayId(xml.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		prepay_id = StringUtil.nullToEmpty(rMap.get("prepay_id"));

		String finalsign = "";
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

		if (prepay_id != null & !prepay_id.equals("")) {

			SortedMap<String, String> finalpackage = new TreeMap<String, String>();
			finalpackage.put("appid", appid); // appid
			finalpackage.put("noncestr", nonce_str_old); // 随机字符串
			finalpackage.put("package", "Sign=WXPay"); // 固定值
			finalpackage.put("partnerid", mch_id); // 商户id（微信商户平台获取）
			finalpackage.put("prepayid", prepay_id); // 第一次请求微信，成功后，返回的参数
			finalpackage.put("timestamp", timestamp); // 时间戳 十位

			// 签名
			finalsign = PayCommonUtil.createSign(WechatpayConfig.SIGN_ENCODE,
					finalpackage);

			result = "{\"appId\":\"" + appid + "\",";
			result = result + "\"return_code\":" + "\""
					+ StringUtil.nullToEmpty(rMap.get("return_code")) + "\",";
			result = result + "\"return_msg\":" + "\""
					+ StringUtil.nullToEmpty(rMap.get("return_msg")) + "\",";
			result = result + "\"err_code\":" + "\""
					+ StringUtil.nullToEmpty(rMap.get("err_code")) + "\",";
			result = result + "\"err_code_des\":" + "\""
					+ StringUtil.nullToEmpty(rMap.get("err_code_des")) + "\",";
			result = result + "\"partnerid\":\"" + mch_id + "\",";
			result = result + "\"prepayid\":\"" + prepay_id + "\",";
			result = result + "\"noncestr\":\"" + nonce_str_old
					+ "\",";
			result = result + "\"timestamp\":\"" + timestamp + "\",";
			result = result + "\"package\":\"Sign=WXPay\",";
			result = result + "\"notify_url\":\"" + notify_url
					+ "\",";
			result = result + "\"sign\":\"" + finalsign + "\"}";

			System.out.println(CURRENT_IP
					+ ": Currently got final signed json is : " + result);
		} else {
			System.out.println(CURRENT_IP + ": 预支付交易号生成失败。。。");

			StringBuilder returnJson = new StringBuilder();
			returnJson.append("{");
			returnJson.append("\"return_code\":" + "\""
					+ StringUtil.nullToEmpty(rMap.get("return_code")) + "\",");
			returnJson.append("\"return_msg\":" + "\""
					+ StringUtil.nullToEmpty(rMap.get("return_msg")) + "\",");
			returnJson.append("\"err_code\":" + "\""
					+ StringUtil.nullToEmpty(rMap.get("err_code")) + "\",");
			returnJson.append("\"err_code_des\":" + "\""
					+ StringUtil.nullToEmpty(rMap.get("err_code_des")) + "\"");
			returnJson.append("}");
			result = returnJson.toString();
			System.out.println(CURRENT_IP + ": Currently got final json is : "
					+ returnJson.toString());
		}

		// 将以上涉及到的字段全部丢给response然后转成JSON传递给客户端；
		return result; 
	}

	/**
	 * Wait for response from wechatpay server.
	 * 
	 * @return
	 */
	public String wPayCallBack(HttpServletRequest request,
			HttpServletResponse response) {
		String str = "wPayCallBack";
		System.out.println("接收到了微信回调：WeChat Call Back Is COMING");

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(ServletInputStream) request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			str = sb.toString();
			System.out.println("Received information from WeChat sever callback is :" + str);
			// 解析对方发来的xml数据
			Map<String, String> map = null;
			try {
				map = XMLUtil.doXMLParse(str);
			} catch (JDOMException e) {
				e.printStackTrace();
			}

			// 解析DOC，取得当前反馈的属性；
			String isSuccess = "";
			isSuccess = (String) map.get("result_code");

			if (isSuccess.toUpperCase().equals("SUCCESS")) {

				// 处理业务逻辑
				System.out.println("==================================================================");
				System.out.println("WeChat Call back received!!");
				System.out.println("==================================================================");

				String outPayString = "";
				String trade_status = map.get("result_code");
				// 如果支付成功，更新充值单或订单状态
				if (null != trade_status && trade_status.equals("SUCCESS")) {
					outPayString = "SUCCESS";
					
					// TODO 在这里处理相应的业务逻辑
				}
				// 返回给微信服务器已经收到并成功完成；
				str = String.format("<xml><return_code>%s</return_code><return_msg>OK</return_msg></xml>",outPayString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 获取客户端请求的IP
	 * @param request
	 * @return
	 */
	private String getRemoteHost(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}

}
