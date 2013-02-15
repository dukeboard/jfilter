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
package fr.inria.jfilter.parsers;

import static java.util.regex.Pattern.compile;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import fr.inria.jfilter.Filter;
import fr.inria.jfilter.FilterParser;
import fr.inria.jfilter.utils.Option;

public class LdapFilterParser extends FilterParser {
	// filter = "(" filtercomp ")"
	private final Pattern filterRule = compile("^\\x28(.+)\\x29$");

	// not = "!" filter
	private final Pattern notRule = compile("^!(.+)$");

	// filtercomp = and / or / not / item

	// and = "&" filterlist
	private final Pattern andRule = compile("^&(.+)$");
	// or = "|" filterlist
	private final Pattern orRule = compile("^\\x7C(.+)$");

	// filterlist = 1*filter
	// item = simple / present / substring / extensible
	// simple = attr filtertype value
	// filtertype = equal / approx / greater / less
	// equal = "=", approx = "~=", greater = ">=", less = "<="
	// private final Pattern simpleRule =
	// compile("^(\\S*)\\s*([=|~|>|<])\\s*(.+)$");
	private final Pattern equalRule = compile("^([^<>]+)=(.+)$");
	private final Pattern differRule = compile("^(.+)~(.+)$");
	private final Pattern greaterRule = compile("^(.+)>([^=]+)$");
	private final Pattern greaterEqRule = compile("^(.+)>=(.+)$");
	private final Pattern lessRule = compile("^(.+)<([^=]+)$");
	private final Pattern lessEqRule = compile("^(.+)<=(.+)$");

	// extensible = attr [":dn"] [":" matchingrule] ":=" value / [":dn"] ":"
	// matchingrule ":=" value
	// present = attr "=*"
	// substring = attr "=" [initial] any [final]
	// initial = value
	// any = "*" *(value "*")
	// final = value
	// attr = AttributeDescription from Section 4.1.5 of [1]
	// matchingrule = MatchingRuleId from Section 4.1.9 of [1]
	// value = AttributeValue from Section 4.1.6 of [1]

	private final Logger log = Logger.getLogger(LdapFilterParser.class
			.getName());

	@SuppressWarnings("unchecked")
	protected Option<Filter> tryToParse(String filter) {
		if (log.isLoggable(Level.FINE))
			log.fine("Trying to parse \"" + filter + "\" as an LDAP filter");
		return filter(filter.trim()).or(filtercomp(filter.trim()));
	}

	private final Option<Filter> filter(String filter) {
		final Matcher m = matches(filter, filterRule);
		return filtercomp(m == null ? filter : m.group(1).trim());
	}

	@SuppressWarnings("unchecked")
	private final Option<Filter> filtercomp(String filter) {
		return and(filter).or(or(filter), not(filter), item(filter));
	}

	private final Filter[] filterlist(String filter) {
		LinkedList<Filter> list = new LinkedList<Filter>();
		for (String f : split(filter, '(', ')')) {
			Option<Filter> res = filter(f);
			if (res.isDefined())
				list.add(res.get());
		}
		return list.toArray(new Filter[list.size()]);
	}

	private final Option<Filter> and(String filter) {
		final Matcher m = matches(filter, andRule);
		if (m == null)
			return none;
		return some(and(filterlist(m.group(1))));
	}

	private final Option<Filter> or(String filter) {
		final Matcher m = matches(filter, orRule);
		if (m == null)
			return none;
		return some(or(filterlist(m.group(1))));
	}

	private final Option<Filter> not(String filter) {
		final Matcher m = matches(filter, notRule);
		if (m == null)
			return none;
		Option<Filter> res = filter(m.group(1).trim());
		if (res.isEmpty())
			return none;
		return some(not(res.get()));
	}

	@SuppressWarnings("unchecked")
	private final Option<Filter> item(String filter) {
		if (filter.startsWith("!")) {
			Option<Filter> res = item(filter.substring(1).trim());
			return res.isEmpty() ? res : some(not(res.get()));
		}
		return equal(filter).or(differ(filter), greater(filter), less(filter),
				greaterEq(filter), lessEq(filter));
	}

	private Option<Filter> equal(String filter) {
		final Matcher m = matches(filter, equalRule);
		if (m == null)
			return none;
		return some(or(equalsTo(attr(m.group(1)), value(m.group(2))),
				wildcard(attr(m.group(1)), value(m.group(2)))));
	}

	private Option<Filter> differ(String filter) {
		final Matcher m = matches(filter, differRule);
		if (m == null)
			return none;
		return some(not(equalsTo(attr(m.group(1)), value(m.group(2)))));
	}

	private Option<Filter> greater(String filter) {
		final Matcher m = matches(filter, greaterRule);
		if (m == null)
			return none;
		return some(moreThan(attr(m.group(1)), value(m.group(2))));
	}

	private Option<Filter> greaterEq(String filter) {
		final Matcher m = matches(filter, greaterEqRule);
		if (m == null)
			return none;
		return some(not(lessThan(attr(m.group(1)), value(m.group(2)))));
	}

	private Option<Filter> less(String filter) {
		final Matcher m = matches(filter, lessRule);
		if (m == null)
			return none;
		return some(lessThan(attr(m.group(1)), value(m.group(2))));
	}

	private Option<Filter> lessEq(String filter) {
		final Matcher m = matches(filter, lessEqRule);
		if (m == null)
			return none;
		return some(not(moreThan(attr(m.group(1)), value(m.group(2)))));
	}

	private String[] attr(String filter) {
		return identifier(filter.trim());
	}

	private String value(String filter) {
		return filter.trim();
	}

	private String[] identifier(String filter) {
		String[] res = filter.split("\\.");
		return res.length > 0 ? res : new String[] { filter };
	}
}
