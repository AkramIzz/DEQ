package com.interpreter;

import java.util.Map;
import java.util.HashMap;

class Instance {
	private Class klass;
	private Map<String, Object> fields = new HashMap<>();

	Instance(Class klass) {
		this.klass = klass;
	}

	Object get(Token name) {
		if (fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme);
		}

		Function method = klass.findMethod(this, name.lexeme);
		if (method != null) return method;

		throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'");
	}

	void set(Token name, Object value) {
		fields.put(name.lexeme, value);
	}

	@Override
	public String toString() {
		return "<instance of " + klass.name + ">";
	}
}