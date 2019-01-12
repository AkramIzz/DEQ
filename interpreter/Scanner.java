package com.interpreter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import static com.interpreter.TokenType.*;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// start marks the beginning of a lexeme
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private char advance() {
		return source.charAt(current++);
	}

	private boolean match(char expected) {
		if (isAtEnd() || source.charAt(current) != expected)
			return false;
		current++;
		return true;
	}

	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}

	private char peekNext() {
		if (current+1 >= source.length()) return '\0';
		return source.charAt(current+1);
	}

	private void scanToken() {
		char c = advance();
		switch (c) {
			case '(': addToken(LEFT_PAREN); break;
			case ')': addToken(RIGHT_PAREN); break;
			case '{': addToken(LEFT_BRACE); break;
			case '}': addToken(RIGHT_BRACE); break;
			case ',': addToken(COMMA); break;
			case '.': addToken(DOT); break;
			case '-': addToken(MINUS); break;
			case '+': addToken(PLUS); break;
			case ';': addToken(SEMICOLON); break;
			case '*': addToken(STAR); break;
			case '?': addToken(QUESTION_MARK); break;
			case ':': addToken(COLON); break;
			case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
			case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
			case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
			case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
			case '/':
				if (match('/')) {
					// A comment goes until the end of the line
					while(peek() != '\n' && !isAtEnd()) advance();
				} else if (match('*')) {
					multilineComment();
				}
				else {
					addToken(SLASH);
				}
				break;

			case ' ': case '\r': case '\t': break; // Ignore whitespace
			case '\n': line++; break;
			
			case '"': string(); break;


			default:
				if (isDigit(c)) number();
				else if (isAlpha(c)) identifier();
				else
					QED.error(line, "Unexpected character.");
		}
	}

	private void identifier() {
		while (isAlphaNumeric(peek())) advance();

		String text = source.substring(start, current);
		
		// see if the identifier is a keyword
		TokenType type = keywords.get(text);
		if (type == null) type = IDENTIFIER;
		addToken(type);
	}

	private void number() {
		while (isDigit(peek())) advance();

		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the dot (.)
			advance();

			while (isDigit(peek())) advance();
		}

		Double value = Double.parseDouble(source.substring(start, current));
		addToken(NUMBER, value);
	}

	private void string() {
		while(peek() != '"' &&  !isAtEnd()) {
			if (peek() == '\n') line++;
			advance();
		}

		// Unterminated string
		if (isAtEnd()) {
			QED.error(line, "Unterminated string.");
			return;
		}

		// Consume the closing qoute (")
		advance();

		// start+1 and current-1 trim the surrounding quotes
		String value = source.substring(start+1, current-1);
		addToken(STRING, value);
	}

	private void multilineComment() {
		while ((peek() != '*' || peekNext() != '/') && !isAtEnd()) {
			if (peek() == '/' && peekNext() == '*') {
				advance(); advance();
				multilineComment();
				continue;
			}
			// **advance** and if it's a new line increment line
			if (advance() == '\n') line++;
		}

		if (isAtEnd()) {
			QED.error(line, "Unterminated block comment");
			return;
		}

		// Consume the star and slash (*/)
		advance(); advance();
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isAlpha(char c) {
		return
			(c >= 'a' && c <= 'z') ||
			(c >= 'A' && c <= 'Z') ||
			(c == '_');
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("break", BREAK);
		keywords.put("class", CLASS);
		keywords.put("continue", CONTINUE);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}
}