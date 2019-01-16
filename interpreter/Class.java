package com.interpreter;

import java.util.List;
import java.util.Map;

class Class implements Callable {
	final String name;
	private final Map<String, Function> methods;

	Class(String name, Map<String, Function> methods) {
		this.name = name;
		this.methods = methods;
	}

	Function findMethod(Instance instance, String name) {
		if (methods.containsKey(name)) {
			return methods.get(name).bind(instance);
		}

		return null;
	}

	public int arity() {
		if (methods.containsKey("init"))
			return methods.get("init").arity();
		return 0;
	}

	public Object call(Interpreter interpreter, List<Object> arguments) {
		Instance instance = new Instance(this);
		Function initializer = findMethod(instance, "init");
		if (initializer != null)
			initializer.call(interpreter, arguments);
		return instance;
	}

	@Override
	public String toString() {
		return "<class " + name + ">";
	}
}