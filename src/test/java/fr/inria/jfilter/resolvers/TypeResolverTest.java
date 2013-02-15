/**
 * Copyright (C) 2012 University Lille 1, Inria
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 * Contact: romain.rouvoy@univ-lille1.fr
 */
package fr.inria.jfilter.resolvers;

import junit.framework.Test;
import junit.framework.TestSuite;
import fr.inria.jfilter.FilterException;
import fr.inria.jfilter.FilterTestCase;

/**
 * Unit test for Type Resolver class.
 */
public class TypeResolverTest extends FilterTestCase {
	public TypeResolverTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(TypeResolverTest.class);
	}

	public void testResolveUnknownKey() throws FilterException {
		assertEmpty(TypeResolver.type.getValues(doe.dad, "abc"));
	}

	public void testResolveBean() throws FilterException {
		assertContains(doe.dad.getClass(),
				TypeResolver.type.getValues(doe.dad, "objectClass"));
	}

	public void testResolvePerson() throws FilterException {
		assertContains(Person.class,
				TypeResolver.type.getValues(Person.class, "objectClass"));
	}
}
