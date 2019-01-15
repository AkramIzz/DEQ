package com.interpreter;

import java.util.List;

class Class implements Callable {
	final String name;

	Class(String name) {
		this.name = name;
	}

	public int arity() {
		return 0;
	}

	public Object call(Interpreter interpreter, List<Object> arguments) {
		Instance instance = new Instance(this);
		return instance;
	}

	@Override
	public String toString() {
		return "<class " + name + ">";
	}
}