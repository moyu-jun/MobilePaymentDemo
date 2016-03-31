# [Android接入支付宝移动支付功能](http://dkylin.com/archives/android-alipay-tutorial.html)

在之前的“动高服务”App的项目开发中，在支付模块使用了三种支付方式：我的钱包支付、支付宝支付、微信支付。

下面整理总结了Android接入支付宝支付功能的过程。

[博客地址](http://dkylin.com/archives/android-alipay-tutorial.html)

## 开发准备

[1. 支付宝官方文档-移动支付](https://doc.open.alipay.com/doc2/detail?treeId=59&articleId=103563&docType=1)

[2. 移动支付的SDK&DEMO下载](http://aopsdkdownload.cn-hangzhou.alipay-pub.aliyun-inc.com/demo/WS_MOBILE_PAY_SDK_BASE.zip?spm=a219a.7629140.0.0.BLcRab&file=WS_MOBILE_PAY_SDK_BASE.zip)

[3. 移动支付-接入指南](https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.vCcVse&treeId=58&articleId=103541&docType=1) - 此文档非常重要，后面详细说明。

[4. 本教程对应的源代码](https://github.com/dkylin/MobilePaymentDemo/tree/master/Android-Alipay)

## 支付宝接入

想要接入支付宝移动支付功能，必须在[支付宝商家服务平台](https://b.alipay.com/order/productDetail.htm?productId=2015110218010538)进行申请与审核。移动支付功能需要企业或者个体工商户进行申请，审核通过之后方可使用。

如何与支付宝签约并审核请参考官方文档：[移动支付-接入指南](https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.vCcVse&treeId=58&articleId=103541&docType=1)，里面详细介绍了产品签约与审核流程。并且详细介绍了申请成功之后如何查看或生成必要的一些配置参数。

个人开发者则可以下载支付宝提供的[Demo](http://aopsdkdownload.cn-hangzhou.alipay-pub.aliyun-inc.com/demo/WS_MOBILE_PAY_SDK_BASE.zip?spm=a219a.7629140.0.0.BLcRab&file=WS_MOBILE_PAY_SDK_BASE.zip)进行学习研究，在实际项目开发中，将必要参数替换即可。

### 开发前配置

**1. 导入Alipay Jar包**

将下载的Jar包`alipaySDK-xxx.jar`复制到libs文件下，如果libs文件夹不存在则新建一个，然后右键Jar包，选择`Add to Build Path`即可。

如果开发的时候发现Jar包不起作用，那么则进入该项目的`Java Build Path`，选中`Order and Export`，将alipaySDK-xxx.jar勾选即可。

**2. 修改Manifest文件**

添加相应权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

添加支付宝的H5支付页面（当手机没有安装支付宝时调用H5支付页面）：

```xml
<!-- alipay sdk begin -->
<!-- 若手机没有安装支付宝，则调用H5支付页面 -->
<activity
    android:name="com.alipay.sdk.app.H5PayActivity"
    android:configChanges="orientation|keyboardHidden|navigation|screenSize"
    android:exported="false"
    android:screenOrientation="behind"
    android:windowSoftInputMode="adjustResize|stateHidden" >
</activity>
<!-- alipay sdk end -->
```

**3. 添加混淆规则**

在项目的`proguard-project.txt`里添加以下相关规则：

```
-libraryjars libs/alipaySDK-20160223.jar
 
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
```

注意：第一行中的alipaySDK-20160223.jar，其中20160223是此版本发布的日期，注意将其修改为你导入的Jar的相应的文件名。

> 至此开发前的配置已完成，接下来就可以进行代码的编写了。

### 程序结构

在我的Demo项目中，我将支付宝支付相关的类统一放在了`com.pay.alipay`包中。

src目录结构如下：

```
src
├── com.pay.activity
|   ├── MainActivity.java
├── com.pay.alipay
|   ├── AlipayAPI.java
|   ├── AlipayConfig.java
|   ├── Base64.java
|   ├── PayResult.java
|   └── SignUtils.java
└── ...
```

* MainActivity.java - 主界面
* AlipayAPI.java - 支付的一些主要方法
* AlipayConfig.java - 基础配置
* Base64.java - RSA密钥转换
* PayResult.java - 支付结果
* SignUtils.java - RSA签名类

官方Demo中将支付，订单创建等操作都放在了Activity中，为了使代码结构更加清晰，我将支付的实现放在了AlipayAPI中。

### 支付接口调用

以我的写的Demo为例，主界面代码如下：

```java
public class MainActivity extends Activity implements OnClickListener{
	
	private static final int SDK_PAY_FLAG = 1;
	
	private Button btnAlipay;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				PayResult payResult = new PayResult((String) msg.obj);
				/**
				 * 同步返回的结果必须放置到服务端进行验证，建议商户依赖异步通知
				 */
				String resultInfo = payResult.getResult();// 同步返回需要验证的信息

				String resultStatus = payResult.getResultStatus();
				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")) {
					Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
				} else {
					// 判断resultStatus 为非"9000"则代表可能支付失败
					// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Toast.makeText(MainActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}
			}
		};
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initView();
    }
    
    private void initView(){
    	btnAlipay = (Button) findViewById(R.id.btn_alipay);
    	btnAlipay.setOnClickListener(this);
    }
    /**
     * 支付宝支付异步任务
     * @author Kylin
     */
    private class AliPayThread extends Thread {
		@Override
		public void run() {
			String result = AlipayAPI.pay(MainActivity.this, "测试的商品", "测试商品的详细描述", "0.01"); 
			Message msg = new Message();
			msg.what = SDK_PAY_FLAG;
			msg.obj = result;
			mHandler.sendMessage(msg);
		}
	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_alipay:
			// 使用支付宝进行支付
			new AliPayThread().start();
			break;

		default:
			break;
		}
	}
}
```

当点击了支付按钮之后，开一个子线程执行支付操作，调用`AlipayAPI.pay()`方法进行支付，返回`result`之后通知Handler进行处理，取出`resultStatus`参数判断支付是否成功。

> * resultStatus = 9000 ：支付成功
> * resultStatus = 8000 ：支付确认中（小概率事件）
> * resultStatus = 其他 ：支付失败

### 支付调用过程

上面介绍了支付的调用以及返回结果的获取和判断这一完整的支付流程，下面解释一下支付调用的过程。

首先需要在`AlipayConfig.java`中配置相应的参数，参数的获取在上面有介绍（由于参数比较敏感，所以此处为空）：

```java
public class AlipayConfig {
	// 商户PID
	public static final String PARTNER = "";
	// 商户收款账号
	public static final String SELLER = "";
	// 商户私钥，pkcs8格式
	public static final String RSA_PRIVATE = "";
	// 支付宝公钥
	public static final String RSA_PUBLIC = "";
}
```

配置好了参数之后才可以使程序正常运行，下面来看`AlipayAPI.pay()`方法：

```java
/**
 * @param activity
 * @param subject 商品名称
 * @param body 商品的详细描述
 * @param price 支付金额
 * @return
 */
public static String pay(Activity activity, String subject, String body, String price) {
		
	String orderInfo = getOrderInfo(subject, body, price); // 创建订单信息
	/**
	 * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
	 */
	String sign = sign(orderInfo);
	try {
		sign = URLEncoder.encode(sign, "UTF-8"); // 仅需对sign 做URL编码
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
	/**
	 * 完整的符合支付宝参数规范的订单信息
	 */
	final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

	PayTask alipay = new PayTask(activity);
	String result = alipay.pay(payInfo, true); // 调用支付接口进行支付
		
	return result;
}
```

首先需要通过`getOrderInfo()`方法创建订单信息，其实就是按照支付宝规定的格式封装相应的数据，其中就有`AlipayConfig.java`中的参数，具体过程请查看源代码。创建完订单信息之后，需要使用`sign()`方法对其签名，然后使用UTF-8编码对`sign`进行编码。

PayTask接口是开发包提供支付，查询的对象接口。下面的代码就是支付宝支付的操作代码，`result`就是支付返回的结果。

```java
PayTask alipay = new PayTask(activity);
String result = alipay.pay(payInfo, true); // 调用支付接口进行支付
```

### 获取当前开发包版本号

可以通过下面的代码获取支付宝开发包的版本号：

```java
PayTask payTask = new PayTask(activity);
String version = payTask.getVersion();
```

## Demo截图

![Android-Alipay-Image](http://7xrnl9.com1.z0.glb.clouddn.com/image%2Fmobile-payment%2Fandroid-alipay.png)

## 注意事项

相信在上面的介绍中都看到了有几处注释说明了，此项操作要放在服务器操作等等之类的。

这是因为代码中有一些参数和操作是比较敏感的，如果被一些不法分子得到之后有可能会对商家造成一定的损失，虽然添加了混淆规则，但是还是将其放在服务器端较为安全。

所以建议在学习了支付宝支付的步骤之后，根据自己的功能需求设计网络通信接口，将支付宝支付中的部分敏感信息及操作放在服务器端进行以保证支付的安全。
