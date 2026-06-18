package com.aml.engine;

public record RuleResult(String ruleName, int score, String reason) {

    public boolean fired() {
        return score > 0;
    }
}
