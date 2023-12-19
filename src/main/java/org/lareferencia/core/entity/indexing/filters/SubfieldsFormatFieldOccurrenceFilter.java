package org.lareferencia.core.entity.indexing.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lareferencia.core.entity.domain.FieldOccurrence;
import org.lareferencia.core.entity.domain.SimpleFieldOccurrence;
import org.springframework.stereotype.Component;

@Component
public class SubfieldsFormatFieldOccurrenceFilter implements IFieldOccurrenceFilter {

    public static final String NAME = "subfields-format";

    private static Logger logger = LogManager.getLogger(SubfieldsFormatFieldOccurrenceFilter.class);

    public String getName() { return NAME; }

    public Collection<FieldOccurrence> filter(Collection<FieldOccurrence> occurrences, Map<String,String> params) {

        List<FieldOccurrence> result = new ArrayList<FieldOccurrence>();

        String[] filterSubfields = params.containsKey("filter-subfields") ? params.get("filter-subfields").split(",") : null;
        String filterFormat = params.containsKey("filter-format") ? params.get("filter-format") : "";

        if (filterSubfields == null || filterFormat == null) {
            logger.error("Error filtering occurrences " + this.getName() + " - filter-subfields and filter-format are required");
            return occurrences;
        }

        // get a stream from the occurrences
        Stream<FieldOccurrence> stream = occurrences.stream();

        // get preferred param
        boolean preferred = params.containsKey("preferred") ? Boolean.parseBoolean(params.get("preferred")) : false;

        // if preferred is true and there is a preferred occurrence, filter by preferred
        if (preferred && occurrences.stream().anyMatch(occurrence -> occurrence.getPreferred() == true) )
            stream = stream.filter(occurrence -> occurrence.getPreferred() == true);

        stream.forEach(occur -> {
            try {
                ArrayList<String> subfields = new ArrayList<String>();

                for ( String subfield: filterSubfields ) {
                    String value = occur.getValue(subfield);
                    if (value != null)
                        subfields.add(value);
                }

                // if the number of subfields is not the same as the number of filterSubfields, then the occurrence is not added to the result
                if (subfields.size() != filterSubfields.length)
                    return;

                // create a new FieldOccurrence in replace of the existing one, the new one is built by selected subfields formatted as filterFormat
       
                result.add( new SimpleFieldOccurrence( String.format(filterFormat, (Object[]) subfields.toArray()) ) );
            } catch (Exception e) {
                logger.error("Error filtering occurrences " + this.getName() + " ", e);
            }
        });

        return result;
    }
}
