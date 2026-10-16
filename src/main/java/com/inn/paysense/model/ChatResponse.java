package com.inn.paysense.model;

import java.time.LocalDateTime;

public record ChatResponse (String prompt, String response, LocalDateTime localDateTime){};
