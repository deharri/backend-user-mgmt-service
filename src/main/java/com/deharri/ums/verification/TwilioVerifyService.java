package com.deharri.ums.verification;

public interface TwilioVerifyService {
    void sendOtp(String e164PhoneNumber);
    boolean checkOtp(String e164PhoneNumber, String code);
}
