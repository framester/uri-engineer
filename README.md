# URI Engineer

The URI Engineer assists you in common URI engineering tasks.

An executable JAR of the tool can be obtained from the Releases page.

The jar can be executed as follows:

```
usage: java -jar framester.uri-engineer.jar -m
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