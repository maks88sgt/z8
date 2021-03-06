package org.zenframework.z8.compiler.parser.statements;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IJavaTypeCast;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.DeclaratorNestedType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Statement extends LanguageElement implements IStatement {
	private ILanguageElement expression;

	public Statement(ILanguageElement expression) {
		this.expression = expression;
	}

	@Override
	public IPosition getSourceRange() {
		return expression.getSourceRange();
	}

	@Override
	public IToken getFirstToken() {
		return expression.getFirstToken();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		return super.resolveTypes(compilationUnit, declaringType) && expression.resolveTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		return super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null) && expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
	}

	@Override
	public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
		return super.resolveNestedTypes(compilationUnit, declaringType) && expression.resolveNestedTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean returnsOnAllControlPaths() {
		return false;
	}

	@Override
	public boolean breaksControlFlow() {
		return false;
	}

	@Override
	public void getClassCode(CodeGenerator codeGenerator) {
		expression.getClassCode(codeGenerator);
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		codeGenerator.getCompilationUnit().setLineNumbers(getSourceRange().getLine(), codeGenerator.getCurrentLine());

		if(expression instanceof IJavaTypeCast) {
			IJavaTypeCast javaTypeCast = (IJavaTypeCast)expression;
			javaTypeCast.setCastPending(false);
		}

		expression.getCode(codeGenerator);

		if(!(expression instanceof DeclaratorNestedType)) {
			codeGenerator.append(";");
			codeGenerator.breakLine();
		}
	}
}
