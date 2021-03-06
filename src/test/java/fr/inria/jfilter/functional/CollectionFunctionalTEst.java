package fr.inria.jfilter.functional;

import fr.inria.jfilter.Filter;
import fr.inria.jfilter.FilterComponentTestCase;
import fr.inria.jfilter.FilterException;
import fr.inria.jfilter.FilterParser;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 13/02/13
 * Time: 09:59
 */
public class CollectionFunctionalTest extends FilterComponentTestCase {

    public CollectionFunctionalTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(CollectionFunctionalTest.class);
    }

    public void testMethodCallOnCollectionAttribute() throws FilterException {
        FakeComponentModel model = new FakeComponentModel();
        assertSize(0, FilterParser.instance.parse("components.size>2").filter(model.getNodes()));
        assertSize(1, FilterParser.instance.parse("components.size=1").filter(model.getNodes()));
        assertSize(1, FilterParser.instance.parse("components.size=0").filter(model.getNodes()));
    }


    public void testMethodCallOnCollectionSubElementAttribute() throws FilterException {
        FakeComponentModel model = new FakeComponentModel();
        Filter filter1 = FilterParser.instance.parse("components.name = *");

        Collection<Node> obj = filter1.filter(model.getNodes());
        assertSize(1,obj);
    }


}
