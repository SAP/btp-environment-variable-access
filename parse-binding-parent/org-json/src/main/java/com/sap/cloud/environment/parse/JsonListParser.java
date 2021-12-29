package com.sap.cloud.environment.parse;

import org.json.JSONArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import com.sap.cloud.environment.api.ListParser;

public class JsonListParser implements ListParser {
    @Nullable
    @Override
    public List<Object> parseAsList(@Nonnull final String rawList) {
        try {
            return new JSONArray(rawList).toList();
        } catch (final Exception e) {
            return null;
        }
    }
}
