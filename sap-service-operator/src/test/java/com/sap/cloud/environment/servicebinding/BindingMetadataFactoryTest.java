/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BindingMetadataFactoryTest
{
    @Nonnull
    private static final BindingProperty TYPE_PROPERTY = BindingProperty.text("type", "type.txt");

    @Nonnull
    private static final BindingProperty TAGS_PROPERTY = BindingProperty.json("tags", "tags.json");

    @Nonnull
    private static final BindingProperty USER_PROPERTY = BindingProperty.text("user", "user.secret");

    @Nonnull
    private static final BindingProperty PASSWORD_PROPERTY = BindingProperty.text("password", "password.secret");

    @Nonnull
    private static final BindingProperty UAA_PROPERTY = BindingProperty.container("uaa", "uaa.json");

    @Test
    void fromJsonWithFlatMetadata()
    {
        final String rawMetadata = TestResource.read(BindingMetadataFactoryTest.class, "flat_metadata.json");

        final BindingMetadata sut = BindingMetadataFactory.getFromJson(rawMetadata);

        assertThat(sut).isNotNull();
        assertThat(sut.getMetadataProperties().size()).isEqualTo(2);
        assertThat(sut.getCredentialProperties().size()).isEqualTo(2);

        assertThat(sut.getMetadataProperties()).containsExactlyInAnyOrder(TYPE_PROPERTY, TAGS_PROPERTY);
        assertThat(sut.getCredentialProperties()).containsExactlyInAnyOrder(USER_PROPERTY, PASSWORD_PROPERTY);
    }

    @Test
    void fromJsonWithContainerMetadata()
    {
        final String rawMetadata = TestResource.read(BindingMetadataFactoryTest.class, "container_metadata.json");

        final BindingMetadata sut = BindingMetadataFactory.getFromJson(rawMetadata);

        assertThat(sut).isNotNull();
        assertThat(sut.getMetadataProperties().size()).isEqualTo(2);
        assertThat(sut.getCredentialProperties().size()).isEqualTo(1);

        assertThat(sut.getMetadataProperties()).containsExactlyInAnyOrder(TYPE_PROPERTY, TAGS_PROPERTY);
        assertThat(sut.getCredentialProperties()).containsExactlyInAnyOrder(UAA_PROPERTY);
    }

    @Test
    void fromJsonWithMultipleContainerMetadata()
    {
        final String rawMetadata = TestResource.read(BindingMetadataFactoryTest.class, "multi_container_metadata.json");

        final BindingMetadata sut = BindingMetadataFactory.getFromJson(rawMetadata);

        assertThat(sut).isNotNull();
        assertThat(sut.getMetadataProperties().size()).isEqualTo(2);
        assertThat(sut.getCredentialProperties().size()).isEqualTo(2);

        assertThat(sut.getMetadataProperties()).containsExactlyInAnyOrder(TYPE_PROPERTY, TAGS_PROPERTY);
        assertThat(sut.getCredentialProperties())
            .containsExactlyInAnyOrder(
                BindingProperty.container("container1"),
                BindingProperty.container("container2"));
    }

    @Test
    void fromJsonWithInvalidInputThrowsIllegalArgumentException()
    {
        assertThatThrownBy(() -> BindingMetadataFactory.getFromJson("this is not a valid JSON object"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasCauseExactlyInstanceOf(JSONException.class);
    }

    @Test
    void fromJsonWithEmptyInputLeadsToEmptyMetadata()
    {
        final BindingMetadata sut = BindingMetadataFactory.getFromJson("{}");

        assertThat(sut).isNotNull();
        assertThat(sut.getMetadataProperties()).isEmpty();
        assertThat(sut.getCredentialProperties()).isEmpty();
    }

    @Test
    void fromJsonIgnoresPropertiesWithMissingFields()
    {
        final String rawMetadata = TestResource.read(BindingMetadataFactoryTest.class, "malformed_metadata.json");

        final BindingMetadata sut = BindingMetadataFactory.getFromJson(rawMetadata);

        assertThat(sut).isNotNull();
        assertThat(sut.getMetadataProperties().size()).isEqualTo(1);
        assertThat(sut.getCredentialProperties().size()).isEqualTo(1);

        assertThat(sut.getMetadataProperties()).containsExactlyInAnyOrder(TYPE_PROPERTY);
        assertThat(sut.getCredentialProperties()).containsExactlyInAnyOrder(USER_PROPERTY);
    }
}
