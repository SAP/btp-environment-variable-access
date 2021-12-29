package com.sap.cloud.environment.parse;

import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import com.sap.cloud.environment.api.MapParser;

public class JsonMapParser implements MapParser {
    @Nullable
    @Override
    public Map<String, Object> parseAsMap(@Nonnull final String rawMapView) {
        try {
            return new JSONObject(rawMapView).toMap();
        } catch (final Exception e) {
            return null;
        }
    }
}
