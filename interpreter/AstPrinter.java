package com.interpreter;

class AstPrinter implements Expr.Visitor<String> {
	// Main method in class
	// call this method to get a string representation of the expression
	String print(Expr expr) {
		// `accept(this)` Calls the right visit function
		// for the expression type
		return expr.accept(this);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return parenthesize("group", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
		// no need for parenthesize; a literal has only the value inside it.
		if (expr.value == null) return "nil";
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
		return parenthesize(expr.operator.lexeme, expr.right);
	}

	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();

		builder.append("(");
		builder.append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			// get string representation of [expr]
			builder.append(print(expr));
		}
		builder.append(")");

		return builder.toString();
	}
}