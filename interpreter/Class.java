package com.interpreter;

class Class {
	final String name;

	Class(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "<class " + name + ">";
	}
}