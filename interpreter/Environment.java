package com.interpreter;

import java.util.Map;
import java.util.HashMap;

class Environment {
	private final Map<String, Object> values = new HashMap<>();

	void define(String name, Object value) {
		values.put(name, value);
	}

	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
		} else {
			throw new RuntimeError(name,
				"Assignment to undefined variable '" + name.lexeme + "'");
		}
	}

	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
	}
}