package com.deharri.ums.util;

import com.deharri.ums.error.exception.CustomDataIntegrityViolationException;

public final class PhoneNumberNormalizer {

    private PhoneNumberNormalizer() {}

    public static String normalizeToE164(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new CustomDataIntegrityViolationException("Phone number is required");
        }
        String digits = raw.replaceAll("[^0-9+]", "");

        if (digits.startsWith("+92") && digits.length() == 13) {
            return digits;
        }
        if (digits.startsWith("0092") && digits.length() == 14) {
            return "+" + digits.substring(2);
        }
        if (digits.startsWith("92") && digits.length() == 12) {
            return "+" + digits;
        }
        if (digits.startsWith("0") && digits.length() == 11) {
            return "+92" + digits.substring(1);
        }
        throw new CustomDataIntegrityViolationException(
                "Invalid Pakistani phone number. Use formats like 03001234567 or +923001234567");
    }

    public static String mask(String e164Phone) {
        if (e164Phone == null || e164Phone.length() < 4) return "******";
        String last4 = e164Phone.substring(e164Phone.length() - 4);
        return "+92******" + last4;
    }
}
