package com.deharri.ums.verification;

import com.deharri.ums.error.exception.CustomDataIntegrityViolationException;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioVerifyServiceImpl implements TwilioVerifyService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.verify-sid:}")
    private String verifySid;

    @Value("${twilio.bypass-otp:false}")
    private boolean bypassOtp;

    private boolean initialized = false;

    @PostConstruct
    void init() {
        if (bypassOtp) {
            log.warn("Twilio bypass-otp is ENABLED. Magic code '000000' will be accepted. " +
                    "Disable in production.");
            return;
        }
        if (accountSid == null || accountSid.isBlank() ||
                authToken == null || authToken.isBlank() ||
                verifySid == null || verifySid.isBlank()) {
            log.warn("Twilio credentials missing - verification will fail until configured.");
            return;
        }
        Twilio.init(accountSid, authToken);
        initialized = true;
        log.info("Twilio Verify initialized for service {}", verifySid);
    }

    @Override
    public void sendOtp(String e164PhoneNumber) {
        if (bypassOtp) {
            log.info("[BYPASS] Pretending to send OTP to {}", e164PhoneNumber);
            return;
        }
        if (!initialized) {
            throw new CustomDataIntegrityViolationException(
                    "SMS service is not configured. Please contact support.");
        }
        try {
            Verification v = Verification.creator(verifySid, e164PhoneNumber, "sms").create();
            log.info("OTP sent to {} (status: {})", e164PhoneNumber, v.getStatus());
        } catch (ApiException e) {
            log.error("Twilio sendOtp failed for {}: code={} message={}",
                    e164PhoneNumber, e.getCode(), e.getMessage());
            throw new CustomDataIntegrityViolationException(
                    "Failed to send verification code. Please try again.");
        }
    }

    @Override
    public boolean checkOtp(String e164PhoneNumber, String code) {
        if (bypassOtp) {
            boolean ok = "000000".equals(code);
            log.info("[BYPASS] checkOtp({}, ****): {}", e164PhoneNumber, ok);
            return ok;
        }
        if (!initialized) return false;
        try {
            VerificationCheck check = VerificationCheck.creator(verifySid)
                    .setTo(e164PhoneNumber).setCode(code).create();
            return "approved".equals(check.getStatus());
        } catch (ApiException e) {
            log.warn("Twilio checkOtp failed for {}: code={} message={}",
                    e164PhoneNumber, e.getCode(), e.getMessage());
            return false;
        }
    }
}
