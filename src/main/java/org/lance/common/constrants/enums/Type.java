package org.lance.common.constrants.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Pattern;

public enum Type {
    @JsonProperty("BILIBILI")
    BILIBILI(new Pattern[]{
            Pattern.compile("https?://(www\\.)?bilibili\\.com"),
            Pattern.compile("av(\\d+)"),
            Pattern.compile("BV(\\S+)"),
            Pattern.compile("ss(\\d+)"),
            Pattern.compile("md(\\d+)"),
            Pattern.compile("ep(\\d+)")
    }),

    @JsonProperty("ACFUN")
    ACFUN(new Pattern[]{Pattern.compile("https?://(www\\.)?acfun\\.cn"),
            Pattern.compile("ac([0-9]+)"),
            Pattern.compile("aa([0-9]+)")
    }),

    @JsonProperty("IMOMOE_LA")
    IMOMOE_LA(new Pattern[]{Pattern.compile("http?://(www\\.)?imomoe\\.la/view/([0-9]+)\\.html"),
            Pattern.compile("http?://(www\\.)?imomoe\\.la/player/[0-9]+-[0-9]+-[0-9]+\\.html")
    });

    public boolean verify(String url) {
        for (Pattern type : types) {
            if (type.matcher(url).find()) {
                return true;
            }
        }
        return false;
    }

    private final Pattern[] types;

    Type(Pattern[] types) {
        this.types = types;
    }
}
