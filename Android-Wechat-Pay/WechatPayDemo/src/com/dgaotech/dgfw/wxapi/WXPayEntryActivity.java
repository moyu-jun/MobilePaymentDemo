package com.dgaotech.dgfw.wxapi;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dgaotech.dgfw.R;
import com.pay.wechat.Constants;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = WXPayEntryActivity.class.getSimpleName();

	private IWXAPI api;

	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_result);
		mTextView = (TextView) this.findViewById(R.id.textView_WXPay_Result);
		Button btnClose = (Button) this.findViewById(R.id.btn_WXPay_Close);
		btnClose.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// should place after UI initialize
		api = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID);
		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");

		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq baseReq) {
		Log.d(TAG, "onReq");
	}

	@Override
	public void onResp(BaseResp baseResp) {
		Log.d(TAG, "onPayFinish, errCode = " + baseResp.errCode);
		if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			Log.d(TAG, "支付结果 = " + baseResp.errCode + ", 原因=" + baseResp.errStr);
			String errStr = null;
			switch (baseResp.errCode) {
			case 0:
				errStr = "已支付成功";
				try {
					JSONObject message = new JSONObject();
					message.put("payresult", true);
					// WXPayPlugin.mCallbackContext.sendPluginResult(new
					// PluginResult(PluginResult.Status.OK, message));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case -1:
				errStr = "支付失败，请检查";
				try {
					JSONObject message = new JSONObject();
					message.put("payresult", false);
					// WXPayPlugin.mCallbackContext.sendPluginResult(new
					// PluginResult(PluginResult.Status.OK, message));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case -2:
				errStr = "支付已取消";
				try {
					JSONObject message = new JSONObject();
					message.put("payresult", false);
					// WXPayPlugin.mCallbackContext.sendPluginResult(new
					// PluginResult(PluginResult.Status.OK, message));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			default:
				try {
					JSONObject message = new JSONObject();
					message.put("payresult", false);
					// WXPayPlugin.mCallbackContext.sendPluginResult(new
					// PluginResult(PluginResult.Status.OK, message));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
			mTextView.setText(errStr);
		}
	}
}
