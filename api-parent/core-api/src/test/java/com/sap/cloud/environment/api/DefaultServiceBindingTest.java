package com.sap.cloud.environment.api;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultServiceBindingTest {

    @Test
    void copyOfCopiesMap() {
        final Map<String, Object> input = new HashMap<>();
        input.put("Key", "Value");

        final DefaultServiceBinding sut = DefaultServiceBinding.copyOf(input);

        assertThat(sut.getKeys()).containsExactlyInAnyOrder("Key");

        // modify input
        input.put("AnotherKey", "AnotherValue");

        // assert that service binding was not modified
        assertThat(sut.getKeys()).containsExactlyInAnyOrder("Key");
    }

    @Test
    @SuppressWarnings("unchecked")
    void copyOfCopiesNestedMap() {
        final Map<String, Object> nestedMap = new HashMap<>();
        final Map<String, Object> input = Collections.singletonMap("Map", nestedMap);

        final DefaultServiceBinding sut = DefaultServiceBinding.copyOf(input);

        assertThat(sut.getKeys()).containsExactlyInAnyOrder("Map");
        assertThat(sut.get("Map")).isInstanceOf(Map.class);
        assertThat((Map<String, Object>) sut.get("Map")).isEmpty();

        // modify nested map
        nestedMap.put("Key", "Value");

        // assert that service binding was not modified
        assertThat(sut.get("Map")).isInstanceOf(Map.class);
        assertThat((Map<String, Object>) sut.get("Map")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void copyOfCopiesCollection() {
        final Collection<Object> collection = new ArrayList<>();
        final Map<String, Object> input = Collections.singletonMap("Collection", collection);

        final DefaultServiceBinding sut = DefaultServiceBinding.copyOf(input);

        assertThat(sut.getKeys()).containsExactlyInAnyOrder("Collection");
        assertThat(sut.get("Collection")).isInstanceOf(Collection.class);
        assertThat((Collection<Object>) sut.get("Collection")).isEmpty();

        // modify collection
        collection.add("New Entry");

        // assert that service binding was not modified
        assertThat(sut.getKeys()).containsExactlyInAnyOrder("Collection");
        assertThat(sut.get("Collection")).isInstanceOf(Collection.class);
        assertThat((Collection<Object>) sut.get("Collection")).isEmpty();
    }

}