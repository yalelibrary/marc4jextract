package edu.yale.library.marc4jextract;

import org.junit.Test;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import java.util.List;
import java.util.Map;

public class JsonExtractionSpecTest {

    @Test
    public void testExtractValues() throws InvalidSpecException {
        JsonExtractionSpec jsonExtractionSpec = new JsonExtractionSpec();
        jsonExtractionSpec.loadExtractors(getClass().getResourceAsStream("/test-extract-spec.json"));
        MarcStreamReader reader = new MarcStreamReader(getClass().getResourceAsStream("/manufacturing_consent.marc"));
        assert reader.hasNext();
        Record record = reader.next();
        Map<String, List<String>> values = jsonExtractionSpec.extractValues(record);
        assert values.size() == 4;
        assert values.get("TitleStatement").get(0).equals("Manufacturing consent : ----- the political economy of the mass media / ----- Edward S. Herman and Noam Chomsky ; with a new introduction by the authors");
        assert values.get("TitleStatement2").get(0).equals("Manufacturing consent : ----- the political economy of the mass media / ----- Edward S. Herman and Noam Chomsky ; with a new introduction by the authors");
        assert values.get("Title").get(0).equals("Manufacturing consent : the political economy of the mass media");
        assert values.get("Creator").get(0).equals("Herman, Edward S.");
    }

    @Test
    public void testExtractValuesWithString() throws InvalidSpecException {
        JsonExtractionSpec jsonExtractionSpec = new JsonExtractionSpec();
        jsonExtractionSpec.loadExtractors("[\n" +
                "  {" +
                "    \"name\": \"Title\"," +
                "    \"fieldSpec\": \"245abnps\"," +
                "    \"trimPunctuation\": true," +
                "    \"scriptInclusion\": \"NONE\"," +
                "    \"unknownProperty\": \"Value\"" +
                "  }]");

        MarcStreamReader reader = new MarcStreamReader(getClass().getResourceAsStream("/manufacturing_consent.marc"));
        assert reader.hasNext();
        Record record = reader.next();
        Map<String, List<String>> values = jsonExtractionSpec.extractValues(record);
        assert values.size() == 1;
        assert values.get("Title").get(0).equals("Manufacturing consent : the political economy of the mass media");
    }

    @Test
    public void testExtractValuesWithLanguageMap() throws InvalidSpecException {
        JsonExtractionSpec jsonExtractionSpec = new JsonExtractionSpec();
        jsonExtractionSpec.loadExtractors("[\n" +
                "  {" +
                "    \"name\": \"Language\"," +
                "    \"fieldSpec\": \"008[35-37]:041a\"," +
                "    \"trimPunctuation\": false," +
                "    \"scriptInclusion\": \"NONE\"," +
                "    \"extractFunction\": \"languageMap\"" +
                "  }]");

        MarcStreamReader reader = new MarcStreamReader(getClass().getResourceAsStream("/manufacturing_consent.marc"));
        assert reader.hasNext();
        Record record = reader.next();
        Map<String, List<String>> values = jsonExtractionSpec.extractValues(record);
        assert values.size() == 1;
        assert values.get("Language").get(0).equals("English");
    }


    @Test
    public void testValueSorting() throws InvalidSpecException {
        // Spec for Test is in order 245, 100, 260
        // marc record has values in order 100, 245, 260
        // Values should be in order of the marc record, not the order in the spec.
        // Test2 is 010, then 008 (mix of control and variable.)  Controls should always come first.
        JsonExtractionSpec jsonExtractionSpec = new JsonExtractionSpec();
        jsonExtractionSpec.loadExtractors("[\n" +
                "  {" +
                "    \"name\": \"Test2\"," +
                "    \"fieldSpec\": \"010:008\"," +
                "    \"trimPunctuation\": false," +
                "    \"scriptInclusion\": \"NONE\"" +
                "  }," +
                "  {" +
                "    \"name\": \"Test\"," +
                "    \"fieldSpec\": \"245ab:100a:260a\"," +
                "    \"trimPunctuation\": false," +
                "    \"scriptInclusion\": \"NONE\"" +
                "  }]");

        MarcStreamReader reader = new MarcStreamReader(getClass().getResourceAsStream("/manufacturing_consent.marc"));
        assert reader.hasNext();
        Record record = reader.next();
        Map<String, List<String>> values = jsonExtractionSpec.extractValues(record);
        assert values.size() == 2;
        assert values.get("Test").get(0).equals("Herman, Edward S.");
        assert values.get("Test").get(1).equals("Manufacturing consent : the political economy of the mass media /");
        assert values.get("Test").get(2).equals("New York :");
        assert values.get("Test2").get(0).equals("010831s2002    nyu      b    001 0 eng  ");
        assert values.get("Test2").get(1).equals("  2001050014");
    }

    @Test
    public void testLoadExtractors() throws InvalidSpecException {
        JsonExtractionSpec jsonExtractionSpec = new JsonExtractionSpec();
        jsonExtractionSpec.loadExtractors(getClass().getResourceAsStream("/test-extract-spec.json"));
    }

    @Test( expected = edu.yale.library.marc4jextract.InvalidSpecException.class)
    public void testLoadInvalidExtractors() throws InvalidSpecException {
        JsonExtractionSpec jsonExtractionSpec = new JsonExtractionSpec();
        jsonExtractionSpec.loadExtractors(getClass().getResourceAsStream("/test-extract-spec-invalid.json"));
    }
}