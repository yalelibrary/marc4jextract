package edu.yale.library.marc4jextract;

import org.junit.Test;
import java.util.Arrays;

public class StringFormatterTest {

    @Test
    public void testTrimPunctuation() {
        String[][] pars = {
                {"Author Esq.","Author Esq"}, // do strip if last work is 3 or more characters.
                {"Author Jr.","Author Jr."}, // don't strip if last work is 2 or 1 characters.
                {"Author Wilson.","Author Wilson"}, // strip normally
                {"  Jan /","Jan"}, // strip and trip
                {"Test;","Test"}, // remove trailing ;
                {";Test",";Test"}, // don't remove leading ;
                {"Jacksol.;","Jacksol"}, // remove single word period
                {"民族知性.","民族知性"},  // remove period with non latin alpha numberic words
                {"民族知 性.","民族知 性."}, // don't strip if last work is 2 or 1 characters.
                {".", ""},
                {"http://id.loc.gov/authorities/names/n00012787.", "http://id.loc.gov/authorities/names/n00012787"},
                {"http://id.loc.gov/authorities/names/n00012787/", "http://id.loc.gov/authorities/names/n00012787"}
        };

        Arrays.stream(pars).forEach(par -> {
            assert (StringFormatter.trimPunctuation(par[0])).equals(par[1]);
        });
    }
}