package net.peyton.gson.adapters;

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class JsonObjectTypeAdapter extends TypeAdapter<JsonObject> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, Class<T> type) {
			if (type == JsonObject.class) {
				return (TypeAdapter<T>) new JsonObjectTypeAdapter(gson);
			}
			return null;
		}
	};
	
	private Gson gson;
	
	public JsonObjectTypeAdapter(Gson gson) {
		this.gson = gson;
	}

	@Override
	public void write(JsonWriter out, JsonObject value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		
		out.beginObject();
		for (Map.Entry<String, JsonElement> entry : value.entrySet()) {
			out.name(entry.getKey());
			gson.toJson(entry.getValue(), out);
		}
		out.endObject();
	}

	@Override
	public JsonObject read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		JsonObject jsonObject = new JsonObject();
		in.beginObject();
		while (in.hasNext()) {
			String name = in.nextName();
			JsonElement value = gson.fromJson(in, JsonElement.class);
			jsonObject.add(name, value);
		}
		in.endObject();
        return jsonObject;
	}

}
