"""
AML Transaction Dataset Generator
===================================
Generates realistic synthetic transaction data for the
AML Transaction Monitoring & SAR Generation System.

Output: transactions.json
Place at: backend/src/main/resources/data/transactions.json

Run:
    python generate_transactions.py
    python generate_transactions.py --count 1000 --output my_data.json

Fields per transaction:
    senderAccount       — originating account
    senderCountry       — country of origin       (OriginCountryRule)
    receiverAccount     — destination account
    receiverCountry     — destination country      (HighRiskCountryRule)
    receiverLastActive  — last activity of receiver account (DormantAccountRule)
    amount              — transaction amount in EUR
    currency            — always EUR
    timestamp           — ISO-8601 datetime

Rules covered:
    LargeAmountRule      amount > 10000                        +40 pts
    HighRiskCountryRule  receiverCountry in sanctioned list    +35 pts
    OriginCountryRule    senderCountry in sanctioned list      +25 pts
    StructuringRule      5 txs near 10k within 7 days         +35 pts
    VelocityRule         20+ txs from one account in 2h       +30 pts
    DormantAccountRule   receiverLastActive > 2 years ago      +25 pts
    RoundTripRule        repeated identical round amounts       +20 pts
"""

import json
import random
import argparse
from datetime import datetime, timedelta


# ─────────────────────────────────────────────
# CONFIG
# ─────────────────────────────────────────────

# Low-risk countries — normal business traffic
LOW_RISK_COUNTRIES = {
    "LU": 0.22,   # Luxembourg
    "DE": 0.18,   # Germany
    "FR": 0.15,   # France
    "GB": 0.10,   # United Kingdom
    "US": 0.08,   # United States
    "NL": 0.07,   # Netherlands
    "BE": 0.06,   # Belgium
    "CH": 0.05,   # Switzerland
    "AT": 0.05,   # Austria
    "SE": 0.04,   # Sweden
}

# High-risk countries — trigger HighRiskCountryRule / OriginCountryRule
HIGH_RISK_COUNTRIES = {
    "IR": 0.30,   # Iran — sanctioned
    "KP": 0.10,   # North Korea — sanctioned
    "RU": 0.30,   # Russia — high risk
    "BY": 0.15,   # Belarus — high risk
    "NG": 0.15,   # Nigeria — elevated risk
}

# Account pools
ACCOUNT_POOL    = [f"ACC-{str(i).zfill(4)}" for i in range(1000, 1200)]
DORMANT_ACCOUNTS = [f"DORM-{str(i).zfill(4)}" for i in range(9000, 9020)]

CURRENCY = "EUR"

# Simulation date range — last 6 months
DATE_START = datetime(2026, 1, 1, 0, 0, 0)
DATE_END   = datetime(2026, 6, 13, 23, 59, 59)

# Dormant threshold — last active more than 2 years before DATE_START
DORMANT_CUTOFF = DATE_START - timedelta(days=730)


# ─────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────

def low_risk_country(exclude=None):
    """Pick a random low-risk country."""
    exclude = set(exclude or [])
    pool = {k: v for k, v in LOW_RISK_COUNTRIES.items() if k not in exclude}
    return random.choices(list(pool.keys()), weights=list(pool.values()), k=1)[0]


def high_risk_country():
    """Pick a random high-risk / sanctioned country."""
    return random.choices(
        list(HIGH_RISK_COUNTRIES.keys()),
        weights=list(HIGH_RISK_COUNTRIES.values()),
        k=1
    )[0]


def any_country(exclude=None):
    """Pick from the full pool (mostly low-risk, occasionally high-risk)."""
    exclude = set(exclude or [])
    all_countries = {**LOW_RISK_COUNTRIES, **HIGH_RISK_COUNTRIES}
    pool = {k: v for k, v in all_countries.items() if k not in exclude}
    return random.choices(list(pool.keys()), weights=list(pool.values()), k=1)[0]


def random_ts():
    """Random timestamp within the simulation window."""
    delta = DATE_END - DATE_START
    return DATE_START + timedelta(seconds=random.randint(0, int(delta.total_seconds())))


def recent_last_active():
    """Last active date within the past 6 months — active account."""
    return (DATE_START - timedelta(days=random.randint(1, 180))).isoformat() + "Z"


def dormant_last_active():
    """Last active date more than 2 years ago — dormant account."""
    return (DORMANT_CUTOFF - timedelta(days=random.randint(0, 730))).isoformat() + "Z"


def random_account():
    return random.choice(ACCOUNT_POOL)


# ─────────────────────────────────────────────
# TRANSACTION PATTERNS
# ─────────────────────────────────────────────

def make_normal(sender, receiver, ts):
    """
    Clean transaction — no rules fire.
    Expected score: 0–20  →  status APPROVED
    """
    return {
        "senderAccount":      sender,
        "senderCountry":      low_risk_country(),
        "receiverAccount":    receiver,
        "receiverCountry":    low_risk_country(),
        "receiverLastActive": recent_last_active(),
        "amount":             round(random.uniform(50, 8_000), 2),
        "currency":           CURRENCY,
        "timestamp":          ts.isoformat() + "Z",
    }


def make_large_amount(sender, receiver, ts):
    """
    LargeAmountRule — amount > €10,000  →  +40 pts
    Expected score: 40  →  FLAGGED
    """
    return {
        "senderAccount":      sender,
        "senderCountry":      low_risk_country(),
        "receiverAccount":    receiver,
        "receiverCountry":    low_risk_country(),
        "receiverLastActive": recent_last_active(),
        "amount":             round(random.uniform(10_001, 80_000), 2),
        "currency":           CURRENCY,
        "timestamp":          ts.isoformat() + "Z",
    }


def make_high_risk_receiver(sender, receiver, ts):
    """
    HighRiskCountryRule — receiverCountry is sanctioned  →  +35 pts
    Expected score: 35–75 depending on amount  →  FLAGGED
    """
    return {
        "senderAccount":      sender,
        "senderCountry":      low_risk_country(),
        "receiverAccount":    receiver,
        "receiverCountry":    high_risk_country(),
        "receiverLastActive": recent_last_active(),
        "amount":             round(random.uniform(500, 25_000), 2),
        "currency":           CURRENCY,
        "timestamp":          ts.isoformat() + "Z",
    }


def make_high_risk_sender(sender, receiver, ts):
    """
    OriginCountryRule — senderCountry is sanctioned  →  +25 pts
    Expected score: 25–65 depending on amount  →  FLAGGED
    """
    return {
        "senderAccount":      sender,
        "senderCountry":      high_risk_country(),
        "receiverAccount":    receiver,
        "receiverCountry":    low_risk_country(),
        "receiverLastActive": recent_last_active(),
        "amount":             round(random.uniform(500, 20_000), 2),
        "currency":           CURRENCY,
        "timestamp":          ts.isoformat() + "Z",
    }


def make_structuring(sender, ts):
    """
    StructuringRule — 5 transactions near €10k limit within 7 days  →  +35 pts
    Returns a LIST of 5 transactions from the same sender.
    """
    txs = []
    for i in range(5):
        t = ts + timedelta(hours=random.randint(1, 36) * i)
        txs.append({
            "senderAccount":      sender,
            "senderCountry":      low_risk_country(),
            "receiverAccount":    random.choice(ACCOUNT_POOL),
            "receiverCountry":    low_risk_country(),
            "receiverLastActive": recent_last_active(),
            "amount":             round(random.uniform(9_200, 9_950), 2),
            "currency":           CURRENCY,
            "timestamp":          t.isoformat() + "Z",
        })
    return txs


def make_velocity(sender, ts):
    """
    VelocityRule — 20+ transactions from one account within 2 hours  →  +30 pts
    Returns a LIST of 22 rapid transactions from the same sender.
    """
    txs = []
    for i in range(22):
        t = ts + timedelta(minutes=random.randint(1, 5) * i)
        txs.append({
            "senderAccount":      sender,
            "senderCountry":      low_risk_country(),
            "receiverAccount":    random.choice(ACCOUNT_POOL),
            "receiverCountry":    low_risk_country(),
            "receiverLastActive": recent_last_active(),
            "amount":             round(random.uniform(100, 3_000), 2),
            "currency":           CURRENCY,
            "timestamp":          t.isoformat() + "Z",
        })
    return txs


def make_dormant(sender, ts):
    """
    DormantAccountRule — dormant receiver account (inactive 2yr+) gets large tx  →  +25 pts
    receiverLastActive is set more than 2 years before the simulation start.
    Expected score: 25–65  →  FLAGGED
    """
    return {
        "senderAccount":      sender,
        "senderCountry":      low_risk_country(),
        "receiverAccount":    random.choice(DORMANT_ACCOUNTS),
        "receiverCountry":    low_risk_country(),
        "receiverLastActive": dormant_last_active(),          # ← key field
        "amount":             round(random.uniform(5_000, 40_000), 2),
        "currency":           CURRENCY,
        "timestamp":          ts.isoformat() + "Z",
    }


def make_round_trip(sender, receiver, ts):
    """
    RoundTripRule — same sender→receiver pair, identical round amount, repeated  →  +20 pts
    Returns a LIST of 4 transactions.
    """
    amount = float(random.choice([5_000, 10_000, 15_000, 20_000, 25_000]))
    txs = []
    for i in range(4):
        t = ts + timedelta(days=i * 2)
        txs.append({
            "senderAccount":      sender,
            "senderCountry":      low_risk_country(),
            "receiverAccount":    receiver,
            "receiverCountry":    low_risk_country(),
            "receiverLastActive": recent_last_active(),
            "amount":             amount,
            "currency":           CURRENCY,
            "timestamp":          t.isoformat() + "Z",
        })
    return txs


def make_max_risk(sender, receiver, ts):
    """
    Multiple rules fire simultaneously — score capped at 100.
    LargeAmount (+40) + HighRiskCountry (+35) + OriginCountry (+25) = 100
    Most dramatic demo case.
    """
    risky = high_risk_country()
    return {
        "senderAccount":      sender,
        "senderCountry":      risky,                          # OriginCountryRule
        "receiverAccount":    receiver,
        "receiverCountry":    high_risk_country(),            # HighRiskCountryRule
        "receiverLastActive": recent_last_active(),
        "amount":             round(random.uniform(50_000, 200_000), 2),  # LargeAmountRule
        "currency":           CURRENCY,
        "timestamp":          ts.isoformat() + "Z",
    }


# ─────────────────────────────────────────────
# MAIN GENERATOR
# ─────────────────────────────────────────────

def generate(count=500):
    transactions = []

    # ── Guaranteed rule-triggering patterns ──

    # 5 structuring clusters (5 txs each = 25 txs)
    for _ in range(5):
        transactions.extend(make_structuring(random_account(), random_ts()))

    # 2 velocity bursts (22 txs each = 44 txs)
    for _ in range(2):
        transactions.extend(make_velocity(random_account(), random_ts()))

    # 3 round-trip patterns (4 txs each = 12 txs)
    for _ in range(3):
        transactions.extend(
            make_round_trip(random_account(), random_account(), random_ts())
        )

    # 10 high-risk receiver country
    for _ in range(10):
        transactions.append(
            make_high_risk_receiver(random_account(), random_account(), random_ts())
        )

    # 8 high-risk sender country (OriginCountryRule — new 7th rule)
    for _ in range(8):
        transactions.append(
            make_high_risk_sender(random_account(), random_account(), random_ts())
        )

    # 15 large amount transactions
    for _ in range(15):
        transactions.append(
            make_large_amount(random_account(), random_account(), random_ts())
        )

    # 10 dormant account transactions
    for _ in range(10):
        transactions.append(make_dormant(random_account(), random_ts()))

    # 5 max risk transactions (score = 100)
    for _ in range(5):
        transactions.append(
            make_max_risk(random_account(), random_account(), random_ts())
        )

    # ── Fill remaining slots with normal clean transactions ──
    remaining = max(0, count - len(transactions))
    for _ in range(remaining):
        transactions.append(
            make_normal(random_account(), random_account(), random_ts())
        )

    random.shuffle(transactions)
    return transactions


# ─────────────────────────────────────────────
# STATS SUMMARY
# ─────────────────────────────────────────────

def print_summary(transactions):
    total          = len(transactions)
    high_risk_set  = set(HIGH_RISK_COUNTRIES.keys())

    large          = sum(1 for t in transactions if t["amount"] > 10_000)
    risky_recv     = sum(1 for t in transactions if t["receiverCountry"] in high_risk_set)
    risky_send     = sum(1 for t in transactions if t["senderCountry"] in high_risk_set)
    dormant        = sum(1 for t in transactions if t["receiverAccount"].startswith("DORM-"))
    round_amounts  = sum(1 for t in transactions if t["amount"] % 1_000 == 0)

    print(f"\n{'─'*48}")
    print(f"  AML Dataset Generation Complete")
    print(f"{'─'*48}")
    print(f"  Total transactions        : {total}")
    print(f"  Large amount (>€10k)      : {large}  ({large/total*100:.1f}%)")
    print(f"  High-risk receiver country: {risky_recv}  ({risky_recv/total*100:.1f}%)")
    print(f"  High-risk sender country  : {risky_send}  ({risky_send/total*100:.1f}%)")
    print(f"  Dormant receivers         : {dormant}  ({dormant/total*100:.1f}%)")
    print(f"  Round amounts             : {round_amounts}  ({round_amounts/total*100:.1f}%)")
    print(f"{'─'*48}")
    print(f"  Rules covered             : 7 / 7")
    print(f"  Fields per transaction    : 8")
    print(f"  Estimated alerts (>40)    : ~{int(total * 0.30)}")
    print(f"  Estimated SARs   (>75)    : ~{int(total * 0.07)}")
    print(f"{'─'*48}\n")


# ─────────────────────────────────────────────
# ENTRY POINT
# ─────────────────────────────────────────────

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate AML transaction dataset")
    parser.add_argument("--count",  type=int, default=500,                 help="Number of transactions (default: 500)")
    parser.add_argument("--output", type=str, default="transactions.json", help="Output filename (default: transactions.json)")
    parser.add_argument("--seed",   type=int, default=42,                  help="Random seed for reproducibility (default: 42)")
    args = parser.parse_args()

    random.seed(args.seed)

    print(f"Generating {args.count} transactions (seed={args.seed})...")
    data = generate(args.count)

    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

    print(f"Saved → {args.output}")
    print_summary(data)
    print(f"  Next step: move {args.output} to")
    print(f"  backend/src/main/resources/data/transactions.json\n")
