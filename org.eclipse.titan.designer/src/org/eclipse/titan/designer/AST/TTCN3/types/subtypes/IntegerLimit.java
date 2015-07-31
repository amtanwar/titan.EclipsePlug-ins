/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import java.math.BigInteger;

/**
 * @author Adam Delic
 * */
public final class IntegerLimit extends LimitType {
	public static final IntegerLimit MAXIMUM = new IntegerLimit(ValueType.PLUS_INFINITY);
	public static final IntegerLimit MINIMUM = new IntegerLimit(ValueType.MINUS_INFINITY);

	private final ValueType value_type;
	private final BigInteger value;

	public enum ValueType {
		MINUS_INFINITY, NUMBER, PLUS_INFINITY
	}

	public IntegerLimit(final BigInteger value) {
		value_type = ValueType.NUMBER;
		this.value = value;
	}

	public IntegerLimit(final ValueType value_type) {
		this.value_type = value_type;
		value = BigInteger.ZERO;
	}

	@Override
	public Type getType() {
		return Type.INTEGER;
	}

	@Override
	public LimitType decrement() {
		return (value_type == ValueType.NUMBER) ? new IntegerLimit(value.subtract(BigInteger.ONE)) : this;
	}

	@Override
	public LimitType increment() {
		return (value_type == ValueType.NUMBER) ? new IntegerLimit(value.add(BigInteger.ONE)) : this;
	}

	@Override
	public boolean isAdjacent(final LimitType other) {
		final IntegerLimit il = (IntegerLimit) other;
		return ((value_type == ValueType.NUMBER) && (il.value_type == ValueType.NUMBER) && value.add(BigInteger.ONE).equals(il.value));
	}

	@Override
	public void toString(final StringBuilder sb) {
		switch (value_type) {
		case MINUS_INFINITY:
			sb.append("-infinity");
			break;
		case PLUS_INFINITY:
			sb.append("infinity");
			break;
		default:
			sb.append(value.toString());
			break;
		}
	}

	@Override
	public int compareTo(final LimitType other) {
		final IntegerLimit il = (IntegerLimit) other;
		switch (value_type) {
		case MINUS_INFINITY:
			return (il.value_type == ValueType.MINUS_INFINITY) ? 0 : -1;
		case PLUS_INFINITY:
			return (il.value_type == ValueType.PLUS_INFINITY) ? 0 : 1;
		default:
			switch (il.value_type) {
			case MINUS_INFINITY:
				return 1;
			case PLUS_INFINITY:
				return -1;
			default:
				return value.compareTo(il.value);
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof IntegerLimit)) {
			return false;
		}

		final IntegerLimit other = (IntegerLimit) obj;

		return value_type == other.value_type && value.equals(other.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}
