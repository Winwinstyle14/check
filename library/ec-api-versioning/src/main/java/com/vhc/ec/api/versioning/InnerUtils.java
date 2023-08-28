package com.vhc.ec.api.versioning;

import java.util.regex.Pattern;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public class InnerUtils {
    private final static Pattern VERSION_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+){0,2}$");

    /**
     * Check if the version matches are composite (up to three versions)
     *
     * @param version
     * @param targetMethodOrType
     */
    public static void checkVersionNumber(String version, Object targetMethodOrType) {
        if (!matchVersionNumber(version)) {
            throw new IllegalArgumentException(String.format("Invalid version number: @ApiVersion(\"%s\") at %s", version, targetMethodOrType));
        }
    }

    /**
     * Determine whether the match of the maximum 3 version numbers is met
     *
     * @param version
     * @return
     */
    public static boolean matchVersionNumber(String version) {
        return version.length() != 0 && VERSION_NUMBER_PATTERN.matcher(version).find();
    }
}
