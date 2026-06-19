package dnsolutions.fi.SelfDevelopment.util;

import dnsolutions.fi.SelfDevelopment.exception.BadRequestException;

public final class FieldUpdateUtils {

    private FieldUpdateUtils() {
    }

    public static String readStringField(String field, Object value) {
        if (!(value instanceof String stringValue)) {
            throw new BadRequestException(field + " must be a string");
        }
        return stringValue;
    }
}
