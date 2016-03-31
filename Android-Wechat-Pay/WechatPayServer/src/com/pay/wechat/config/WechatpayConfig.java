package com.pay.wechat.config;

/**
 * @author Kylin
 */
public class WechatpayConfig {
	/**
	 * 统一下单URL.
	 */
	public final static String PREORDERURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	public final static String ORDERQUERY = "https://api.mch.weixin.qq.com/pay/orderquery";
	public final static String ACCOUNT_ORDER_DOWNLOAD = "https://api.mch.weixin.qq.com/pay/downloadbill";
	/**
	 * 微信服务器回调地址
	 */
	public final static String WECHAT_NOTIFY_URL = "http://127.0.0.1/WechatPayServer/WechatPayCallBack";
	
	public final static String APPID = "xxxx"; // 你的APP_ID：如：wxd930ea5d5a258f4f
	public final static String API_KEY = "xxx";// API密钥
	/**
	 * 商户相关资料：商户号 mch_id:微信支付分配的商户号
	 */
	public final static String PARTNERID = "xxx"; // 你的PARTNER_ID 
	/**
	 * 商户相关资料：没弄明白是啥
	 */
	public final static String APPSECRET = "";
	/**
	 * 商户相关资料：没弄明白是啥
	 */
	public final static String PARTNERKEY = "";
	/**
	 * 交易类型 trade_type
	 */
	public final static String TRADE_TYPE_APP = "APP";
	public final static String TRADE_TYPE_JSAPI = "JSAPI";
	public final static String TRADE_TYPE_NATIVE = "NATIVE";
	/**
	 * 签名字符集
	 */
	public final static String SIGN_ENCODE = "utf-8";

}
