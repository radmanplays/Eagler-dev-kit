package net.peyton.gson.adapters;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class JsonArrayTypeAdapter extends TypeAdapter<JsonArray> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, Class<T> type) {
			if (type == JsonArray.class) {
				return (TypeAdapter<T>) new JsonObjectTypeAdapter(gson);
			}
			return null;
		}
	};
	
	private Gson gson;
	
	public JsonArrayTypeAdapter(Gson gson) {
		this.gson = gson;
	}

	@Override
	public void write(JsonWriter out, JsonArray value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		
		out.beginArray();
		for (JsonElement element : value) {
            gson.toJson(element, out);
        }
		out.endArray();
	}

	@Override
	public JsonArray read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		JsonArray jsonArray = new JsonArray();
		in.beginArray();
		while (in.hasNext()) {
			JsonElement element = gson.fromJson(in, JsonElement.class);
            jsonArray.add(element);
		}
		in.endArray();
        return jsonArray;
	}

}
