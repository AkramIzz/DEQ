package com.interpreter;

import java.util.List;

class RpnAstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
	// Main method in class
	// call this method to get a string representation of program statements
	String println(List<Stmt> statements) {
		StringBuilder builder = new StringBuilder();
		for (Stmt stmt : statements) {
			builder.append(printStmt(stmt));
			builder.append("\n");
		}
		return builder.toString();
	}

	String printStmt(Stmt statement) {
		// `accept(this)` Calls the right visit function
		// for the statement type
		return statement.accept(this);
	}

	String printExpr(Expr expr) {
		// `accept(this)` Calls the right visit function
		// for the expression type
		return expr.accept(this);
	}

	@Override
	public String visitPrintStmt(Stmt.Print statement) {
		StringBuilder builder = new StringBuilder();

		for (Expr expr : statement.expressions) {
			builder.append(printExpr(expr));
			builder.append(" ");
		}
		builder.append("print");

		return builder.toString();
	}

	@Override
	public String visitVarStmt(Stmt.Var statement) {
		if (statement.initializer != null)
			return buildString("var " + statement.name.lexeme, statement.initializer);
		return "var " + statement.name.lexeme;
	}

	@Override
	public String visitExpressionStmt(Stmt.Expression statement) {
		return printExpr(statement.expression);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return buildString(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return printExpr(expr.expression);
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

	@Override
	public String visitVariableExpr(Expr.Variable expr) {
		return expr.name.lexeme;
	}

	String buildString(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();

		for (Expr expr : exprs) {
			builder.append(printExpr(expr));
			builder.append(" ");
		}
		builder.append(name);

		return builder.toString();
	}
}