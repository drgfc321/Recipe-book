package com.recipebook.service;

import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.elytron.security.common.BcryptUtil;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PasswordService {

    private static final Logger LOG = Logger.getLogger(PasswordService.class);

    public String hashPassword(String plainPassword) {
        LOG.debug("Hashing password");
        return BcryptUtil.bcryptHash(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        LOG.debug("Verifying password");
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
