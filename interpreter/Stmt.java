package com.interpreter;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitClassStmt(Class stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
        R visitBreakStmt(Break stmt);
        R visitContinueStmt(Continue stmt);
        R visitPrintStmt(Print stmt);
        R visitReturnStmt(Return stmt);
        R visitVarStmt(Var stmt);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        final List<Stmt> statements;
    }

    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }

    static class Function extends Stmt {
        Function(Token name, List<Token> parameters, List<Stmt> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }

        final Token name;
        final List<Token> parameters;
        final List<Stmt> body;
    }

    static class Class extends Stmt {
        Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }

        final Token name;
        final Expr.Variable superclass;
        final List<Stmt.Function> methods;
    }

    static class If extends Stmt {
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;
    }

    static class While extends Stmt {
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }

        final Expr condition;
        final Stmt body;
    }

    static class For extends Stmt {
        For(Stmt initializer, Expr condition, Expr increment, Stmt body) {
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }

        final Stmt initializer;
        final Expr condition;
        final Expr increment;
        final Stmt body;
    }

    static class Break extends Stmt {
        Break() {
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
        }

    }

    static class Continue extends Stmt {
        Continue() {
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitContinueStmt(this);
        }

    }

    static class Print extends Stmt {
        Print(List<Expr> expressions) {
            this.expressions = expressions;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        final List<Expr> expressions;
    }

    static class Return extends Stmt {
        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }

        final Token keyword;
        final Expr value;
    }

    static class Var extends Stmt {
        Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }

        final Token name;
        final Expr initializer;
    }
}
