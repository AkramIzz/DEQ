package com.interpreter;

class Instance {
	private Class klass;

	Instance(Class klass) {
		this.klass = klass;
	}

	@Override
	public String toString() {
		return "<instance of " + klass.name + ">";
	}
}