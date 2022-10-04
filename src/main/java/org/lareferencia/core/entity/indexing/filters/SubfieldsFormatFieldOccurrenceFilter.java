package org.lareferencia.core.entity.indexing.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

        for ( FieldOccurrence occur: occurrences ) {
            try {
                // create a new FieldOccurrence in replace of the existing one, the new one is built by selected subfields formatted as filterFormat
                FieldOccurrence newOccur = new SimpleFieldOccurrence(occur.getFieldType());
                newOccur.addValue( String.format(filterFormat, (Object[]) filterSubfields) );
                result.add(newOccur);
            } catch (Exception e) {
                logger.error("Error filtering occurrences " + this.getName() + " ", e);
            }
        }

        return result;
    }
}
