package com.sap.cloud.environment.servicebinding;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;

import com.sap.cloud.environment.api.ServiceBinding;
import com.sap.cloud.environment.parse.JsonMapParser;

import static org.assertj.core.api.Assertions.assertThat;

class SecretRootKeyParsingStrategyTest {

    @Test
    void emptyDirectoryLeadsToNull() throws IOException {
        final Path emptyDirectoryPath = TestResource.get(SecretRootKeyParsingStrategyTest.class, "EmptyDirectory");

        final SecretRootKeyParsingStrategy sut = new SecretRootKeyParsingStrategy(StandardCharsets.UTF_8,
                new JsonMapParser(),
                Collections.emptyMap(),
                PropertySetter.TO_ROOT);

        final ServiceBinding serviceBinding = sut.parse("none", "empty", emptyDirectoryPath);

        assertThat(serviceBinding).isNull();
    }
}