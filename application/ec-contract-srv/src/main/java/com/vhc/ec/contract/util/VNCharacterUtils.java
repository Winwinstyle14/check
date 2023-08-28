package com.vhc.ec.contract.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class VNCharacterUtils {
    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        return pattern.matcher(temp).replaceAll("")
                .replaceAll("Đ", "D")
                .replace("đ", "d");
    }
}
