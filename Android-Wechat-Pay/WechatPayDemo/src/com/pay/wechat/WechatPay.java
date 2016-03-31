package com.pay.wechat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import com.pay.utils.HttpUtils;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;

public class WechatPay {
	/**
	 * 生成订单的方法
	 * 
	 * @param tradeNo 交易号
	 * @param totalFee 支付金额
	 * @param subject 详细描述
	 * @return
	 */
	public static String createOrder(String tradeNo, String totalFee, String subject) {
		String result = "";
		String URL_PREPAY = Constants.URL_PAY_CALLBACK + "/UnifiedOrderServlet";
		try {
			subject = URLEncoder.encode(subject, "UTF-8");
			String url = URL_PREPAY + "?trade_no=" + tradeNo + "&total_fee=" + totalFee + "&subject=" + subject;
			result = HttpUtils.doGet(url);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 支付的方法
	 * 
	 * @param activity
	 * @param result 服务器生成订单返回的json字符串
	 * 
	 */
	public static void pay(Activity activity, String result) {
		IWXAPI api = WXAPIFactory.createWXAPI(activity, Constants.WX_APP_ID); // 将该app注册到微信
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(result);
			PayReq payReq = new PayReq();
			payReq.appId = Constants.WX_APP_ID;
			payReq.partnerId = Constants.WX_MCH_ID;
			payReq.prepayId = jsonObject.getString("prepayid");
			payReq.nonceStr = jsonObject.getString("noncestr");
			payReq.timeStamp = jsonObject.getString("timestamp");
			payReq.packageValue = jsonObject.getString("package");
			payReq.sign = jsonObject.getString("sign");
			api.sendReq(payReq);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
