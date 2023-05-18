package it.cnr.istc.stlab.framester.uriengineer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

//import au.com.bytecode.opencsv.CSVReader;
import it.cnr.istc.stlab.lgu.commons.semanticweb.iterators.ClosableIterator;
import it.cnr.istc.stlab.lgu.commons.semanticweb.streams.StreamRDFUtils;

public class PrefixRefactorizer {

	private static final Logger logger = LogManager.getLogger(PrefixRefactorizer.class);
	private static final Set<String> rdfExtensions = Sets.newHashSet("ttl", "nt", "rdf", "owl");

	public static void main(String[] args) throws CompressorException, IOException {
		try {
			Configurations configs = new Configurations();
			Configuration config = configs.properties("config.properties");

			if (args.length < 2) {
				System.out.println("Unsufficient number of parameters.");
				System.exit(1);
			}

			String mappingFile = config.getString("mappingFile");
			logger.info("Mapping file: " + mappingFile);
			String outSyntax = config.getString("outSyntax");
			logger.info("Out syntax: " + outSyntax);
			String outFolder = args[args.length - 1];
			logger.info("Out folder: " + outFolder);
			Map<String, String> prefixMap = loadPrefixMap(mappingFile);
			boolean useModel = config.getBoolean("useModel");

			Pattern p = createRegexWithKeys(prefixMap);

			for (int i = 0; i < args.length - 1; i++) {
				logger.trace("IN " + args[i]);
				File in = new File(args[i]);
				if (in.isDirectory()) {
					for (File f : in.listFiles()) {
						mapPrefixes(f.getAbsolutePath(), outFolder, prefixMap, outSyntax, p, useModel);
					}
				} else {
					mapPrefixes(in.getAbsolutePath(), outFolder, prefixMap, outSyntax, p, useModel);
				}
			}

		} catch (ConfigurationException | FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static void mapPrefixes(String inFilepath, String outFolder, Map<String, String> prefixMap,
			String outSyntax, Pattern p, boolean useModel) throws CompressorException, IOException {
		File f = new File(inFilepath);
		logger.trace("Processing " + inFilepath);
		if (f.isDirectory()) {
			new File(outFolder + "/" + f.getName()).mkdirs();
			for (File child : f.listFiles()) {
				mapPrefixes(child.getAbsolutePath(), outFolder + "/" + f.getName(), prefixMap, outSyntax, p, useModel);
			}
		} else {
			new File(outFolder).mkdirs();
			mapPrefixesOfFile(inFilepath, outFolder, prefixMap, outSyntax, p, useModel);
		}
	}

	private static void mapPrefixesOfFile(String inFilepath, String outFolder, Map<String, String> prefixMap,
			String outSyntax, Pattern p, boolean useModel) throws CompressorException, IOException {
		if (FilenameUtils.isExtension(inFilepath, rdfExtensions)) {
			// TODO manage compression
			String fileName = FilenameUtils.getBaseName(inFilepath);
			String outFile = outFolder + "/" + fileName + "."
					+ RDFLanguages.fileExtToLang(outSyntax).getFileExtensions().get(0);

			ClosableIterator<Triple> it = StreamRDFUtils.createIteratorTripleFromFile(inFilepath);
			if (!useModel) {
				StreamRDF stream = StreamRDFWriter.getWriterStream(new FileOutputStream(new File(outFile)),
						RDFLanguages.fileExtToLang(outSyntax));
				while (it.hasNext()) {
					Triple triple = (Triple) it.next();
					stream.triple(mapTriple(triple, prefixMap, p));

				}
				stream.finish();
			} else {
				Graph g = GraphFactory.createGraphMem();
				while (it.hasNext()) {
					Triple triple = (Triple) it.next();
					g.add(mapTriple(triple, prefixMap, p));
				}
				ModelFactory.createModelForGraph(g).write(new FileOutputStream(new File(outFile)),
						RDFLanguages.fileExtToLang(outSyntax).getName());

			}
			it.close();
		}
	}

	private static Pattern createRegexWithKeys(Map<String, String> map) {
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

	private static Triple mapTriple(Triple t, Map<String, String> prefixMap, Pattern pattern) {
		return Triple.createMatch(mapNode(t.getSubject(), prefixMap, pattern),
				mapNode(t.getPredicate(), prefixMap, pattern), mapNode(t.getObject(), prefixMap, pattern));
	}

	private static Node mapNode(Node n, Map<String, String> prefixMap, Pattern pattern) {
		Node result = n;
		if (n.isURI()) {

			String uri = n.getURI();
			Matcher m = pattern.matcher(uri);
			if (m.find()) {
				uri = prefixMap.get(uri.substring(m.start(), m.end())) + uri.substring(m.end());
			}
			result = NodeFactory.createURI(uri);
		}

		return result;
	}

	private static Map<String, String> loadPrefixMap(String mappingFile) throws IOException {
//		CSVReader csvReader = new CSVReader(new FileReader(new File(mappingFile)));
//		List<String[]> rows = csvReader.readAll();
//		Iterator<String[]> ri = rows.iterator();
//		Map<String, String> prefixMap = new HashMap<>();
//		while (ri.hasNext()) {
//			String[] strings = ri.next();
//			prefixMap.put(strings[0], strings[1]);
//			logger.trace(strings[0] + " -> " + strings[1]);
//		}
//		csvReader.close();
//		logger.trace("Loaded " + prefixMap.size() + " mappings");
//		return prefixMap;
		return null;
	}
}
