package com.interpreter;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitArrayExpr(Array expr);
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGetExpr(Get expr);
        R visitSetExpr(Set expr);
        R visitArrayGetExpr(ArrayGet expr);
        R visitArraySetExpr(ArraySet expr);
        R visitThisExpr(This expr);
        R visitSuperExpr(Super expr);
        R visitLogicalExpr(Logical expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
        R visitTernaryExpr(Ternary expr);
        R visitVariableExpr(Variable expr);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Array extends Expr {
        Array(List<Expr> values) {
            this.values = values;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayExpr(this);
        }

        final List<Expr> values;
    }

    static class Assign extends Expr {
        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        final Token name;
        final Expr value;
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        final Expr callee;
        final Token paren;
        final List<Expr> arguments;
    }

    static class Get extends Expr {
        Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }

        final Expr object;
        final Token name;
    }

    static class Set extends Expr {
        Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }

        final Expr object;
        final Token name;
        final Expr value;
    }

    static class ArrayGet extends Expr {
        ArrayGet(Expr array, Token bracket, Expr index) {
            this.array = array;
            this.bracket = bracket;
            this.index = index;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }

        final Expr array;
        final Token bracket;
        final Expr index;
    }

    static class ArraySet extends Expr {
        ArraySet(Expr array, Token bracket, Expr index, Expr value) {
            this.array = array;
            this.bracket = bracket;
            this.index = index;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }

        final Expr array;
        final Token bracket;
        final Expr index;
        final Expr value;
    }

    static class This extends Expr {
        This(Token keyword) {
            this.keyword = keyword;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }

        final Token keyword;
    }

    static class Super extends Expr {
        Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }

        final Token keyword;
        final Token method;
    }

    static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
    }

    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }

    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }

    static class Ternary extends Expr {
        Ternary(Expr condition, Expr onTrue, Expr onFalse) {
            this.condition = condition;
            this.onTrue = onTrue;
            this.onFalse = onFalse;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }

        final Expr condition;
        final Expr onTrue;
        final Expr onFalse;
    }

    static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }

        final Token name;
    }
}
