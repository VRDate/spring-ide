/*******************************************************************************
 * Copyright (c) 2005, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;

/**
 * Unit test for {@link Introspector}.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0.3
 */
public class IntrospectorTest extends BeansCoreTestCase {

	/**
	 * Several tests dealing with intertype declared methods on the bean class
	 */
	@Test
	public void testDeclaredMethods() throws CoreException, IOException {
		Assume.assumeTrue(AjdtUtils.isJdtWeavingPresent());
		IProject project = createPredefinedProject("aspectj");
		IType foo = JdtUtils.getJavaType(project, "org.springframework.Foo");
		Set<IMethod> methods = Introspector.findAllWritableProperties(foo);
		assertTrue(methods.size() > 1);
		assertTrue(Introspector.hasWritableProperty(foo, "bar"));
		assertNotNull(Introspector.getWritableProperty(foo, "bar"));
		assertNotNull(Introspector.getReadableProperty(foo, "bar"));
		assertTrue(Introspector.hasWritableProperty(foo, "foochen"));
		methods = Introspector.findAllNoParameterMethods(foo, "getBar");
		assertEquals(1, methods.size());
		
		Set<IMethod> allMethods = Introspector.getAllMethods(foo);
		boolean containsSetFoochen = false;
		boolean containsSetBar = false;
		for (IMethod method : allMethods) {
			if (method.getElementName().contains("setBar")) {
				containsSetBar = true;
			}
			if (method.getElementName().contains("setFoochen")) {
				containsSetFoochen = true;
			}
		}
		
		assertTrue(containsSetBar);
		assertTrue(containsSetFoochen);
	}

	@Test
	public void testDoesExtend() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation");
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		assertTrue(Introspector.doesExtend(foo, "org.springframework.Base"));
		assertTrue(!Introspector.doesExtend(foo, "org.springframework.beans.factory.BeanFactory"));
	}

	@Test
	public void testDoesImplement() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation");
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		assertTrue(Introspector.doesImplement(foo, "org.springframework.FooInterface"));
		assertTrue(!Introspector
				.doesImplement(foo, "org.springframework.beans.factory.BeanFactory"));
		IType base = JdtUtils.getJavaType(project, "org.springframework.Base");
		assertTrue(Introspector.doesImplement(base, "org.springframework.FooInterface"));
	}

	@Test
	public void testfindAllConstructor() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation");
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		Set<IMethod> cons = Introspector.findAllConstructors(foo);
		assertTrue(!cons.isEmpty());
		assertTrue(cons.toArray().length == 5);
	}

	@Test
	public void testFindAllMethodsWithFilter() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation");
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		Set<IMethod> methods = Introspector.findAllMethods(foo, new IMethodFilter() {

			public boolean matches(IMethod method, String prefix) {
				return true;
			}
		});
		assertTrue(!methods.isEmpty());
		checkResult(methods, 24);

		methods = Introspector.findAllMethods(foo, "set", new IMethodFilter() {

			public boolean matches(IMethod method, String prefix) {
				return (method.getElementName().startsWith(prefix));
			}
		});
		assertTrue(!methods.isEmpty());
		checkResult(methods, 3);
	}

	@Test
	public void testFindAllMethods() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation");
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		// only methods; no constructors
		Set<IMethod> methods = Introspector.findAllMethods(foo, "", -1, Public.DONT_CARE,
				Static.DONT_CARE);
		checkResult(methods, 24);
		// only public setters (static does not matter)
		methods = Introspector.findAllMethods(foo, "set", 1, Public.YES, Static.DONT_CARE);
		checkResult(methods, 3);
		// only public setters (non-static)
		methods = Introspector.findAllMethods(foo, "set", 1, Public.YES, Static.NO);
		checkResult(methods, 2);
		// only protected methods setters (static does not matter)
		methods = Introspector.findAllMethods(foo, "", -1, Public.NO, Static.DONT_CARE);
		checkResult(methods, 9);
		// only protected methods setters (static)
		methods = Introspector.findAllMethods(foo, "", -1, Public.NO, Static.YES);
		checkResult(methods, 4);
	}

	@Test
	public void testGetAllImplementedInterfaces() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation");
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		Set<IType> interfaces = Introspector.getAllImplementedInterfaces(foo);
		assertTrue(interfaces.toArray().length == 2);
		IType base = JdtUtils.getJavaType(project, "org.springframework.Base");
		interfaces = Introspector.getAllImplementedInterfaces(base);
		assertTrue(interfaces.toArray().length == 1);
	}

	@Test
	public void testGetAllMethods() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation");
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		Set<IMethod> methods = Introspector.getAllMethods(foo);
		checkResult(methods, 24);
		IType base = JdtUtils.getJavaType(project, "org.springframework.Base");
		methods = Introspector.getAllMethods(base);
		checkResult(methods, 20);
	}

	private void checkResult(Set<IMethod> methods, int expectedSize) {
		assertTrue("Expected " + expectedSize + " methods to be found. actual is: "
				+ methods.toArray().length, methods.toArray().length == expectedSize);
	}

}
