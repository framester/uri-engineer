package it.cnr.istc.stlab.framester.uriengineer;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import it.cnr.istc.stlab.lgu.commons.semanticweb.iterators.ClosableIterator;
import it.cnr.istc.stlab.lgu.commons.semanticweb.streams.StreamRDFUtils;
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
    private final HashMap<String, Set<String>> prefixes = new HashMap<>();
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

    public void collectPrefixOfNode(Node n, String inputFile) {
        if (n.isURI()) {
            collectPrefixOfUriString(n.getURI(), inputFile);
        }
    }

    public void collectPrefixOfNodesOfTriple(Triple t, String inputFile) {
        collectPrefixOfNode(t.getSubject(), inputFile);
        collectPrefixOfNode(t.getPredicate(), inputFile);
        collectPrefixOfNode(t.getObject(), inputFile);
    }

    public void collectPrefixOfUriString(String uri, String inputFile) {
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
        Set<String> files = prefixes.get(prefixToAdd);
        if(files==null){
            files = new HashSet<>();
        }
        files.add(inputFile);
        prefixes.put(prefixToAdd, files);
        if (collectUriExamples) {
            collectExample(prefixToAdd, uri);
        }

    }

    public HashMap<String, Set<String>> getCollectedPrefixes() {
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
                    collectPrefixOfNodesOfTriple(it.next(), f.getAbsolutePath());
                }
            } catch (CompressorException | IOException e) {
                throw new RuntimeException(e);
            }
        } else if (FilenameUtils.isExtension(f.getAbsolutePath(), rdfQuadsExtensions)) {
            try {
                Iterator<Quad> it = StreamRDFUtils.createIteratorQuadsFromFile(f.getAbsolutePath());
                while (it.hasNext()) {
                    Quad q = it.next();
                    collectPrefixOfNode(q.getGraph(), f.getAbsolutePath());
                    collectPrefixOfNodesOfTriple(q.asTriple(), f.getAbsolutePath());
                }
            } catch (CompressorException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
