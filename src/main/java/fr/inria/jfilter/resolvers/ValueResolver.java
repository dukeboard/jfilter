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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class ValueResolver {
	public static final ValueResolver instance = new ValueResolver();

    protected static final ValueResolver[] resolvers = new ValueResolver[]{
            BeanResolver.bean, TypeResolver.type, MapResolver.map};

	protected ValueResolver() {
	}

	public Collection<Object> getValues(Object pojo, String[] path) {
		Collection<Object> input = Collections.singleton(pojo);
		for (String key : path) {
			Collection<Object> output = new HashSet<Object>();
			for (Object obj : input)
				output.addAll(getValues(obj, key));
			if (output.isEmpty())
				return output;
			input = output;
		}
		return input;
	}

	public Collection<Object> getValues(Object pojo, String key) {
		// Collection<Object> res = new HashSet<Object>();
		for (ValueResolver resolver : resolvers) {
			// res.addAll(resolver.getValue(pojo, key));
			final Collection<Object> res = resolver.getValues(pojo, key);
			if (!res.isEmpty()) // Returns result from 1st matching resolver
				return res;
		}
		return Collections.emptySet();
	}

	protected final Collection<Object> update(Collection<Object> col, Object value) {
		if (value == null)
			return col;
		col.add(value);
		if (value instanceof Collection<?>)
			col.addAll((Collection<?>) value);
		return col;
	}
}
