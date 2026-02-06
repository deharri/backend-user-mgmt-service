package com.deharri.ums.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Language {
    URDU("Urdu"),
    ENGLISH("English"),
    PUNJABI("Punjabi"),
    SINDHI("Sindhi"),
    PASHTO("Pashto"),
    BALOCHI("Balochi"),
    SARAIKI("Saraiki");

    private final String displayName;
}
