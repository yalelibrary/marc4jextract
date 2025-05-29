package edu.yale.library.marc4jextract;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecordExtractorTest {

    private Record record;

    @Before
    public void setup() {
        record = loadTestRecord();
    }

    @Test
    public void extractValuesTest() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("041", "041|0 |a", ScriptInclusion.NONE, false, null, " " ).compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();
        assert result.get("041").size() == 1;
        assert result.get("041").get(0).equals("jpn");
    }

    @Test
    public void extractValuesWith880Test() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("245a", "245a", ScriptInclusion.ONLY, true, null, " " ).compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();
        assert result.get("245a").size() == 1;
        assert result.get("245a").get(0).equals("靖國神社");
    }

    @Test
    public void extractValuesBracketsAndPeriod() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("245c", "245c", ScriptInclusion.NONE, true, null, " " ).compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();
        assert result.get("245c").size() == 1;
        assert result.get("245c").get(0).equals("[henshū Yasukuni Jinja Shamusho].");
    }


    @Test
    public void extractValues008NoRange() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("008", "008", ScriptInclusion.NONE, false, null, " " ).compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();
        assert result.get("008").size() == 1;
        assert result.get("008").get(0).equals("871002s1986    ja a         f00000 jpn  ");
    }
    @Test
    public void extractValues008RangeTest1() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("008", "008[0-5]", ScriptInclusion.NONE, false, null, " " ).compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();
        assert result.get("008").size() == 1;
        assert result.get("008").get(0).equals("871002");
    }

    @Test
    public void extractValues008RangeTest2() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("008", "008[15]", ScriptInclusion.NONE, false, null, " " ).compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();
        assert result.get("008").size() == 1;
        assert result.get("008").get(0).equals("j");
    }


    @Test
    public void extractValuesWithMultipleSubfieldsTest() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("710ab", "710ab", ScriptInclusion.NONE, true, null," " ).compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();
        assert result.get("710ab").size() == 1;
        assert result.get("710ab").get(0).equals("Yasukuni Jinja (Tokyo, Japan). Shamusho");
    }


    @Test
    public void extractValuesWithXXTest() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("9XXab", "9XXab", ScriptInclusion.NONE, true, null," " ).compile());
        extractors.add(new Extractor("90Xab", "90Xab", ScriptInclusion.NONE, true, null," " ).compile());
        extractors.add(new Extractor("99Xab", "99Xab", ScriptInclusion.NONE, true, null," " ).compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();

        assert result.get("9XXab").size() == 4;
        assert result.get("9XXab").get(0).equals("BL2225 T6");
        assert result.get("9XXab").get(1).equals("Sterling Memorial Library SML, Stacks, LC Classification >>  BL2225 T6 Y3882 1986 (LC)+ Oversize|DELIM|638567");
        assert result.get("9XXab").get(2).equals("2002-06-01T00:00:00.000Z");
        assert result.get("9XXab").get(3).equals("10/02/87 EAT");

        assert result.get("90Xab").size() == 3;
        assert result.get("90Xab").get(0).equals("BL2225 T6");
        assert result.get("90Xab").get(1).equals("Sterling Memorial Library SML, Stacks, LC Classification >>  BL2225 T6 Y3882 1986 (LC)+ Oversize|DELIM|638567");
        assert result.get("90Xab").get(2).equals("2002-06-01T00:00:00.000Z");

        assert result.get("99Xab").size() == 1;
        assert result.get("99Xab").get(0).equals("10/02/87 EAT");
    }


    @Test
    public void extractValuesWithFilter() throws InvalidSpecException {
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(new Extractor("710ab", "710ab", ScriptInclusion.NONE, true, ".*\\((.*?)\\).*", " ").compile());
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> result = recordExtractor.extractValues(extractors);
        assert result.size() == extractors.size();
        assert result.get("710ab").size() == 1;
        assert result.get("710ab").get(0).equals("Tokyo, Japan");
    }


    private Record loadTestRecord() {
        // mocking marc records is probably not worth the trouble.
        MarcReader marcReader = new MarcStreamReader( this.getClass().getResourceAsStream("/558505.marc"));
        if ( marcReader.hasNext() ) {
            return marcReader.next();
        }
        throw new RuntimeException("Unable to load test marc record.");
    }
}