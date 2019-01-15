package com.interpreter.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(1);
		}
		String outputDir = args[0];
		defineAst(outputDir, "Expr", Arrays.asList(
			// ClassName : Fields
			"Assign   : Token name, Expr value",
			"Binary   : Expr left, Token operator, Expr right",
			// paren is needed for error reporting
			"Call     : Expr callee, Token paren, List<Expr> arguments",
			// This makes for cleaner impl in interpreter than cramming them in
			// Binary expression type, although it's possible
			"Logical  : Expr left, Token operator, Expr right",
			"Grouping : Expr expression",
			"Literal  : Object value",
			"Unary    : Token operator, Expr right",
			"Ternary  : Expr condition, Expr onTrue, Expr onFalse",
			"Variable : Token name"
		));

		defineAst(outputDir, "Stmt", Arrays.asList(
			"Block      : List<Stmt> statements",
			"Expression : Expr expression",
			"Function   : Token name, List<Token> parameters, List<Stmt> body",
			"Class      : Token name, List<Stmt.Function> methods",
			"If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
			"While      : Expr condition, Stmt body",
			"For        : Stmt initializer, Expr condition, Expr increment, Stmt body",
			// ATTENTION: DO NOT delete the space after ':'
			// because in defineAst method we split the string by ':'
			// thus having no space gives a string array with only one element,
			// but two elements string array resulting from this operation is assumed
			"Break      : ",
			"Continue   : ",
			"Print      : List<Expr> expressions",
			"Return     : Token keyword, Expr value",
			"Var        : Token name, Expr initializer"
		));
	}

	private static void defineAst(
			String outputDir, String baseName, List<String> types) 
			throws IOException {
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");

		writer.println("package com.interpreter;");
		writer.println();
		writer.println("import java.util.List;");
		writer.println();
		writer.println("abstract class " + baseName + " {");
		
		defineVisitor(writer, baseName, types);

		// Abstart accept method for visitor pattern
		
		writer.println("    abstract <R> R accept(Visitor<R> visitor);");
		
		// Write each type as a static class in [baseName] class
		for (String type : types) {
			writer.println();
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(writer, baseName, className, fields);
		}

		writer.println("}");
		writer.close();
	}

	// Defines visitor interface
	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("    interface Visitor<R> {");
		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("        R visit" + typeName + baseName + "("
				+ typeName + " " + baseName.toLowerCase() + ");");
		}

		writer.println("    }");
		writer.println();
	}

	private static void defineType(
			PrintWriter writer, String baseName,
			String className, String fields) {
		// defintion of type class
		writer.println("    static class " + className + " extends " + baseName + " {");
		
		// Constructor
		writer.println("        " + className + "(" + fields + ") {");

		// Extract each field's name
		String[] fieldsList = fields.split(", ");
		// if there's no fields as in continue and break statements
		if (fields.isEmpty())
			fieldsList = new String[0];

		// Fields value assignment
		for (String field : fieldsList) {
			// Field contains fieldType and fieldName in a string
			// seperated by a space
			String name = field.split(" ")[1];
			writer.println("            this." + name + " = " + name + ";");
		}
		// End of constructor
		writer.println("        }");
		writer.println();
		
		writer.println("        <R> R accept(Visitor<R> visitor) {");
		writer.println("            return visitor.visit" + className + baseName + "(this);");
		writer.println("        }");
		writer.println();

		// Class fields definition
		for (String field : fieldsList) {
			writer.println("        final " + field + ";");
		}

		// End of class
		writer.println("    }");
	}
}