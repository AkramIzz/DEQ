package com.interpreter;

import static com.interpreter.TokenType.*;

class Interpreter implements Expr.Visitor<Object> {
	public Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
			case PLUS:
				if (left instanceof Double && right instanceof Double)
					return (double)left + (double)right;
				if (left instanceof String && right instanceof String)
					return (String)left + (String)right;
			case MINUS:
				return (double)left - (double)right;
			case STAR:
				return (double)left * (double)right;
			case SLASH:
				return (double)left / (double)right;

			case GREATER:
				return (double)left > (double)right;
			case GREATER_EQUAL:
				return (double)left >= (double)right;
			case LESS:
				return (double)left < (double)right;
			case LESS_EQUAL:
				return (double)left <= (double)right;

			case EQUAL_EQUAL:
				return isEqual(left, right);
			case BANG_EQUAL:
				return !isEqual(left, right);
		}

		// unreachable
		return null;
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary expr) {
		Object right = evaluate(expr.right);
		if (expr.operator.type == MINUS)
			return -(double)(right);
		if (expr.operator.type == BANG)
			return !isTruthy(right);

		// unreachable
		return null;
	}

	@Override
	public Object visitTernaryExpr(Expr.Ternary expr) {
		if (isTruthy(evaluate(expr.condition)))
			return evaluate(expr.onTrue);

		return evaluate(expr.onFalse);
	}

	private boolean isTruthy(Object test) {
		if (test == null) return false;
		if (test instanceof Boolean) return (boolean)test;

		return true;
	}

	private boolean isEqual(Object a, Object b) {
		if (a == null && b == null) return true;
		// If they aren't both null ^ but one of them is
		// then they aren't equal
		if (a == null || b == null) return false;

		return a.equals(b);
	}
}