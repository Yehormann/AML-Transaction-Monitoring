package com.aml.engine;

import com.aml.model.Transaction;

public interface FraudRule {
    RuleResult evaluate(Transaction transaction);
}
