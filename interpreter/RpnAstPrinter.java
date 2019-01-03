package com.interpreter;

class RpnAstPrinter implements Expr.Visitor<String> {
	// Main method in class
	// call this method to get a string representation of the expression
	String print(Expr expr) {
		// `accept(this)` Calls the right visit function
		// for the expression type
		return expr.accept(this);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return buildString(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return print(expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
		if (expr.value == null) return "nil";
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
		// to differentiate from the binary subtraction operator
		if (expr.operator.type == TokenType.MINUS)
			return buildString("~", expr.right);
		return buildString(expr.operator.lexeme, expr.right);
	}

	@Override
	public String visitTernaryExpr(Expr.Ternary expr) {
		return buildString("?", expr.condition, expr.onTrue, expr.onFalse);
	}

	String buildString(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();

		for (Expr expr : exprs) {
			builder.append(print(expr));
			builder.append(" ");
		}
		builder.append(name);

		return builder.toString();
	}
}