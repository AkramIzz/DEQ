package com.interpreter;

import java.util.List;

class Function implements Callable {
	private final Stmt.Function declaration;

	Function(Stmt.Function declaration) {
		this.declaration = declaration;
	}

	@Override
	public int arity() {
		return declaration.parameters.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment env = new Environment(interpreter.globals);
		for (int i = 0; i < arguments.size(); ++i) {
			env.define(
				declaration.parameters.get(i).lexeme,
				arguments.get(i)
			);
		}

		try {
			interpreter.executeBlock(declaration.body, env);
		} catch (ReturnException ret) {
			return ret.value;
		}
		return null;
	}

	@Override
	public String toString() {
		return "<fun " + declaration.name.lexeme + ">";
	}
}