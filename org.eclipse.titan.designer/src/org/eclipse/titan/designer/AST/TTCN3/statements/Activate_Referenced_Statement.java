/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.types.Altstep_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Activate_Referenced_Statement extends Statement {
	private static final String ALTSTEPEXPECTED = "A value of type altstep was expected in the argument of `derefers()'' instead of `{0}''";
	private static final String RUNONSELFERROR = "the argument of `derefers()' cannot be an altstep reference with 'runs on self' clause";

	private static final String FULLNAMEPART1 = ".referenced";
	private static final String FULLNAMEPART2 = ".<parameters>";
	private static final String STATEMENT_NAME = "activate";

	private final Value dereferedValue;
	private final ParsedActualParameters actualParameterList;

	public Activate_Referenced_Statement(final Value dereferedValue, final ParsedActualParameters actualParameterList) {
		this.dereferedValue = dereferedValue;
		this.actualParameterList = actualParameterList;

		if (dereferedValue != null) {
			dereferedValue.setFullNameParent(this);
		}
		if (actualParameterList != null) {
			actualParameterList.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_ACTIVATE_REFERENCED;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (dereferedValue == child) {
			return builder.append(FULLNAMEPART1);
		} else if (actualParameterList == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (dereferedValue != null) {
			dereferedValue.setMyScope(scope);
		}
		if (actualParameterList != null) {
			actualParameterList.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isErroneous = false;
		lastTimeChecked = timestamp;

		if (dereferedValue == null) {
			setIsErroneous();
			return;
		}

		dereferedValue.setLoweridToReference(timestamp);
		IType type = dereferedValue.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (type != null) {
			type = type.getTypeRefdLast(timestamp);
		}

		if (type == null || type.getIsErroneous(timestamp)) {
			setIsErroneous();
			return;
		}

		if (!Type_type.TYPE_ALTSTEP.equals(type.getTypetype())) {
			dereferedValue.getLocation().reportSemanticError(MessageFormat.format(ALTSTEPEXPECTED, type.getTypename()));
			setIsErroneous();
			return;
		}

		if (((Altstep_Type) type).isRunsOnSelf()) {
			dereferedValue.getLocation().reportSemanticError(RUNONSELFERROR);
			setIsErroneous();
			return;
		}

		if (myStatementBlock != null) {
			myStatementBlock.checkRunsOnScope(timestamp, type, this, STATEMENT_NAME);
		}

		ActualParameterList tempActualParameters = new ActualParameterList();
		FormalParameterList formalParameterList = ((Altstep_Type) type).getFormalParameters();
		if (formalParameterList.checkActualParameterList(timestamp, actualParameterList, tempActualParameters)) {
			setIsErroneous();
			return;
		}

		tempActualParameters.setFullNameParent(this);
		tempActualParameters.setMyScope(getMyScope());
		if (!formalParameterList.checkActivateArgument(timestamp, tempActualParameters, getFullName())) {
			setIsErroneous();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (dereferedValue != null) {
			dereferedValue.updateSyntax(reparser, false);
			reparser.updateLocation(dereferedValue.getLocation());
		}
		if (actualParameterList != null) {
			actualParameterList.updateSyntax(reparser, false);
			reparser.updateLocation(actualParameterList.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (dereferedValue != null) {
			dereferedValue.findReferences(referenceFinder, foundIdentifiers);
		}
		if (actualParameterList != null) {
			actualParameterList.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (dereferedValue != null && !dereferedValue.accept(v)) {
			return false;
		}
		if (actualParameterList != null && !actualParameterList.accept(v)) {
			return false;
		}
		return true;
	}
}
