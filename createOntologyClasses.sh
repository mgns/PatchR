/usr/local/Cellar/jena/2.11.1/bin/schemagen -i http://www.w3.org/ns/prov-o --owl --package de.hpi.patchr.vocab -o ./software/patchr-impl/src/main/java/ -n ProvOntology
/usr/local/Cellar/jena/2.11.1/bin/schemagen -i ./vocabulary/guo.ttl --owl --package de.hpi.patchr.vocab -o ./software/patchr-impl/src/main/java/ -n GuoOntology
/usr/local/Cellar/jena/2.11.1/bin/schemagen -i ./vocabulary/patchr.ttl --owl --package de.hpi.patchr.vocab -o ./software/patchr-impl/src/main/java/ -n PatchrOntology
/usr/local/Cellar/jena/2.11.1/bin/schemagen -i http://rdfs.org/ns/void# -a http://rdfs.org/ns/void# --package de.hpi.patchr.vocab -o ./software/patchr-impl/src/main/java/ -n VoidOntology
