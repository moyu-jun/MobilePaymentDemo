package com.pay.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dgaotech.dgfw.R;
import com.pay.wechat.WechatPay;

public class MainActivity extends Activity implements OnClickListener{
	private Button btnWechatPay;

	Handler createOrderHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				String result = (String) msg.obj;
				WechatPay.pay(MainActivity.this, result);
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
    	btnWechatPay = (Button) findViewById(R.id.btn_wechat_pay);
    	btnWechatPay.setOnClickListener(this);
    }
    
	public class CreateOrderThread extends Thread {
		@Override
		public void run() {
			final String trade_no = System.currentTimeMillis()+""; // 每次交易号不一样
			final String total_fee = "0.01";
			final String subject = "测试的商品";
			String result = WechatPay.createOrder(trade_no, total_fee, subject);
			Message msg = createOrderHandler.obtainMessage();
			msg.what = 0;
			msg.obj = result;
			createOrderHandler.sendMessage(msg);
		}
	}
    @Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_wechat_pay:
			// 使用微信进行支付
			new CreateOrderThread().start();
			
			break;

		default:
			break;
		}
	}
}
