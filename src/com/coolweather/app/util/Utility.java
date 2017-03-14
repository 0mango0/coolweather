package com.coolweather.app.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;

public class Utility {
	/**
	 * 解析和处理服务器返回的省级数据
	 * 
	 * @param coolWeatherDB
	 * @param respone
	 * @return
	 */
	public  static boolean handleProvincesResponse(
			CoolWeatherDB coolWeatherDB, String respone) {
		if (!TextUtils.isEmpty(respone)) {
			String[] allProvinces = respone.split(",");
			if (allProvinces != null && allProvinces.length > 0) {

				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					coolWeatherDB.saveProvince(province);
				}
				
				return true;

			}
		}

		return false;
	}

	/**
	 * 解析和处理服务器返回的市级数据
	 * 
	 * @param coolWeatherDB
	 * @param response
	 * @param provinceId
	 * @return
	 */
	public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
			String response, int provinceId) {

		if (!TextUtils.isEmpty(response)) {

			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}

				return true;
			}

		}

		return false;

	}

	/**
	 * 解析和处理服务器返回的县级数据
	 * 
	 * @param coolWeatherDB
	 * @param response
	 * @param cityId
	 * @return
	 */
	public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					Country country = new Country();
					country.setCountyCode(array[0]);
					country.setCountyName(array[1]);
					country.setCityId(cityId);
					coolWeatherDB.saveCounty(country);
				}

				return true;
			}
		}
		return false;
	}

}
