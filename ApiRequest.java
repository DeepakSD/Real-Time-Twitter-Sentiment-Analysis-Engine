package org.streaming.Tweet_Sentiment_Analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.IndexOutOfBoundsException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.streaming.Tweet_Sentiment_Analysis.Address;
import org.streaming.Tweet_Sentiment_Analysis.Address.location;

public class ApiRequest {

	public static String jsonCoord(String city) throws IOException {

		String address = city.replace(" ", "");
		// URL url = new
		// URL("http://maps.googleapis.com/maps/api/geocode/json?address=" +
		// address + "&sensor=false");
		URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + address
				+ "&key=AIzaSyC97NJWlbfnRk5h4XRpxGZ3ZLBxmf0Sf4E");
		URLConnection connection = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		String jsonResult = "";
		while ((inputLine = in.readLine()) != null) {
			jsonResult += inputLine;
		}
		in.close();
		return jsonResult;
	}

	@SuppressWarnings("static-access")
	public static String coordinates(String city) throws JsonSyntaxException, IOException,IndexOutOfBoundsException {
		Gson gson = new Gson();
		ApiRequest apireq = new ApiRequest();
		Address result = gson.fromJson(apireq.jsonCoord(city), Address.class);
		if(result.results.length!=0)
		{
			location coords = result.results[0].geometry.location;
			String latitude = coords.lat;
			String longitude = coords.lng;
			return latitude + "::concat::" + longitude;
		}
		else
			return null;
	}

}
