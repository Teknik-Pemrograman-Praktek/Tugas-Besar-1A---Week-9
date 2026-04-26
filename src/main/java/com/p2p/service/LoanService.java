package com.p2p.service;
import com.p2p.domain.*;
import java.math.BigDecimal;

public class LoanService {

    public Loan createLoan(Borrower borrower, BigDecimal amount) {

        // VALIDASI (delegasi ke domain)
        validateBorrower(borrower);

        // CREATE LOAN (domain object)
        Loan loan = new Loan();

        // BUSINESS ACTION (domain behavior)
        if (borrower.getCreditScore() >= 600) {
            loan.approve();
        } else {
            loan.reject();
        }

        return loan;
    }

    // PRIVATE VALIDATION METHOD
    private void validateBorrower(Borrower borrower) {
        if (!borrower.canApplyLoan()) {
            throw new IllegalArgumentException("Borrower not verified");
        }
    }
}

