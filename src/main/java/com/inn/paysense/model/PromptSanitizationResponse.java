package com.inn.paysense.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PromptSanitizationResponse(
        @JsonProperty("result") String result,
        @JsonProperty("reason") String reason) {};
