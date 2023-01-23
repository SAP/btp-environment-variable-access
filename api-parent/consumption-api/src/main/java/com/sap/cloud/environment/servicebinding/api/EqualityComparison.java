package com.sap.cloud.environment.servicebinding.api;

import javax.annotation.Nullable;

@FunctionalInterface
public interface EqualityComparison<T>
{
    final class String
    {
        public static EqualityComparison<java.lang.String> CASE_SENSITIVE = ( a, b ) -> {
            if( a == null || b == null ) {
                return false;
            }

            return a.contentEquals(b);
        };

        public static EqualityComparison<java.lang.String> CASE_INSENSITIVE = ( a, b ) -> {
            if( a == null || b == null ) {
                return false;
            }

            return a.equalsIgnoreCase(b);
        };

        private String()
        {
            throw new IllegalStateException("This static utility class must not be instantiated!");
        }
    }

    boolean areEqual( @Nullable final T a, @Nullable final T b );
}
