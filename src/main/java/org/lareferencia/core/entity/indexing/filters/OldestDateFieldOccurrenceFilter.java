package org.lareferencia.core.entity.indexing.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lareferencia.core.entity.domain.EntityRelationException;
import org.lareferencia.core.entity.domain.FieldOccurrence;
import org.lareferencia.core.entity.domain.SimpleFieldOccurrence;
import org.lareferencia.core.util.date.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OldestDateFieldOccurrenceFilter implements IFieldOccurrenceFilter {

    public static final String OLDEST_DATE = "oldest-date";
    
    @Autowired
    private DateHelper dateHelper;

    private static Logger logger = LogManager.getLogger(OldestDateFieldOccurrenceFilter.class);

    public String getName() { return OLDEST_DATE; }

    public Collection<FieldOccurrence> filter(Collection<FieldOccurrence> occurrences, Map<String,String> params) {

        List<FieldOccurrence> result = new ArrayList<FieldOccurrence>();

        int filter_limit = params.containsKey("filter-limit") ? Integer.parseInt(params.get("filter-limit")) : 1;
        String filter_date_format = params.containsKey("filter-date-format") ? params.get("filter-date-format") : "yyyy-MM-dd";

        Collection<FieldOccurrence> filteredOccurrences = occurrences.stream()
                .filter(occurrence -> getLocalDateTime(occurrence, params).equals(occurrences.stream()
                        .map(occ -> getLocalDateTime(occ, params))
                        .min(compareLocalDateTimes).get()))
                .collect(Collectors.toList());


        // limit the number of occurrences to filter_limit
        filteredOccurrences = filteredOccurrences.stream().limit(filter_limit).collect(Collectors.toList());


        for (FieldOccurrence fo : filteredOccurrences) {

            LocalDateTime ldt = getLocalDateTime(fo, params);
            // create a new FieldOccurrence in replace of the existing one, the new one is built by selected subfields formatted as filterFormat
            FieldOccurrence newOccur = new SimpleFieldOccurrence(fo.getFieldType());

            try {
                newOccur.addValue( DateHelper.getDateTimeFormattedString(ldt, filter_date_format) );
                result.add(newOccur);
            } catch (EntityRelationException e) {
                logger.error("Error filtering date occurrences " + this.getName() + " ", e);
            }

        }

        return result;
    }

    // TODO: move this to a helper class
    Comparator<LocalDateTime> compareLocalDateTimes = (LocalDateTime o1, LocalDateTime o2) -> {
        if (o1.isBefore(o2)) {
            return -1;
        } else if (o1.isAfter(o2)) {
            return 1;
        } else {
            return 0;
        }
    };

    private LocalDateTime getLocalDateTime(FieldOccurrence a, Map<String,String> params) {

        try {
            return dateHelper.parseDate( a.getValue() );
        } catch (EntityRelationException e) {
            logger.error("Error filtering occurrences " + this.getName() + " ", e);
        }
        return LocalDateTime.now();
    }
}
