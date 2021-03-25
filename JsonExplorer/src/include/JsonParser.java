package include;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonParser {

	public static JsonObject json(String json) {
		return new Gson().fromJson(json, JsonObject.class);
	}
	
	public static JsonArray jsonArr(String json) {
		return new Gson().fromJson(json, JsonArray.class);
	}
	
	public static String prettyJson(JsonObject jo) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(jo);
	}
	
	public static String prettyJson(JsonArray ja) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(ja);
	}
	
}
