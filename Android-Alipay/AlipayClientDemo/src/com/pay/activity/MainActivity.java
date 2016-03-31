package com.pay.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.pay.alipay.AlipayAPI;
import com.pay.alipay.PayResult;
import com.pay.alipay.R;

public class MainActivity extends Activity implements OnClickListener{
	
	private static final int SDK_PAY_FLAG = 1;
	
	private Button btnAlipay;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				PayResult payResult = new PayResult((String) msg.obj);
				/**
				 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
				 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
				 * docType=1) 建议商户依赖异步通知
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
