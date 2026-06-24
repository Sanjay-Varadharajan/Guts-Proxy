package com.guts.proxy.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MasterKeyValidator {

    @Value("${security.master-key}")
    private String masterKey;

    public boolean isValid(String providedKey) {

        return providedKey != null
                && providedKey.equals(masterKey);
    }
}
