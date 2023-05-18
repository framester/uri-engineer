package it.cnr.istc.stlab.framester.uriengineer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class InputTraverser {
    private static final Logger logger = LoggerFactory.getLogger(InputTraverser.class);
    private final String path;

    public InputTraverser(String path) {
        this.path = path;
    }


    public void traverse(Action a) {
        File f = new File(path);
        logger.info("Processing {}", path);
        traverseRec(a, f);
    }

    private void traverseRec(Action a, File f) {
        logger.info("Processing {}", f.getAbsolutePath());
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                traverseRec(a, child);
            }
        } else {
            a.act(f);
        }
    }
}
