/*
 * Copyright (c) 2014, vir
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.jstach.apt.internal.context;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author vir
 */
class BooleanRenderingContext implements BooleanExpressionContext {

	private final String expression;

	private final RenderingContext parent;

	BooleanRenderingContext(String expression, RenderingContext parent) {
		this.expression = expression;
		this.parent = parent;
	}

	@Override
	public String beginSectionRenderingCode() {
		return parent.beginSectionRenderingCode() + "if (" + expression + ") { ";
	}

	@Override
	public String endSectionRenderingCode() {
		return " }" + parent.endSectionRenderingCode();
	}

	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public @Nullable BooleanExpressionContext getParentExpression() {
		if (parent instanceof BooleanExpressionContext be) {
			return be;
		}
		return null;
	}

	@Override
	public @Nullable JavaExpression get(String name) throws ContextException {
		return parent.get(name);
	}

	@Override
	public JavaExpression currentExpression() {
		return parent.currentExpression();
	}

	@Override
	public VariableContext createEnclosedVariableContext() {
		return parent.createEnclosedVariableContext();
	}

	@Override
	public @Nullable RenderingContext getParent() {
		return parent;
	}

	@Override
	public String description() {
		return toString();
	}

	@Override
	public String toString() {
		return "BooleanRenderingContext [expression=" + expression + "]";
	}

}
