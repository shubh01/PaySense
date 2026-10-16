package com.inn.paysense.advisor;

import com.inn.paysense.utility.Constants;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class GeneralQueryResolver {

    private static final List<Pattern> GENERAL_QUERIES = List.of(Pattern.compile(Constants.GENERAL_QUERY_PATTERN_1),
            Pattern.compile(Constants.GENERAL_QUERY_PATTERN_2), Pattern.compile(Constants.GENERAL_QUERY_PATTERN_3), Pattern.compile(Constants.GENERAL_QUERY_PATTERN_4));

    public Optional<String> resolve(String query){
        if(query == null || query.isBlank()){
            return Optional.empty();
        }

        String sanitizedQuery = query.trim();

        for(Pattern pattern: GENERAL_QUERIES){
            if(pattern.matcher(sanitizedQuery).matches()){
                return Optional.of(Constants.GENERAL_QUERY_RESPONSE);
            }
        }
        return Optional.empty();
    }

}
