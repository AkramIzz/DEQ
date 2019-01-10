package com.interpreter;

import java.util.List;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
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
	public String visitBlockStmt(Stmt.Block statement) {
		StringBuilder builder = new StringBuilder();

		builder.append("(");
		builder.append("block\n");
		for (Stmt stmt : statement.statements) {
			builder.append("  "); // indentation
			// get string representation of [stmt]
			builder.append(printStmt(stmt));
			builder.append("\n"); // statements seperator
		}
		builder.append(")");

		return builder.toString();
	}

	@Override
	public String visitIfStmt(Stmt.If statement) {
		String ifStmt = "(if " + printExpr(statement.condition)
			+ " " + printStmt(statement.thenBranch);
		if (statement.elseBranch != null)
			ifStmt = ifStmt + " " + printStmt(statement.elseBranch);
		return ifStmt + ")";
	}

	@Override
	public String visitWhileStmt(Stmt.While statement) {
		return "(while " + printExpr(statement.condition)
			+ " " + printStmt(statement.body) + ")";
	}

	@Override
	public String visitPrintStmt(Stmt.Print statement) {
		StringBuilder builder = new StringBuilder();

		builder.append("(");
		builder.append("print");
		for (Expr expr : statement.expressions) {
			builder.append(" ");
			// get string representation of [expr]
			builder.append(printExpr(expr));
		}
		builder.append(")");

		return builder.toString();
	}

	@Override
	public String visitVarStmt(Stmt.Var statement) {
		if (statement.initializer != null)
			return parenthesize("var " + statement.name.lexeme, statement.initializer);
		return "var " + statement.name.lexeme;
	}

	@Override
	public String visitExpressionStmt(Stmt.Expression statement) {
		return printExpr(statement.expression);
	}

	@Override
	public String visitAssignExpr(Expr.Assign expr) {
		return parenthesize(expr.name.lexeme, expr.value);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitLogicalExpr(Expr.Logical expr) {
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

	@Override
	public String visitTernaryExpr(Expr.Ternary expr) {
		return parenthesize("?", expr.condition, expr.onTrue, expr.onFalse);
	}

	@Override
	public String visitVariableExpr(Expr.Variable expr) {
		return expr.name.lexeme;
	}

	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();

		builder.append("(");
		builder.append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			// get string representation of [expr]
			builder.append(printExpr(expr));
		}
		builder.append(")");

		return builder.toString();
	}
}