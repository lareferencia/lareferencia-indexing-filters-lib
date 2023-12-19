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
import java.util.stream.Stream;

@Component
public class OldestDateFieldOccurrenceFilter implements IFieldOccurrenceFilter {

    public static final String OLDEST_DATE = "oldest-date";
    
    @Autowired
    private DateHelper dateHelper;

    private static Logger logger = LogManager.getLogger(OldestDateFieldOccurrenceFilter.class);

    public String getName() { return OLDEST_DATE; }

    public Collection<FieldOccurrence> filter(Collection<FieldOccurrence> occurrences, Map<String,String> params) {

        List<FieldOccurrence> result = new ArrayList<FieldOccurrence>();

        int filter_limit = params.containsKey("filter-limit") ? Integer.parseInt(params.get("filter-limit")) : 0;
        String filter_date_format = params.containsKey("filter-date-format") ? params.get("filter-date-format") : "yyyy-MM-dd";

        // get preferred param
        boolean preferred = params.containsKey("preferred") ? Boolean.parseBoolean(params.get("preferred")) : false;

        // if preferred is true and there is a preferred occurrence, filter by preferred
        if (preferred && occurrences.stream().anyMatch(occurrence -> occurrence.getPreferred() == true) )
            occurrences = occurrences.stream().filter(occurrence -> occurrence.getPreferred() == true).collect(Collectors.toList());

        if (occurrences.size() == 0)
            return result;

        Stream<FieldOccurrence> stream = null;

        // if there is only one occurrence, dont filter by min date
        if (occurrences.size() == 1) {
            stream = occurrences.stream();
        } else { // filter by min date
            LocalDateTime minTime = occurrences.stream().map(occ -> getLocalDateTime(occ, params)).min(compareLocalDateTimes).get();
            stream = occurrences.stream().filter(occurrence -> getLocalDateTime(occurrence, params) != null && getLocalDateTime(occurrence, params).equals(minTime) );
        }

        // limit the number of occurrences to filter_limit, 0 means no limit
        if (filter_limit > 0)
            stream = stream.limit(filter_limit);

        // map to new occurrences
        stream.map(fo -> {
        LocalDateTime ldt = getLocalDateTime(fo, params);
        if (ldt != null) {
            return new SimpleFieldOccurrence(DateHelper.getDateTimeFormattedString(ldt, filter_date_format));    
        }
        return null;
        })
        .filter(Objects::nonNull)
        .forEach(result::add);

        return result;
    }

    // TODO: move this to a helper class
    Comparator<LocalDateTime> compareLocalDateTimes = (LocalDateTime o1, LocalDateTime o2) -> {

        // null cases
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        }

        if (o1.isBefore(o2)) {
            return -1;
        } else if (o1.isAfter(o2)) {
            return 1;
        } else {
            return 0;
        }
    };

    private LocalDateTime getLocalDateTime(FieldOccurrence a, Map<String,String> params) {

        String value = "NULL";

        try {
            value = a.getValue();
            return dateHelper.parseDate(value);
        } catch (Exception e) {
            logger.debug("Error parsing date " + value + " in filtering occurrences  " + this.getName(), e);
            return null;
        }

    }
}
