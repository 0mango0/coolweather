package com.coolweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	/**
	 * ���ݿ���
	 */
	public static final String DB_NAME = "cool_weather";
	/**
	 * ���ݿ�汾
	 */
	public static final int VERSION = 1;

	private static CoolWeatherDB coolWeatherDB;

	private SQLiteDatabase db;

	/**
	 * �����췽��˽�л�
	 * 
	 * @param context
	 */
	private CoolWeatherDB(Context context) {

		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();

	}

	public synchronized static CoolWeatherDB getInstance(Context context) {

		if (coolWeatherDB == null) {

			coolWeatherDB = new CoolWeatherDB(context);

		}

		return coolWeatherDB;

	}

	/**
	 * ��Provinceʵ���浽���ݿ�
	 * 
	 * @param province
	 */
	public void saveProvince(Province province) {
		if (province != null) {

			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);

		}
	}

	/**
	 * �����ݿ��ȡȫ������ʡ����Ϣ
	 * 
	 * @return list
	 */
	public List<Province> loadProvince() {

		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {

			do {

				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));

			} while (cursor.moveToNext());

		}
		if (cursor != null) {

			cursor.close();
		}

		return list;

	}

	/**
	 * ��Cityʵ���洢�����ݿ�
	 * 
	 * @param city
	 */
	private void saveCity(City city) {

		if (city != null) {
			ContentValues values = new ContentValues();
			// values.put("id", city.getId());
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}

	/**
	 * �����ݿ��ȡĳʡ�����еĳ�����Ϣ
	 * 
	 * @param provinceId
	 * @return
	 */
	public List<City> loadCities(int provinceId) {

		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id=?",
				new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor.moveToFirst()) {

			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);

			} while (cursor.moveToNext());

		}

		if (cursor != null) {

			cursor.close();

		}
		return list;
	}

	/**
	 * ��Countryʵ���浽���ݿ�
	 * 
	 * @param country
	 */
	public void saveCounty(Country country) {

		if (country != null) {
			ContentValues values = new ContentValues();
			values.put("country_name", country.getCountyName());
			values.put("country_code", country.getCountyCode());
			values.put("city_id", country.getCityId());
			db.insert("Country", null, values);
		}

	}

	/**
	 * �����ݿ��ȡĳ�����������سǵ���Ϣ
	 * 
	 * @param cityId
	 * @return
	 */
	public List<Country> loadCountries(int cityId) {
		List<Country> list = new ArrayList<Country>();
		Cursor cursor = db.query("Country", null, "city_id=?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor.moveToFirst()) {

			do {
				Country country = new Country();
				country.setId(cursor.getInt(cursor.getColumnIndex("id")));
				country.setCountyName(cursor.getString(cursor
						.getColumnIndex("country_name")));
				country.setCountyCode(cursor.getString(cursor
						.getColumnIndex("country_code")));
				country.setCityId(cursor.getInt(cursor
						.getColumnIndex("city_id")));

			} while (cursor.moveToNext());

		}
		if (cursor != null) {
			cursor.close();

		}
		return list;

	}

}
