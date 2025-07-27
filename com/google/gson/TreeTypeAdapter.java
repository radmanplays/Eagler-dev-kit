/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * Adapts a Gson 1.x tree-style adapter as a streaming TypeAdapter. Since the
 * tree adapter may be serialization-only or deserialization-only, this class
 * has a facility to lookup a delegate type adapter on demand.
 */
final class TreeTypeAdapter<T> extends TypeAdapter<T> {
	private final JsonSerializer<T> serializer;
	private final JsonDeserializer<T> deserializer;
	private final Gson gson;
	private final Class<T> typeToken;
	private final TypeAdapterFactory skipPast;

	/**
	 * The delegate is lazily created because it may not be needed, and creating it
	 * may fail.
	 */
	private TypeAdapter<T> delegate;

	private TreeTypeAdapter(JsonSerializer<T> serializer, JsonDeserializer<T> deserializer, Gson gson,
			Class<T> typeToken, TypeAdapterFactory skipPast) {
		this.serializer = serializer;
		this.deserializer = deserializer;
		this.gson = gson;
		this.typeToken = typeToken;
		this.skipPast = skipPast;
	}

	@Override
	public T read(JsonReader in) throws IOException {
		if (deserializer == null) {
			return delegate().read(in);
		}
		JsonElement value = Streams.parse(in);
		if (value.isJsonNull()) {
			return null;
		}
		return deserializer.deserialize(value, typeToken, gson.deserializationContext);
	}

	@Override
	public void write(JsonWriter out, T value) throws IOException {
		if (serializer == null) {
			delegate().write(out, value);
			return;
		}
		if (value == null) {
			out.nullValue();
			return;
		}
		JsonElement tree = serializer.serialize(value, typeToken, gson.serializationContext);
		Streams.write(tree, out);
	}

	private TypeAdapter<T> delegate() {
		TypeAdapter<T> d = delegate;
		return d != null ? d : (delegate = gson.getDelegateAdapter(skipPast, typeToken));
	}

	/**
	 * Returns a new factory that will match each type against {@code exactType}.
	 */
	public static TypeAdapterFactory newFactory(Class<?> exactType, Object typeAdapter) {
		return new SingleTypeFactory(typeAdapter, exactType, null);
	}

	/**
	 * Returns a new factory that will match each type and its raw type against
	 * {@code exactType}.
	 */
	public static TypeAdapterFactory newFactoryWithMatchRawType(Class<?> exactType, Object typeAdapter) {
		return new SingleTypeFactory(typeAdapter, exactType, null);
	}

	/**
	 * Returns a new factory that will match each type's raw type for assignability
	 * to {@code hierarchyType}.
	 */
	public static TypeAdapterFactory newTypeHierarchyFactory(Class<?> hierarchyType, Object typeAdapter) {
		return new SingleTypeFactory(typeAdapter, null, hierarchyType);
	}

	private static class SingleTypeFactory implements TypeAdapterFactory {
		private final Class<?> exactType;
		private final Class<?> hierarchyType;
		private final JsonSerializer<?> serializer;
		private final JsonDeserializer<?> deserializer;

		private SingleTypeFactory(Object typeAdapter, Class<?> exactType, Class<?> hierarchyType) {
			serializer = typeAdapter instanceof JsonSerializer ? (JsonSerializer<?>) typeAdapter : null;
			deserializer = typeAdapter instanceof JsonDeserializer ? (JsonDeserializer<?>) typeAdapter : null;
			$Gson$Preconditions.checkArgument(serializer != null || deserializer != null);
			this.exactType = exactType;
			this.hierarchyType = hierarchyType;
		}

		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, Class<T> type) {
			boolean matches = exactType != null ? exactType.equals(type) : hierarchyType.isAssignableFrom(type);
			return matches
					? new TreeTypeAdapter<T>((JsonSerializer<T>) serializer, (JsonDeserializer<T>) deserializer, gson,
							type, this)
					: null;
		}
	}
}
