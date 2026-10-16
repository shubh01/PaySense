package com.inn.paysense.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRequest (@JsonProperty("query") String query, @JsonProperty("fileCategory") FileCategory fileCategory) {
}
