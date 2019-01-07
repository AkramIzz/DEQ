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
			"Binary   : Expr left, Token operator, Expr right",
			"Grouping : Expr expression",
			"Literal  : Object value",
			"Unary    : Token operator, Expr right",
			"Ternary  : Expr condition, Expr onTrue, Expr onFalse"
		));

		defineAst(outputDir, "Stmt", Arrays.asList(
			"Expression : Expr expression",
			"Print      : Expr expression"
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