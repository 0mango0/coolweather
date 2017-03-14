package com.coolweather.app.util;

public interface HttpCallbackListener {
	void onFinish(String respone);

	void onError(Exception e);
}
