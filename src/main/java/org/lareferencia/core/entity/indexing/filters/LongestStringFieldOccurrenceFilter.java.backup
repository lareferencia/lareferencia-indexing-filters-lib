package org.lareferencia.core.entity.indexing.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lareferencia.core.entity.domain.EntityRelationException;
import org.lareferencia.core.entity.domain.FieldOccurrence;
import org.lareferencia.core.entity.indexing.filters.IFieldOccurrenceFilter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LongestStringFieldOccurrenceFilter implements IFieldOccurrenceFilter {

    public static final String NAME = "longest-string";

    private static Logger logger = LogManager.getLogger(LongestStringFieldOccurrenceFilter.class);

    public String getName() { return NAME; }

    public Collection<FieldOccurrence> filter(Collection<FieldOccurrence> occurrences, Map<String,String> params) {

       int filter_limit = params.containsKey("filter-limit") ? Integer.parseInt(params.get("filter-limit")) : 0;

        // get preferred param
        boolean preferred = params.containsKey("preferred") ? Boolean.parseBoolean(params.get("preferred")) : false;

        // if preferred is true and there is a preferred occurrence, filter by preferred
        if (preferred && occurrences.stream().anyMatch(occurrence -> occurrence.getPreferred() == true) )
            occurrences = occurrences.stream().filter(occurrence -> occurrence.getPreferred() == true).collect(Collectors.toList());

        if (occurrences.size() == 0)
            return occurrences;

        if (occurrences.size() == 1)
            return occurrences;

        int maxLength = occurrences.stream().mapToInt(occ -> getLength(occ, params)).max().getAsInt();

         // then filter by the longest string
        Stream<FieldOccurrence> stream = occurrences.stream().filter(occurrence -> getLength(occurrence, params) == maxLength);

        // limit the number of occurrences to filter_limit, 0 means no limit
        if (filter_limit > 0)
            stream = stream.limit(filter_limit);

        // return the stream as a list
        return stream.collect(Collectors.toList());
    }


    private int getLength(FieldOccurrence a, Map<String,String> params) {

        String subfield = params.containsKey("subfield") ? params.get("subfield") : null;
        // regex example ,\s|\.|Doctor
        String regexClean = params.containsKey("filter-regex-clean") ? params.get("filter-regex-clean") : "";

        try {
            if (subfield != null)
                return a.getValue(subfield).replaceAll(regexClean, "") .length();
            else
                return a.getValue().replaceAll(regexClean, "").length();

        } catch (EntityRelationException e) {
            logger.error("Error filtering occurrences " + this.getName() + " ", e);
        }
        return 0;
    }
}
