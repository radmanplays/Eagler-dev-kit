package net.peyton.gson.adapters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationAdvancementProgressMapTypeAdapter extends TypeAdapter<Map<ResourceLocation, AdvancementProgress>> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, Class<T> type) {
			if (Map.class.isAssignableFrom(type)) {
				return (TypeAdapter<T>) new ResourceLocationAdvancementProgressMapTypeAdapter(gson);
			}
			return null;
		}
	};
	
	private Gson gson;
	private final TypeAdapter<AdvancementProgress> adapter;
	
	public ResourceLocationAdvancementProgressMapTypeAdapter(Gson gson) {
		this.gson = gson;
		this.adapter = gson.getAdapter(AdvancementProgress.class);
	}

	@Override
	public void write(JsonWriter out, Map<ResourceLocation, AdvancementProgress> value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		
		out.beginObject();
		for (Entry<ResourceLocation, AdvancementProgress> entry : value.entrySet()) {
			ResourceLocation key = entry.getKey();
            AdvancementProgress progress = entry.getValue();
            
            out.name(key.toString());
            this.adapter.write(out, progress);
        }
		out.endObject();
	}

	@Override
	public Map<ResourceLocation, AdvancementProgress> read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		Map<ResourceLocation, AdvancementProgress> map = new HashMap<>();
		in.beginObject();
		while (in.hasNext()) {
			String location = in.nextName();
			ResourceLocation id = new ResourceLocation(location);
			
			AdvancementProgress progress = this.adapter.read(in);
			map.put(id, progress);
		}
		in.endObject();
        return map;
	}

}
