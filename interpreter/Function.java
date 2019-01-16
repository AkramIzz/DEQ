package com.interpreter;

import java.util.List;

class Function implements Callable {
	private final Stmt.Function declaration;
	private final Environment closure;
	private final boolean isInitializer;

	Function(Stmt.Function declaration, Environment closure,
			boolean isInitializer) {
		this.isInitializer = isInitializer;
		this.declaration = declaration;
		this.closure = closure;
	}

	Function bind(Instance instance) {
		Environment environment = new Environment(closure);
		environment.define("this", instance);
		return new Function(declaration, environment, isInitializer);
	}

	@Override
	public int arity() {
		return declaration.parameters.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment env = new Environment(this.closure);
		for (int i = 0; i < arguments.size(); ++i) {
			env.define(
				declaration.parameters.get(i).lexeme,
				arguments.get(i)
			);
		}

		try {
			interpreter.executeBlock(declaration.body, env);
		} catch (ReturnException ret) {
			if (isInitializer)
				return closure.getAt(0, "this");
			return ret.value;
		}

		if (isInitializer) return closure.getAt(0, "this");
		return null;
	}

	@Override
	public String toString() {
		return "<fun " + declaration.name.lexeme + ">";
	}
}