package com.example.alexahern.raindrop;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexahern on 25/04/16.
 */

public class JsonParser {
    private String jsonStr;

    public JsonParser(String jsonStr) {
        this.jsonStr = jsonStr;
    }

    public Double getPercentageChanceOfRain(String timeFrame)
            throws JSONException {
        JSONObject fullForecast = new JSONObject(jsonStr);
        JSONObject forecastForTimeFrame = fullForecast.getJSONObject(timeFrame)
                .getJSONArray("data")
                .getJSONObject(0);

        Double probabilityOfRain = forecastForTimeFrame.getDouble("precipProbability");
        return probabilityOfRain * 100;
    }
}
