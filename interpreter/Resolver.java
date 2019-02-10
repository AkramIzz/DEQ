package com.interpreter;

import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
	private final Interpreter interpreter;
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();
	private FunctionType currentFunction = FunctionType.NONE;
	private ClassType currentClass = ClassType.NONE;

	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	private enum FunctionType {
		NONE, FUNCTION, METHOD, INITIALIZER
	}

	private enum ClassType {
		NONE, CLASS, SUBCLASS
	}

	void resolve(List<Stmt> statements) {
		for (Stmt statement : statements) {
			resolve(statement);
		}
	}

	private void resolveExprs(List<Expr> expressions) {
		for (Expr expr : expressions) {
			resolve(expr);
		}
	}

	private void resolve(Stmt stmt) {
		stmt.accept(this);
	}

	private void resolve(Expr expr) {
		expr.accept(this);
	}

	private void beginScope() {
		scopes.push(new HashMap<String, Boolean>());
	}

	private void endScope() {
		scopes.pop();
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		beginScope();
		resolve(stmt.statements);
		endScope();
		return null;
	}

	@Override
	public Void visitClassStmt(Stmt.Class stmt) {
		declare(stmt.name);
		define(stmt.name);
		boolean isSubclass = stmt.superclass != null;

		if (isSubclass)
			resolve(stmt.superclass);
		
		if (isSubclass) {
			beginScope();
			scopes.peek().put("super", true);
		}
		beginScope();
		scopes.peek().put("this", true);
		
		ClassType previousClass = currentClass;
		if (isSubclass) {
			currentClass = ClassType.SUBCLASS;
		} else {
			currentClass = ClassType.CLASS;
		}

		for (Stmt.Function method : stmt.methods) {
			FunctionType type = FunctionType.METHOD;
			if (method.name.lexeme.equals("init"))
				type = FunctionType.INITIALIZER;

			resolveFunction(method, type);
		}
		
		currentClass = previousClass;

		endScope();
		if (isSubclass) endScope();
		return null;
	}

	@Override
	public Void visitFunctionStmt(Stmt.Function stmt) {
		declare(stmt.name);
		define(stmt.name);
		resolveFunction(stmt, FunctionType.FUNCTION);
		return null;
	}

	private void resolveFunction(Stmt.Function function, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;

		beginScope();
		for (Token param : function.parameters) {
			declare(param);
			define(param);
		}
		resolve(function.body);
		endScope();

		currentFunction = enclosingFunction;
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		declare(stmt.name);
		if (stmt.initializer != null) {
			resolve(stmt.initializer);
		}
		define(stmt.name);
		return null;
	}
	
	@Override
	public Void visitVariableExpr(Expr.Variable expr) {
		if (!scopes.isEmpty()
				&& scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
			QED.error(expr.name, "Cannot read local variable in its own initializer");
		}

		resolveLocal(expr, expr.name);
		return null;
	}

	@Override
	public Void visitThisExpr(Expr.This expr) {
		if (currentClass == ClassType.NONE) {
			QED.error(expr.keyword, "'this' can't be used outside of a class method");
			return null;
		}
		resolveLocal(expr, expr.keyword);
		return null;
	}

	@Override
	public Void visitSuperExpr(Expr.Super expr) {
		if (currentClass != ClassType.SUBCLASS) {
			QED.error(expr.keyword, "'super' can't be used outside of a subclass method");
			return null;
		}
		resolveLocal(expr, expr.keyword);
		return null;
	}

	@Override
	public Void visitAssignExpr(Expr.Assign expr) {
		resolve(expr.value);
		resolveLocal(expr, expr.name);
		return null;
	}

	private void resolveLocal(Expr expr, Token name) {
		for (int i = scopes.size() - 1; i >= 0; --i) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				interpreter.resolve(expr, scopes.size() - 1 - i);
				return;
			}
		}

		// assume global
		return;
	}

	private void declare(Token name) {
		if (scopes.isEmpty()) return;

		Map<String, Boolean> scope = scopes.peek();
		if (scope.containsKey(name.lexeme)) {
			QED.error(name, "Variable " + name.lexeme + " already declared in this scope");
		}
		scope.put(name.lexeme, false);
	}

	private void define(Token name) {
		if (scopes.isEmpty()) return;
		
		Map<String, Boolean> scope = scopes.peek();
		scope.put(name.lexeme, true);
	}

	@Override
	public Void visitBreakStmt(Stmt.Break stmt) {
		return null;
	}

	@Override
	public Void visitContinueStmt(Stmt.Continue stmt) {
		return null;
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitForStmt(Stmt.For stmt) {
		if (stmt.initializer != null) resolve(stmt.initializer);
		if (stmt.condition != null) resolve(stmt.condition);
		if (stmt.increment != null) resolve(stmt.increment);
		resolve(stmt.body);
		return null;
	}

	@Override
	public Void visitIfStmt(Stmt.If stmt) {
		resolve(stmt.condition);
		resolve(stmt.thenBranch);
		if (stmt.elseBranch != null) resolve(stmt.elseBranch);
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt) {
		resolveExprs(stmt.expressions);
		return null;
	}

	@Override
	public Void visitReturnStmt(Stmt.Return stmt) {
		if (currentFunction == FunctionType.NONE) {
			QED.error(stmt.keyword, "'return' can't be used outside of a function");
		}

		if (stmt.value != null) {
			if (currentFunction == FunctionType.INITIALIZER) {
				QED.error(stmt.keyword, "'return' with value can't be used in a method");
			}

			resolve(stmt.value);
		}
		
		return null;
	}

	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		resolve(stmt.condition);
		resolve(stmt.body);
		return null;
	}

	@Override
	public Void visitArrayExpr(Expr.Array expr) {
		for (Expr element : expr.values) {
			resolve(element);
		}
		return null;
	}

	@Override
	public Void visitBinaryExpr(Expr.Binary expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitCallExpr(Expr.Call expr) {
		resolve(expr.callee);
		resolveExprs(expr.arguments);
		return null;
	}

	@Override
	public Void visitGetExpr(Expr.Get expr) {
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitArrayGetExpr(Expr.ArrayGet expr) {
		resolve(expr.array);
		resolve(expr.index);
		return null;
	}

	@Override
	public Void visitGroupingExpr(Expr.Grouping expr) {
		resolve(expr.expression);
		return null;
	}

	@Override
	public Void visitLiteralExpr(Expr.Literal expr) {
		return null;
	}

	@Override
	public Void visitLogicalExpr(Expr.Logical expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitSetExpr(Expr.Set expr) {
		resolve(expr.value);
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitArraySetExpr(Expr.ArraySet expr) {
		resolve(expr.value);
		resolve(expr.array);
		resolve(expr.index);
		return null;
	}

	@Override
	public Void visitTernaryExpr(Expr.Ternary expr) {
		resolve(expr.condition);
		resolve(expr.onTrue);
		resolve(expr.onFalse);
		return null;
	}

	@Override
	public Void visitUnaryExpr(Expr.Unary expr) {
		resolve(expr.right);
		return null;
	}
}