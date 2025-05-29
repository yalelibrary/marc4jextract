# MARC4J Extraction Library
## Overview
This is a Java library for extracting values from MARC bibliographic records using JSON specifications, inspired by the Ruby Traject project. The library uses the MARC4J API for MARC record processing.

The library provides an easy way to create a set of extractors

## Building and Testing
This project is setup using maven.

Run tests:
```
mvn test
```

Build and install into local maven repository with:
```
mvn clean install
```
The jar file is created in `./target`.


## Single Extraction
The simplest way to use the library is to use Extractor directly.  This does not take advantage of
reusing the compiled extractors. See JSON Extraction Specifications for a more efficient way to extract
from multiple records.
```java
    org.marc4j.marc.Record record;
...
    List<String> results = Extractor.extract(record, "710ab", ScriptInclusion.NONE, true, null, " ");
```

## JSON Extraction Specifications
JSON can be used to build a set of extractors which can be used with multiple
MARC records to increase performance and to externalize the extraction specification.

Sample JSON Specification File:
```json
[
  {
    "name": "Creator",
    "fieldSpec": "100abcdegqu:110abcdegnu:111acdegjnqu",
    "trimPunctuation": true,
    "scriptInclusion": "BOTH"
  },
  {
    "name": "Title",
    "fieldSpec": "245abnps",
    "trimPunctuation": true,
    "scriptInclusion": "BOTH"
  },
  {
    "name": "TitleStatement",
    "fieldSpec": "245",
    "trimPunctuation": true,
    "scriptInclusion": "BOTH",
    "delimiter": " - "
  }
]
```

### name
The name field is the key used for extractor results.

### fieldSpec
The `fieldSpec` property of each field indicates which MARC fields and subfields to use similarly to Traject specifications.

If a fieldSpec has no subfields, the library will extract all non-numeric fields (excluding 0-9).  In the example
above, TitleStatement uses `245`, which is equivalent to `245:abcdefghijklmnopqrstuvwxyz`.

To filter the values by indicators, specify the values between pipes.
`100|03|ab` means only include when indicator 1 is `0` and indicator 2 is `3`. It's possible to
wildcard one indicator with an *. (`|**|` is the default).

To indicate that you want subfields from the same field, but not concatenated, they can be separated by colons.
So, 505a:505b indicates two values, one with 505a and one with 505b. As opposed to 505ab which
would indicate concatenating subfields a and b into one value.

There is a shortcut for all fields a-z when they need to be combined with numeric subfields. A `fieldSpec` of
`245:_ATOZ_56` is equivalent to `245:abcdefghijklmnopqrstuvwxyz56`.

Parts of fields can be extracted using byte ranges. `008[0-5]` indicates field 008, bytes 0-5.

### trimPunctuation
Trim punctuation indicates if the field should be trimmed.
- trailing: comma, slash, semicolon, colon (possibly preceded and followed by whitespace)
- trailing period if it is preceded by at least three letters (possibly preceded and followed by whitespace)

### scriptInclusion
Indicates if linked 880 fields should be included in the results.  Values can be
BOTH, NONE, or ONLY.  ONLY indicates to include just the 880 field.

- NONE: extract all fields, but not their associated transcript 880 fields. Only display English
- ONLY: extract only transcript 880 fields. Only display foreign language.
- BOTH: extract both fields and associated 880 fields. Display both English and foreign language.

### filter
Filter is a Java regex used to filter out part of the field.  The first group populates the field value.

For example, a filter of `".*\\((.*?)\\).*"` will populate the value with the portion of the value that is between
parenthesis.

### delimiter
The delimiter used when concatenating subfields.  The default is space.

## Loading JSON and Extracting Values
In order to efficiently extract from multiple records, load the JSON once and reuse the
JsonExtractionSpec on multiple records.  You can load the JSON as a string, stream, or byte array.

This is a simple example providing the JSON as a string:
```java
JsonExtractionSpec jsonExtractionSpec = new JsonExtractionSpec();
jsonExtractionSpec.loadExtractors("[\n" +
        "  {" +
        "    \"name\": \"Title\"," +
        "    \"fieldSpec\": \"245abnps\"," +
        "    \"trimPunctuation\": true," +
        "    \"scriptInclusion\": \"NONE\"" +
        "  }]");

MarcStreamReader reader = new MarcStreamReader(marcInputStream);
while (reader.hasNext()) {
    Record record = reader.next();
    Map<String, List<String>> values = jsonExtractionSpec.extractValues(record);
}
```
Values is a map of extractor name to list of values.
So, `values.get("Title")` is used to get the values
for Title.

See tests for more examples.

## Versioning

We are using simple version numbers in the format: #.#.# (e.g. 1.0.0). Typically, if a new version is going to be released, increase the minor version and create a new release.

First, update the pom.xml file so that the <version>1.0.1</version> tag is updated to match the release. Create a PR and get this change into the main branch.

Create the release so that it is based on main with the updated pom file.

Make the tag and release name the version: e.g. 1.0.2, 1.0.3, etc.

Building the jar with `mvn clean install` to create the new jar in target/ with the new version in the file name.