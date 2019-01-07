package com.interpreter;

import static com.interpreter.TokenType.*;

class Interpreter implements Expr.Visitor<Object> {
	public void interpret(Expr expr) {
		try {
			Object value = evaluate(expr);
			System.out.println(stringify(value));
		} catch(RuntimeError error) {
			QED.runtimeError(error);
		}
	}

	private Object evaluate(Expr expr) {
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
				throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
			case MINUS:
				checkNumberOperands(expr.operator, left, right);
				return (double)left - (double)right;
			case STAR:
				checkNumberOperands(expr.operator, left, right);
				return (double)left * (double)right;
			case SLASH:
				checkNumberOperands(expr.operator, left, right);
				return (double)left / (double)right;
			case GREATER:
				if (left instanceof Double && right instanceof Double)
					return (double)left > (double)right;
				if (left instanceof String && right instanceof String)
					return ((String)left).compareTo((String)right) > 0;
				throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
			case GREATER_EQUAL:
				if (left instanceof Double && right instanceof Double)
					return (double)left >= (double)right;
				if (left instanceof String && right instanceof String)
					return ((String)left).compareTo((String)right) >= 0;
				throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
			case LESS:
				if (left instanceof Double && right instanceof Double)
					return (double)left < (double)right;
				if (left instanceof String && right instanceof String)
					return ((String)left).compareTo((String)right) < 0;
				throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
			case LESS_EQUAL:
				if (left instanceof Double && right instanceof Double)
					return (double)left <= (double)right;
				if (left instanceof String && right instanceof String)
					return ((String)left).compareTo((String)right) <= 0;
				throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");

			case EQUAL_EQUAL:
				return isEqual(left, right);
			case BANG_EQUAL:
				return !isEqual(left, right);

			case COMMA:
				return right;
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
		if (expr.operator.type == MINUS) {
			checkNumberOperand(expr.operator, right);
			return -(double)(right);
		}
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

	private String stringify(Object object) {
		if (object == null) return "nil";

		// Work around Java adding ".0" to integer-valued doubles
		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0"))
				text = text.substring(0, text.length() - 2);
			return text;
		}

		return object.toString();
	}

	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number");
	}

	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;
		throw new RuntimeError(operator, "Operands must be numbers");
	}
}