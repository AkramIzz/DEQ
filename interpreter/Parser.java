package com.interpreter;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import static com.interpreter.TokenType.*;

class Parser {
	private static class ParseError extends RuntimeException {}

	private final List<Token> tokens;
	private int current = 0;
	// we need to track if we are in a loop so that
	// we can report a syntax error if the break and continue
	// satements are used outside of a loop
	private int loopDepth = 0;
	// we also need to track if we are in a function so that
	// we can report a syntax error if the return statement
	// is used outside of a function
	private int funDepth = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	public List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while(!isAtEnd()) {
			statements.add(declaration());
		}

		return statements;
	}

	private Stmt declaration() {
		try {
			if (match(VAR)) return varDecl();
			if (match(FUN)) return funDecl();
			if (match(CLASS)) return classDecl();

			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	private Stmt classDecl() {
		Token name = consume(IDENTIFIER, "Expected class name");
		consume(LEFT_BRACE, "Expected '{' after class name");

		List<Stmt.Function> methods = new ArrayList<>();
		while(match(FUN)) {
			methods.add((Stmt.Function)funDecl());
		}

		consume(RIGHT_BRACE, "Expected '}' at the end of class definition");
		return new Stmt.Class(name, methods);
	}

	private Stmt funDecl() {
		Token name = consume(IDENTIFIER, "Expected function name");
		consume(LEFT_PAREN, "Expected '(' after function name");
		
		List<Token> parameters = null;
		if (!match(RIGHT_PAREN)) {
			parameters = parameters();
			consume(RIGHT_PAREN, "Expected ')' after function parameters");
		} else {
			parameters = new ArrayList<>();
		}

		consume(LEFT_BRACE, "Expected '{' before function body");
		++funDepth;
		try {
			List<Stmt> body = ((Stmt.Block)blockStatement()).statements;
			return new Stmt.Function(name, parameters, body);
		} finally {
			// correct the funDepth even if a parsing error occured
			--funDepth;
		}
	}

	private List<Token> parameters() {
		List<Token> parameters = new ArrayList<>();
		do {
			parameters.add(
				consume(IDENTIFIER, "Expected identifier in parameters list")
			);
		} while (match(COMMA));

		return parameters;
	}

	private Stmt varDecl() {
		Token name = consume(IDENTIFIER, "Expected variable name");
		
		Expr initializer = match(EQUAL) ? expression() : null;

		consume(SEMICOLON, "Expected ';' after variable declaration");
		
		return new Stmt.Var(name, initializer);
	}

	private Stmt statement() {
		if (match(PRINT)) return printStatement();
		if (match(IF)) return ifStatement();
		if (match(WHILE)) return whileStatement();
		if (match(FOR)) return forStatement();
		if (match(BREAK)) return breakStatement();
		if (match(CONTINUE)) return continueStatement();
		if (match(LEFT_BRACE)) return blockStatement();
		if (match(RETURN)) return returnStatement();

		return expressionStatement();
	}

	private Stmt ifStatement() {
		consume(LEFT_PAREN, "Expected '(' after if");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expected ')' after if condition");
		Stmt thenBranch = statement();
		Stmt elseBranch = match(ELSE) ? statement() : null;
		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	private Stmt whileStatement() {
		consume(LEFT_PAREN, "Expected '(' after while");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expected ')' after while condition");
		
		++loopDepth;
		try {
			Stmt body = statement();
			return new Stmt.While(condition, body);
		} finally {
			// make sure to correct the loop depth
			// even if a parsing error occurred
			--loopDepth;
		}
	}

	private Stmt forStatement() {
		consume(LEFT_PAREN, "Expected '(' after for");

		Stmt initializer = null;
		if (!match(SEMICOLON)) {
			if (match(VAR)) initializer = varDecl();
			else initializer = expressionStatement();
		}

		// if the condition isn't specified, it defaults to true
		Expr condition = new Expr.Literal(true);
		if (!check(SEMICOLON)) {
			condition = expression();
		}
		consume(SEMICOLON, "Expected ';' after for condition");

		Expr increment = null;
		if (!check(RIGHT_PAREN)) {
			increment = expression();
		}
		consume(RIGHT_PAREN, "Expected ')' after for clause");

		++loopDepth;
		try {
			Stmt body = statement();
			return new Stmt.For(initializer, condition, increment, body);
		} finally {
			// make sure to correct the loop depth
			// even if a parsing error occurred
			--loopDepth;
		}
	}

	private Stmt breakStatement() {
		if (loopDepth == 0) {
			error(previous(), "break can't be used outside of a loop");
		}
		consume(SEMICOLON, "Expected ';' after 'break'");
		return new Stmt.Break();
	}

	private Stmt continueStatement() {
		if (loopDepth == 0) {
			error(previous(), "continue can't be used outside of a loop");
		}
		consume(SEMICOLON, "Expected ';' after 'continue'");
		return new Stmt.Continue();
	}

	private Stmt blockStatement() {
		List<Stmt> statements = new ArrayList<>();
		while(!check(RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "Expected '}' after block");

		return new Stmt.Block(statements);
	}

	private Stmt returnStatement() {
		if (funDepth == 0) {
			error(previous(), "return can't be used outside of a function");
		}

		Token keyword = previous();
		Expr value = check(SEMICOLON) ? null : expression();
		consume(SEMICOLON, "Expected ';' after return value");
		return new Stmt.Return(keyword, value);
	}

	private Stmt printStatement() {
		List<Expr> exprs = new ArrayList<>();
		// we don't want to accept comma operator,
		// because comma in print statement is used to seperate values
		// that must be printed, and so we parse the next expression in precedence
		// and handle the comma token seperately from the comma operator.
		exprs.add(assignment());

		while(match(COMMA)) {
			exprs.add(assignment());
		}

		consume(SEMICOLON, "Expected ';' at the end of print statement");
		return new Stmt.Print(exprs);
	}

	private Stmt expressionStatement() {
		Expr expr = expression();
		consume(SEMICOLON, "Expected ';' after expression");
		return new Stmt.Expression(expr);
	}

	private Expr expression() {
		return comma();
	}

	private Expr comma() {
		Expr expr = assignment();

		while (match(COMMA)) {
			Token operator = previous();
			Expr right = assignment();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr assignment() {
		Expr expr = ternary();

		if (match(EQUAL)) {
			Token equals = previous();
			Expr rvalue = ternary();
			if (expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable)expr).name;
				return new Expr.Assign(name, rvalue);
			}

			// We don't need to throw the error because the parser
			// isn't in a confused state and doesn't need to be
			// synchronized. It's in a state where it can continue parsing
			error(equals, "Can't assign to non variable");
		}

		return expr;
	}

	private Expr ternary() {
		Expr expr = or();

		if (match(QUESTION_MARK)) {
			Expr onTrue = ternary();
			consume(COLON, "Expected ':' after then branch of ternary operator");
			Expr onFalse = ternary();
			expr = new Expr.Ternary(expr, onTrue, onFalse);
		}

		return expr;
	}

	private Expr or() {
		Expr expr = and();

		while (match(OR)) {
			Token operator = previous();
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr and() {
		Expr expr = equality();

		while (match(AND)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Logical(expr, operator, right);
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

		return call();
	}

	private Expr call() {
		Expr expr = primary();

		while (match(LEFT_PAREN)) {
			Token paren = previous();

			if (match(RIGHT_PAREN)) {
				expr = new Expr.Call(expr, paren, new ArrayList<>());
			} else {
				List<Expr> args = arguments();
				expr = new Expr.Call(expr, paren, args);
				consume(RIGHT_PAREN, "Expected ')' at the end of function arguments");
			}
		}

		return expr;
	}

	private List<Expr> arguments() {
		List<Expr> args = new ArrayList<>();
		do {
			args.add(assignment());
		} while(match(COMMA));
		
		return args;
	}

	private Expr primary() {
		if (match(TRUE)) return new Expr.Literal(true);
		if (match(FALSE)) return new Expr.Literal(false);
		if (match(NIL)) return new Expr.Literal(null);

		if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);

		if (match(IDENTIFIER)) return new Expr.Variable(previous());

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
		QED.error(token, message);
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