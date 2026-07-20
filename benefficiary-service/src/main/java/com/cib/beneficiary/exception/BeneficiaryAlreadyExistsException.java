package com.cib.beneficiary.exception;

public class BeneficiaryAlreadyExistsException extends RuntimeException {
    public BeneficiaryAlreadyExistsException(String message) {
        super(message);
    }
}
