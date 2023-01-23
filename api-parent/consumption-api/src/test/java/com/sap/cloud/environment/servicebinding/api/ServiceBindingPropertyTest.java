package com.sap.cloud.environment.servicebinding.api;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceBindingPropertyTest
{

    @Test
    void testStringCollectionComparesCaseInsensitivelyByDefault()
    {
        final ServiceBinding binding =
            DefaultServiceBinding
                .builder()
                .copy(Collections.emptyMap())
                .withTags(Collections.singletonList("Foo"))
                .build();
        final ServiceBindingProperty.StringCollectionFilterBuilder sut =
            new ServiceBindingProperty.StringCollectionFilterBuilder(ServiceBinding::getTags);

        assertThat(sut.contains("foo").matches(binding)).isTrue();
        assertThat(sut.contains("FOO").matches(binding)).isTrue();
        assertThat(sut.contains("fOo").matches(binding)).isTrue();
        assertThat(sut.contains("FoO").matches(binding)).isTrue();
    }
}
