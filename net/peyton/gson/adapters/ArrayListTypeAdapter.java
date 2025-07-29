package net.peyton.gson.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ArrayListTypeAdapter extends TypeAdapter<ArrayList<String>> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, Class<T> type) {
			if (List.class.isAssignableFrom(type)) {
				return (TypeAdapter<T>) new ArrayListTypeAdapter(gson);
			}
			return null;
		}
	};

	private Gson gson;

	public ArrayListTypeAdapter(Gson gson) {
		this.gson = gson;
	}

	@Override
	public void write(JsonWriter out, ArrayList<String> value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}

		out.beginArray();
		for (int i = 0, j = value.size(); i < j; ++i) {
			String element = value.get(i);
			out.value(element);
		}
		out.endArray();
	}

	@Override
	public ArrayList<String> read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}

		ArrayList<String> list = new ArrayList<>();
		in.beginArray();
		while (in.hasNext()) {
			if (in.peek() == JsonToken.STRING) {
				list.add(in.nextString());
			} else {
				in.skipValue();
			}
		}
		in.endArray();
		return list;
	}

}
