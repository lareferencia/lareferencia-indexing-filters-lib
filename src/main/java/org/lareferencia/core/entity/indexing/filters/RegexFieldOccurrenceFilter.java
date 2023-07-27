package org.lareferencia.core.entity.indexing.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lareferencia.core.entity.domain.EntityRelationException;
import org.lareferencia.core.entity.domain.FieldOccurrence;
import org.lareferencia.core.entity.domain.SimpleFieldOccurrence;
import org.lareferencia.core.util.date.DateHelper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RegexFieldOccurrenceFilter implements IFieldOccurrenceFilter {

    public static final String NAME = "regex";

    private static Logger logger = LogManager.getLogger(RegexFieldOccurrenceFilter.class);

    public String getName() { return NAME; }

    public Collection<FieldOccurrence> filter(Collection<FieldOccurrence> occurrences, Map<String,String> params) {

        int filter_limit = params.containsKey("filter-limit") ? Integer.parseInt(params.get("filter-limit")) : 0;

        String searchRegex = params.containsKey("filter-search") ? params.get("filter-search") : "";
        String replaceRegex = params.containsKey("filter-replace") ? params.get("filter-replace") : "";

        // get preferred param
        boolean preferred = params.containsKey("preferred") ? Boolean.parseBoolean(params.get("preferred")) : false;

        // if preferred is true and there is a preferred occurrence, filter by preferred
        if (preferred && occurrences.stream().anyMatch(occurrence -> occurrence.getPreferred() == true) )
            occurrences = occurrences.stream().filter(occurrence -> occurrence.getPreferred() == true).collect(Collectors.toList());

        if (occurrences.size() == 0)
            return occurrences;

        Pattern searchPattern = Pattern.compile(searchRegex);
        Pattern replacePattern = Pattern.compile(replaceRegex);

        // filter and transform the ocurrences using the search and replace regex
        Stream<FieldOccurrence> stream = occurrences.stream().map(occurrence -> {

            try {
                String value = occurrence.getValue();

                Matcher matcher = searchPattern.matcher(value);

                // if match
                if (matcher.find()) {

                    // if replaceRegex is empty, then return the original value
                    if (replaceRegex.isEmpty()) {
                        // if replaceRegex is empty, then return the original value
                        return occurrence;
                    } else { // if replaceRegex is not empty, then return the replaced value
                        String replacedValue = matcher.replaceAll(replaceRegex);

                        FieldOccurrence newOccur = new SimpleFieldOccurrence(occurrence.getFieldType());

                        newOccur.addValue(replacedValue);
                        return newOccur;
                    }
                }

            } catch (Exception e) {
                logger.error("Error getting value from occurrence", e);
            }

            // if no match, return null
            return null;
        }).filter(occurrence -> occurrence != null);


        // limit the number of occurrences to filter_limit, 0 means no limit
        if (filter_limit > 0)
            stream = stream.limit(filter_limit);

        // return the stream as a list
        return stream.collect(Collectors.toList());
    }



}
