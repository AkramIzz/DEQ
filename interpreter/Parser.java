package com.interpreter;

import java.util.List;

import static com.interpreter.TokenType.*;

class Parser {
	private static class ParseError extends RuntimeException {}

	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	public Expr parse() {
		try {
			return expression();
		} catch(ParseError e) {
			return null;
		}
	}

	private Expr expression() {
		return comma();
	}

	private Expr comma() {
		Expr expr = ternary();

		while (match(COMMA)) {
			Token operator = previous();
			Expr right = ternary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr ternary() {
		Expr expr = equality();

		if (match(QUESTION_MARK)) {
			Expr onTrue = ternary();
			consume(COLON, "Expected ':' after expression");
			Expr onFalse = ternary();
			expr = new Expr.Ternary(expr, onTrue, onFalse);
		}

		return expr;
	}

	private Expr equality() {
		Expr expr = comparison();

		while (match(EQUAL_EQUAL, BANG_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr comparison() {
		Expr expr = addition();

		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = previous();
			Expr right = addition();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr addition() {
		Expr expr = multiplication();

		while (match(PLUS, MINUS)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr multiplication() {
		Expr expr = unary();

		while (match(STAR, SLASH)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}

		return primary();
	}

	private Expr primary() {
		if (match(TRUE)) return new Expr.Literal(true);
		if (match(FALSE)) return new Expr.Literal(false);
		if (match(NIL)) return new Expr.Literal(null);

		if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);

		if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "Expected ')' after expression");
			return expr;
		}

		throw error(peek(), "Expected an expression");
	}

	private Token consume(TokenType type, String message) {
		if (check(type)) return advance();

		throw error(peek(), message);
	}

	private ParseError error(Token token, String message) {
		Interpreter.error(token, message);
		return new ParseError();
	}

	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			if (previous().type == SEMICOLON) return;

			switch (peek().type) {
				case CLASS:
				case FUN:
				case VAR:
				case FOR:
				case IF:
				case WHILE:
				case PRINT:
				case RETURN:
					return;
			}

			advance();
		}
	}

	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}

	private boolean check(TokenType type) {
		if (isAtEnd()) return false;
		return peek().type == type;
	}

	private Token advance() {
		if (!isAtEnd()) ++current;
		return previous();
	}

	private Boolean isAtEnd() {
		return peek().type == EOF;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}
}