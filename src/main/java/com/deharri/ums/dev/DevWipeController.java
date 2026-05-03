package com.deharri.ums.dev;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <strong>Dev-only.</strong> Wipes every UMS table. Intended for the seed-data tooling
 * (start.html "Empty Database" button) so the developer can reset state without tearing
 * the Docker volumes down. Do NOT expose this in production — gate it behind a profile
 * or feature flag if you ever harden the deployment.
 *
 * <p>Uses a Postgres anonymous DO block to discover every public-schema table and
 * TRUNCATE it with CASCADE+RESTART IDENTITY. That way new tables added later are
 * wiped automatically without needing to update the list.
 */
@RestController
@RequestMapping("/api/v1/dev")
@Slf4j
public class DevWipeController {

    @PersistenceContext
    private EntityManager em;

    @DeleteMapping("/wipe")
    @Transactional
    public ResponseEntity<Map<String, Object>> wipe() {
        log.warn("DEV WIPE: clearing all UMS tables");
        em.createNativeQuery(
                "DO $$ DECLARE r RECORD; " +
                "BEGIN FOR r IN " +
                "  SELECT tablename FROM pg_tables " +
                "  WHERE schemaname = 'public' " +
                "LOOP " +
                "  EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE'; " +
                "END LOOP; END $$;"
        ).executeUpdate();
        return ResponseEntity.ok(Map.of("service", "user-mgmt-service", "wiped", true));
    }
}
