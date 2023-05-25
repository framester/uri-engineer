package it.cnr.istc.stlab.framester.uriengineer;

import it.cnr.istc.stlab.lgu.commons.semanticweb.iterators.ClosableIterator;
import it.cnr.istc.stlab.lgu.commons.semanticweb.streams.StreamRDFUtils;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixRefactorizer implements Action {
    private static final Logger logger = LoggerFactory.getLogger(PrefixRefactorizer.class);
    private final Map<String, String> prefixMap;
    private final String input;
    private final String output;

    public void setSameAsFile(String sameAsFolder) {
        new File(sameAsFolder).mkdirs();
        this.sameAsFolder = sameAsFolder;
    }

    private  String sameAsFolder;

    private final Pattern pattern;
    private  Map<String, String> sameAsLinks = new HashMap<>();
    private boolean generateSameAsLinks;

    public PrefixRefactorizer(String input, String output, String mappingFile) throws IOException {
        logger.info("Input {}\nOutput {}\nMapping file {}", input, output, mappingFile);
        this.prefixMap = loadPrefixMap(mappingFile);
        this.input = input;
        this.output = output;
        new File(output).mkdirs();
        this.pattern = createRegexWithKeys(prefixMap);
    }

    public boolean isGenerateSameAsLinks() {
        return generateSameAsLinks;
    }

    public void setGenerateSameAsLinks(boolean generateSameAsLinks) {
        this.generateSameAsLinks = generateSameAsLinks;
    }

    private Pattern createRegexWithKeys(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = map.keySet().iterator();
        if (it.hasNext()) {
            sb.append(Pattern.quote(it.next()));
        }
        while (it.hasNext()) {
            sb.append('|');
            sb.append(Pattern.quote(it.next()));
        }
        logger.trace(sb.toString());
        return Pattern.compile(sb.toString());
    }

    private Triple mapTriple(Triple t) {
        logger.trace("Mapping {}", t.toString());
        return Triple.create(mapNode(t.getSubject()), mapNode(t.getPredicate()), mapNode(t.getObject()));
    }

    private Node mapNode(Node n) {
        Node result = n;
        if (n.isURI()) {

            String uri = n.getURI();
            Matcher m = pattern.matcher(uri);
            if (m.find()) {
                uri = prefixMap.get(uri.substring(m.start(), m.end())) + uri.substring(m.end());
            }
            result = NodeFactory.createURI(uri);

            if (!n.getURI().equals(uri) && generateSameAsLinks) {
                sameAsLinks.put(uri, n.getURI());
            }
        }

        return result;
    }

    @Override
    public void act(File f) {

        if (!FilenameUtils.isExtension(f.getAbsolutePath(), PrefixCollector.rdfTripleExtensions) && !FilenameUtils.isExtension(f.getAbsolutePath(), PrefixCollector.rdfQuadsExtensions)) {
            return;
        }

        String outFolder = FilenameUtils.getFullPath(f.getAbsolutePath()).replace(input, output);
        logger.info("Transforming {}, Path {}, out folder {}", f.getAbsolutePath(), FilenameUtils.getPath(f.getAbsolutePath()), outFolder);
        File outFolderFile = new File(outFolder);
        if (!outFolderFile.exists()) {
            logger.info("Creating Out folder {}", outFolderFile.getAbsolutePath());
            outFolderFile.mkdirs();
        }
        String fileName = FilenameUtils.getBaseName(f.getAbsolutePath());

        Lang lang = RDFLanguages.filenameToLang(f.getAbsolutePath());
        String outFile = outFolderFile.getAbsolutePath() + "/" + fileName + "." + lang.getFileExtensions().get(0);

        if(generateSameAsLinks)
            sameAsLinks = new HashMap<>();

        logger.info("Out folder {} Out file {}", outFolderFile.getAbsolutePath(), outFile);
        try {
            StreamRDF stream = StreamRDFWriter.getWriterStream(new FileOutputStream(outFile), lang);
            if (FilenameUtils.isExtension(f.getAbsolutePath(), PrefixCollector.rdfTripleExtensions)) {
                ClosableIterator<Triple> it = StreamRDFUtils.createIteratorTripleFromFile(f.getAbsolutePath());
                while (it.hasNext()) stream.triple(mapTriple(it.next()));
                it.close();
            } else if (FilenameUtils.isExtension(f.getAbsolutePath(), PrefixCollector.rdfQuadsExtensions)) {
                ClosableIterator<Quad> it = StreamRDFUtils.createIteratorQuadsFromFile(f.getAbsolutePath());
                while (it.hasNext()) {
                    Quad q = it.next();
                    stream.quad(Quad.create(mapNode(q.getGraph()), mapTriple(q.asTriple())));
                }
                it.close();
            }
            stream.finish();

            if (generateSameAsLinks)
                generateSameAsLinks();

        } catch (CompressorException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void generateSameAsLinks() throws FileNotFoundException {
        StreamRDF stream = StreamRDFWriter.getWriterStream(new FileOutputStream(sameAsFolder+"/sameAs.nt", true), Lang.NT);
        sameAsLinks.forEach((k, v) -> stream.triple(Triple.create(NodeFactory.createURI(k), OWL.sameAs.asNode(), NodeFactory.createURI(v))));
        stream.finish();
    }


    private Map<String, String> loadPrefixMap(String mappingFile) throws IOException {

        Reader in = new FileReader(mappingFile);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
        Map<String, String> prefixMap = new HashMap<>();
        for (CSVRecord record : records) {
            prefixMap.put(record.get(0), record.get(1));
            logger.trace(record.get(0) + " -> " + record.get(1));
        }
        in.close();
        logger.trace("Loaded {} mappings", prefixMap.size());

        return prefixMap;
    }
}
