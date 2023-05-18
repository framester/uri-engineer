package it.cnr.istc.stlab.framester.uriengineer;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import it.cnr.istc.stlab.lgu.commons.semanticweb.iterators.ClosableIterator;
import it.cnr.istc.stlab.lgu.commons.semanticweb.streams.StreamRDFUtils;
import org.apache.commons.collections.list.FixedSizeList;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PrefixCollector implements Action {

    private static final Logger logger = LoggerFactory.getLogger(PrefixCollector.class);
    public static final Set<String> rdfTripleExtensions = Sets.newHashSet("ttl", "nt", "rdf", "owl");
    public static final Set<String> rdfQuadsExtensions = Sets.newHashSet("nq");
    private final Set<String> prefixes = new HashSet<>();
    private final Map<String, Set<String>> examples = new HashMap<>();
    private boolean collectUriExamples = false;
    private final int nOfExamples = 10;

    public void collectExample(String prefix, String uri) {
        Set<String> prefixExamples = examples.computeIfAbsent(prefix, k -> new HashSet<>(nOfExamples));
        if (prefixExamples.size() < nOfExamples) {
            prefixExamples.add(uri);
        }

    }

    public void setCollectUriExamples(boolean collectUriExamples) {
        this.collectUriExamples = collectUriExamples;
    }

    public void collectPrefixOfNode(Node n) {
        if (n.isURI()) {
            collectPrefixOfUriString(n.getURI());
        }
    }

    public void collectPrefixOfNodesOfTriple(Triple t) {
        collectPrefixOfNode(t.getSubject());
        collectPrefixOfNode(t.getPredicate());
        collectPrefixOfNode(t.getObject());
    }

    public void collectPrefixOfUriString(String uri) {
        String prefixToAdd;
        if (uri.contains("#")) {
            if (uri.charAt(uri.length() - 1) == '#') {
                prefixToAdd = uri;
            } else {
                prefixToAdd = uri.split("#")[0].concat("#");
            }

        } else {
            if (uri.charAt(uri.length() - 1) == '/') {
                prefixToAdd = uri;
            } else {
                String[] split = uri.split("/");
                logger.trace("Last '{}'", split[split.length - 1]);
                prefixToAdd = StringUtils.join(Arrays.asList(split).subList(0, split.length - 1), "/").concat("/");
            }
        }
        prefixes.add(prefixToAdd);
        if (collectUriExamples) {
            collectExample(prefixToAdd, uri);
        }

    }

    public Set<String> getCollectedPrefixes() {
        return prefixes;
    }

    public Map<String, Set<String>> getCollectedExamples(){
        return examples;
    }

    @Override
    public void act(File f) {
        if (FilenameUtils.isExtension(f.getAbsolutePath(), rdfTripleExtensions)) {
            try {
                ClosableIterator<Triple> it = StreamRDFUtils.createIteratorTripleFromFile(f.getAbsolutePath());
                while (it.hasNext()) {
                    collectPrefixOfNodesOfTriple(it.next());
                }
            } catch (CompressorException | IOException e) {
                throw new RuntimeException(e);
            }
        } else if (FilenameUtils.isExtension(f.getAbsolutePath(), rdfQuadsExtensions)) {
            try {
                Iterator<Quad> it = StreamRDFUtils.createIteratorQuadsFromFile(f.getAbsolutePath());
                while (it.hasNext()) {
                    Quad q = it.next();
                    collectPrefixOfNode(q.getGraph());
                    collectPrefixOfNodesOfTriple(q.asTriple());
                }
            } catch (CompressorException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
