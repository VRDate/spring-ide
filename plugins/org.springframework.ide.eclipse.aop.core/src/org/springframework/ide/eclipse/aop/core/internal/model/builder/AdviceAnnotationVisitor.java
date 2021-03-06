/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.ide.eclipse.aop.core.internal.model.AnnotationAspectDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.AnnotationIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.util.StringUtils;

/**
 * ASM-based Visitor that collects all <code>@AspectJ<code>-style annotations
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class AdviceAnnotationVisitor extends EmptyVisitor {

	private static final String BEFORE_ANNOTATION_DESC = Type
			.getDescriptor(Before.class);

	private static final String AFTER_ANNOTATION_DESC = Type
			.getDescriptor(After.class);

	private static final String AFTERRETURNING_ANNOTATION_DESC = Type
			.getDescriptor(AfterReturning.class);

	private static final String AFTERTHROWING_ANNOTATION_DESC = Type
			.getDescriptor(AfterThrowing.class);

	private static final String AROUND_ANNOTATION_DESC = Type
			.getDescriptor(Around.class);

	private static final String DECLARE_PARENTS_ANNOTATION_DESC = Type
			.getDescriptor(DeclareParents.class);

	private static Map<String, ADVICE_TYPE> ANNOTATION_TYPES = null;

	static {
		ANNOTATION_TYPES = new HashMap<String, ADVICE_TYPE>();
		ANNOTATION_TYPES.put(BEFORE_ANNOTATION_DESC, ADVICE_TYPE.BEFORE);
		ANNOTATION_TYPES.put(AFTER_ANNOTATION_DESC, ADVICE_TYPE.AFTER);
		ANNOTATION_TYPES.put(AFTERRETURNING_ANNOTATION_DESC,
				ADVICE_TYPE.AFTER_RETURNING);
		ANNOTATION_TYPES.put(AFTERTHROWING_ANNOTATION_DESC,
				ADVICE_TYPE.AFTER_THROWING);
		ANNOTATION_TYPES.put(AROUND_ANNOTATION_DESC, ADVICE_TYPE.AROUND);
		ANNOTATION_TYPES.put(DECLARE_PARENTS_ANNOTATION_DESC,
				ADVICE_TYPE.DECLARE_PARENTS);
	}

	private String visitedMethod = null;

	private String visitedField = null;

	private String visitedFieldType = null;

	private List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();

	private IAspectDefinition lastAspectDefinition = null;

	private int aspectStartLineNumber;

	private int aspectEndLineNumber;

	private String aspectName;

	private String aspectClassName;

	public AdviceAnnotationVisitor(String aspectName,
			String aspectClassName, int aspectStartLineNumber, int aspectEndLineNumber) {
		this.aspectStartLineNumber = aspectStartLineNumber;
		this.aspectEndLineNumber = aspectEndLineNumber;
		this.aspectName = aspectName;
		this.aspectClassName = aspectClassName;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (visitedMethod != null && visible
				&& ANNOTATION_TYPES.containsKey(desc)) {

			AnnotationAspectDefinition def = new AnnotationAspectDefinition();
			aspectDefinitions.add(def);
			lastAspectDefinition = def;

			def.setAspectClassName(aspectClassName);
			def.setAdviceMethodName(visitedMethod);
			def.setAspectStartLineNumber(aspectStartLineNumber);
			def.setAspectEndLineNumber(aspectEndLineNumber);
			def.setAspectName(aspectName);
			def.setType(ANNOTATION_TYPES.get(desc));

			visitedMethod = null;
			visitedField = null;
			return this;
		}
		else if (visitedField != null && visible
				&& ANNOTATION_TYPES.containsKey(desc)) {
			AnnotationIntroductionDefinition def = new AnnotationIntroductionDefinition();
			aspectDefinitions.add(def);
			lastAspectDefinition = def;

			def.setDefiningField(visitedField);
			def.setIntroducedInterfaceName(visitedFieldType);

			def.setAspectClassName(aspectClassName);
			def.setAspectName(aspectName);
			def.setAspectStartLineNumber(aspectStartLineNumber);
			def.setAspectEndLineNumber(aspectEndLineNumber);

			visitedMethod = null;
			visitedField = null;
			return this;
		}
		return new EmptyVisitor();
	}

	@Override
	public void visit(String name, Object value) {
		if (lastAspectDefinition != null) {
			if (lastAspectDefinition instanceof AnnotationAspectDefinition) {
				if ("value".equals(name) || "pointcut".equals(name)) {
					((AnnotationAspectDefinition) lastAspectDefinition)
							.setPointcutExpression(value.toString());
				}
				else if ("argNames".equals(name)) {
					((AnnotationAspectDefinition) lastAspectDefinition).setArgNames(StringUtils
							.commaDelimitedListToStringArray(value.toString()));
				}
				else if ("returning".equals(name)) {
					((AnnotationAspectDefinition) lastAspectDefinition)
						.setReturning(value.toString());
				}
				else if ("throwing".equals(name)) {
					((AnnotationAspectDefinition) lastAspectDefinition)
						.setThrowing(value.toString());
				}
			}
			else if (lastAspectDefinition instanceof AnnotationIntroductionDefinition) {
				if ("defaultImpl".equals(name)) {
					((AnnotationIntroductionDefinition) lastAspectDefinition)
							.setDefaultImplName(value.toString());
				}
				else if ("value".equals(name)) {
					((AnnotationIntroductionDefinition) lastAspectDefinition)
							.setTypePattern(value.toString());
				}
			}
		}
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		visitedField = name;
		visitedFieldType = Type.getType(desc).getClassName();
		visitedMethod = null;
		return this;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		Type[] types = Type.getArgumentTypes(desc);
		StringBuffer buf = new StringBuffer(name);
		if (types != null && types.length > 0) {
			buf.append("(");
			for (int i = 0; i < types.length; i++) {
				Type type = types[i];
				buf.append(type.getClassName());
				if (i < (types.length - 1)) {
					buf.append(", ");
				}
			}
			buf.append(")");
		}
		visitedMethod = buf.toString();
		visitedField = null;
		lastAspectDefinition = null;
		return this;
	}

	public List<IAspectDefinition> getAspectDefinitions() {
		return aspectDefinitions;
	}

}
