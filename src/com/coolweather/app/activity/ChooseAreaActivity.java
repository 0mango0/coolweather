package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	/**
	 * 标记当前是省级页面
	 */
	public static final int LEVEL_PROVIDER = 0;
	/**
	 * 标记当前是市级页面
	 */
	public static final int LEVEL_CITY = 1;
	/**
	 * 标记当前 是县级页面
	 */
	public static final int LEVEL_COUNTY = 2;
	/**
	 * 加载对话框
	 */
	private ProgressDialog progressDialog;
	/**
	 * 标题
	 */
	private TextView titleText;
	/**
	 * 用来展现省市县列表
	 */
	private ListView listView;
	/**
	 * 适配器
	 */
	private ArrayAdapter<String> adapter;
	/**
	 * 操作数据库的工具类
	 */
	private CoolWeatherDB coolWeatherDB;
	/**
	 * 适配Listview的数据
	 */
	private List<String> dataList = new ArrayList<String>();
	/**
	 * 省列表
	 */
	private List<Province> provincesList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 县列表
	 */
	private List<Country> countyList;
	/**
	 * 选中省份
	 */
	private Province selectedProvince;
	/**
	 * 选中城市
	 */
	private City selectedCity;
	/**
	 * 当前选中级别
	 */
	private int currentLevel;
	/**
	 * 是否从WeatherActivity中跳转出来
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weather_city", false);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {

			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;

		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		initView();

	}

	private void initView() {
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		coolWeatherDB = CoolWeatherDB.getInstance(this);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if (currentLevel == LEVEL_PROVIDER) {
					selectedProvince = provincesList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {

					selectedCity = cityList.get(index);
					queryCounties();

				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}

		});
		queryProvinces();// 加载省级数据
	}

	/**
	 * 查询全国所有的省，优先从数据库中查询，如果没有查询到再去服务器上查询
	 */
	private void queryProvinces() {

		provincesList = coolWeatherDB.loadProvince();
		if (provincesList.size() > 0) {
			dataList.clear();
			for (Province province : provincesList) {
				dataList.add(province.getProvinceName());

			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVIDER;
		} else {
			// 从服务器上查询所有的省
			queryFromServer(null, "province");
		}
	}

	/**
	 * 查询选中省内所有得的市，优先从数据库查询，如果没有查到，再从服务器上查询
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/**
	 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到，再去服务器查询
	 */
	private void queryCounties() {

		countyList = coolWeatherDB.loadCountries(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (Country country : countyList) {
				dataList.add(country.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}

	}

	private void queryFromServer(final String code, final String type) {

		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}

		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String respone) {

				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(coolWeatherDB,
							respone);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolWeatherDB,
							respone, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(coolWeatherDB,
							respone, selectedCity.getId());
				}

				if (result) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});

				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败！",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载中。。。。。");
			progressDialog.setCanceledOnTouchOutside(false);

		}
		progressDialog.show();

	}

	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/**
	 * 捕获back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出？
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}

	}

}
