package com.mulesoft.portal.client;

import java.lang.reflect.Field;

import org.json.simple.JSONObject;

public class SimpleToJSON {

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		store(this.getClass(), obj);

		return obj;
	}

	@SuppressWarnings("rawtypes")
	public static <T extends SimpleToJSON> T load(JSONObject obj, Class<T> clazz) {
		try {
			T newInstance = clazz.newInstance();
			loadFields(((Class) newInstance.getClass()), newInstance, obj);
			return newInstance;
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	public void load(JSONObject object) {
		loadFields(this.getClass(),this, object);
		
	}
	@SuppressWarnings("unchecked")
	public static void loadFields(Class<?> cl, Object o, JSONObject jo) {
		Field[] declaredFields = cl.getDeclaredFields();
		for (Field d : declaredFields) {
			d.setAccessible(true);
			try {
				Object object = jo.get(d.getName());
				if (d.getType() == boolean.class) {
					d.set(o, Boolean.parseBoolean((String) (""+object)));
				}
				if (d.getType() == Long.class) {
					d.set(o, object);
				}
				if (d.getType() == String.class) {
					d.set(o, ((String) object));
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		Class<?> superclass = cl.getSuperclass();
		if (superclass != null) {
			loadFields((Class<? extends SimpleToJSON>) superclass, o, jo);
		}
	}

	@SuppressWarnings("unchecked")
	private void store(Class<? extends SimpleToJSON> class1, JSONObject obj) {
		for (Field f : class1.getDeclaredFields()) {
			f.setAccessible(true);
			try {
				Object object = f.get(this);
				if (object != null) {
					obj.put(f.getName(), "" + object);
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		Class<?> superclass = class1.getSuperclass();
		if (superclass != null) {
			store((Class<? extends SimpleToJSON>) superclass, obj);
		}
	}
}
