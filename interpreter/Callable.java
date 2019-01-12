package com.interpreter;

import java.util.List;

interface Callable {
	int arity();
	Object call(Interpreter interpreter, List<Object> arguments);
}