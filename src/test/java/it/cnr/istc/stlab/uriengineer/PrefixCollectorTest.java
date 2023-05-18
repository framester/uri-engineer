package it.cnr.istc.stlab.uriengineer;

import it.cnr.istc.stlab.framester.uriengineer.PrefixCollector;
import org.apache.commons.compress.utils.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Assert;
import org.junit.Test;

public class PrefixCollectorTest {
    @Test
    public void test1(){
        PrefixCollector pc = new PrefixCollector();
        pc.collectPrefixOfNode(NodeFactory.createURI("https://example.com/test/"));
        pc.collectPrefixOfNode(NodeFactory.createURI("https://example.com/test/a"));
        pc.collectPrefixOfUriString("https://example.com/test/a");
        pc.collectPrefixOfUriString("https://example.com/test/a#");
        pc.collectPrefixOfUriString("https://example.com/test/a#asd");

        Assert.assertEquals(Sets.newHashSet("https://example.com/test/", "https://example.com/test/a#"),pc.getCollectedPrefixes());
    }
}
