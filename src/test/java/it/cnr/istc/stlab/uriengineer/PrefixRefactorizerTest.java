package it.cnr.istc.stlab.uriengineer;

import it.cnr.istc.stlab.framester.uriengineer.InputTraverser;
import it.cnr.istc.stlab.framester.uriengineer.PrefixRefactorizer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class PrefixRefactorizerTest {

    @Test
    public void test1() throws IOException, URISyntaxException {
        String inputFolder = getClass().getClassLoader().getResource("testResources/test1/input").toURI().toURL().getFile();
        File folderOut = new File("/Users/lgu/Desktop/out");
        folderOut.mkdir();
        String mappingFile = getClass().getClassLoader().getResource("testResources/test1/input/mappingFile.csv").toURI().toURL().getFile();
        PrefixRefactorizer pr = new PrefixRefactorizer(inputFolder, folderOut.getAbsolutePath(), mappingFile);
        InputTraverser it = new InputTraverser(inputFolder);
        it.traverse(pr);
    }
}
