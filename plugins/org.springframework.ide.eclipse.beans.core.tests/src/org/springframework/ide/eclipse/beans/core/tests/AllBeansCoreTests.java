/*******************************************************************************
 * Copyright (c) 2005, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.ide.core.classreading.tests.JdtAnnotationMetadataTest;
import org.springframework.ide.core.classreading.tests.JdtBasedAnnotationMetadataTest;
import org.springframework.ide.core.classreading.tests.JdtClassMetadataTest;
import org.springframework.ide.eclipse.beans.core.autowire.AutowiredAnnotationInjectionMetadataProviderTests;
import org.springframework.ide.eclipse.beans.core.autowire.CommonAnnotationInjectionMetadataProviderTests;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanClassRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanConstructorArgumentRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanInitDestroyMethodRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanPropertyRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.NamespaceElementsRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.RequiredPropertyRuleTest;
import org.springframework.ide.eclipse.core.java.IntrospectorTest;

/**
 * Test suite for <code>beans.core</code> plugin.
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @author Martin Lippert
 * @since 2.0.3
 */
@RunWith(Suite.class)
@SuiteClasses({
	BeansCoreUtilsTest.class,
	BeanClassRuleTest.class,
	BeanConstructorArgumentRuleTest.class,
	BeanPropertyRuleTest.class,
	BeanInitDestroyMethodRuleTest.class,
	RequiredPropertyRuleTest.class,
	NamespaceElementsRuleTest.class,
	IntrospectorTest.class,
	AutowiredAnnotationInjectionMetadataProviderTests.class,
	CommonAnnotationInjectionMetadataProviderTests.class,
	JdtAnnotationMetadataTest.class,
	JdtBasedAnnotationMetadataTest.class,
	JdtClassMetadataTest.class
})
public class AllBeansCoreTests {
	// goofy junit4, no class body needed
}
