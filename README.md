# URI Engineer

The URI Engineer assists you in common URI engineering tasks.

At the moment the tool implements two methods:

- 'collect-prefixes': given a path to a (folder of) RDF file(s) returns the list of prefixes of the entities occurring in the file(s) as subjects, predicates, objects of triples and named-graphs of quads.
- 'refactor-prefixes': given a path to a (folder of) RDF file(s), an output folder and a mapping specification, substitutes the prefixes of the entities as specified in the mapping file. The mapping file is a table in csv format containing a row for each mapping where the first cell of the row is the prefix to be substituted and the second is the desired prefix.

An executable JAR file of the tool can be obtained from the Releases page.
To be executed the JAR requires Java 11+.

The jar can be executed as follows:

```
usage: java -jar framester.uri-engineer-v<version>.jar -m
            (collect-prefixes|refactor-prefixes) -i filepath [-e -mf
            filepath -o filepath]
 -m,--method <collect-prefixes|refactor-prefixes>   The method to invoke.
                                                    Only two methods
                                                    available
                                                    'collect-prefixes' and
                                                    'refactor-prefixes'.
                                                    By passing
                                                    'collect-prefixes' the
                                                    tool returns the set
                                                    of prefixes used in
                                                    the URIs files. By
                                                    passing
                                                    'refactor-prefixes',
                                                    the tool performs the
                                                    refactoring of the
                                                    input files.
 -i,--input <filepath>                              A path to a file or a
                                                    folder.
 -mf,--mapping-file <filepath>                      A path to a csv file
                                                    (Mandatory for
                                                    refactor-prefixes).
 -o,--output-folder <filepath>                      A path to an output
                                                    folder (Mandatory for
                                                    refactor-prefixes).
 -e,--collect-examples                              If set, the tool will
                                                    collect examples of
                                                    URIs for each prefix.
```

##  License

The URI Engineer is distributed under [Apache 2.0 License](LICENSE)