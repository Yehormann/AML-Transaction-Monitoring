# AML Transaction Monitoring & SAR Generation System
## AI Context Prompt — for development assistant setup

---

## WHO WE ARE

Two university students at the University of Luxembourg building a software engineering course project. The goal is to demonstrate team collaboration via GitHub (branching, pull requests, code review), CI/CD pipelines, testing, and full-stack Java development. The project must also serve as a serious portfolio piece targeting the Luxembourg fintech/banking job market.

---

## WHAT WE ARE BUILDING

**Project name:** AML Transaction Monitoring & SAR Generation System

**Domain:** Anti-Money Laundering (AML) — RegTech / FinTech compliance tooling

**One-line description:** A Spring Boot REST API backend with a React dashboard that ingests financial transactions, scores them for fraud risk using a rule engine, manages compliance analyst alerts, and auto-generates Suspicious Activity Reports (SARs) as PDF documents.

**Why this project:** AML compliance is a legal obligation for every bank and fintech in Luxembourg (regulated by the CSSF). Every financial institution needs tooling that monitors transactions, flags suspicious activity, and files SARs with the FIU (Financial Intelligence Unit). We are building a simplified but architecturally realistic version of this tooling.

---

## FULL TECH STACK

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security — HTTP Basic (ANALYST / ADMIN roles) |
| Persistence | Spring Data JPA + PostgreSQL 15 |
| DB Migrations | Flyway |
| PDF Generation | iText 7 |
| Frontend | React 18 + Vite + Recharts |
| Testing | JUnit 5 + Mockito + H2 in-memory |
| Containerisation | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Deployment | Render (free tier) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |

**No machine learning. No external AI API calls.** Rule-based scoring only — intentional decision for scope and interview defensibility.

---

## SYSTEM ARCHITECTURE — 5 LAYERS

```
Layer 1 — Data Input
  Java mock transaction generator + optional Kaggle seed data
  REST POST /api/transactions

Layer 2 — Spring Security
  HTTP Basic auth — two hardcoded roles: ANALYST, ADMIN
  Protects all endpoints

Layer 3 — REST Controllers (@RestController)
  TransactionController  → POST /api/transactions, GET /api/transactions
  AlertController        → GET /api/alerts, PATCH /api/alerts/{id}/dismiss, PATCH /api/alerts/{id}/escalate
  SARController          → GET /api/reports/sar/{id}
  DashboardController    → GET /api/dashboard/stats

Layer 4 — Services + Rule Engine
  TransactionService  → validate, persist, trigger rule engine, create alert if score > 40
  AlertService        → manage alert lifecycle (OPEN → DISMISSED or ESCALATED)
  SARService          → generate PDF report, write audit log entry SAR_FILED
  DashboardService    → aggregate stats and chart data

  Rule Engine (Strategy Pattern):
    Interface: FraudRule → evaluate(Transaction) → RuleResult(score contribution, reason)
    Rules:
      LargeAmountRule      → single transaction > €10,000              → +40 pts
      StructuringRule      → 5 transactions near €10k limit in 7 days  → +35 pts
      HighRiskCountryRule  → transfer to sanctioned/high-risk country   → +35 pts
      VelocityRule         → 20+ transactions from one account in 2hrs  → +30 pts
      DormantAccountRule   → account inactive 2yr+ receives large tx    → +25 pts
      RoundTripRule        → repeated identical round amounts           → +20 pts
    Final score = sum of all fired rule contributions, capped at 100

Layer 5 — Data + DevOps
  PostgreSQL tables: transactions, alerts, sar_reports, audit_log
  Flyway migrations: V1 through V4
  JUnit unit tests per rule + integration tests with H2
  GitHub Actions: build → test → Docker image → deploy to Render
  Docker Compose: spring boot app + postgres, single command startup
```

---

## TRANSACTION LIFECYCLE (step by step)

```
1. Transaction submitted via POST /api/transactions
2. TransactionService validates fields
3. Rule engine runs all 6 rules, sums weighted score → risk score 0-100
4. Transaction persisted with score + list of fired rules
   → score ≤ 40  : status APPROVED, no alert created
   → score > 40  : alert created with status OPEN
5. Analyst opens dashboard, reviews alert
   → Dismiss: adds note, status → DISMISSED, audit log entry written
   → Escalate: status → ESCALATED, SARService triggered
6. SARService generates PDF containing:
   - Transaction metadata
   - Risk score + all fired rules with individual contributions
   - Analyst notes
   - Full timestamped audit trail
   - SAR_FILED audit log entry written
7. PDF available at GET /api/reports/sar/{id}
```

---

## DATABASE SCHEMA (4 tables)

**transactions**
- id (UUID), sender_account, receiver_account, amount, currency, country, timestamp, risk_score, status (APPROVED/FLAGGED), fired_rules (JSON), created_at

**alerts**
- id (UUID), transaction_id (FK), risk_score_snapshot, status (OPEN/DISMISSED/ESCALATED), analyst_note, created_at, updated_at

**sar_reports**
- id (UUID), alert_id (FK), pdf_path, filed_at, created_at

**audit_log**
- id (UUID), entity_type, entity_id, action (ALERT_CREATED / ALERT_DISMISSED / ALERT_ESCALATED / SAR_FILED), performed_by, note, timestamp

---

## FUNCTIONAL REQUIREMENTS (3 core)

**FR-1: Transaction Ingestion and Risk Scoring**
Accept transaction via REST, validate, run rule engine, return scored transaction with HTTP 201.

**FR-2: Alert Lifecycle Management**
Auto-create alert when score > 40. Analyst can dismiss (with note) or escalate. Every transition logged to audit_log.

**FR-3: SAR Generation**
Auto-trigger SAR when score > 75 or manual escalation. Generate PDF with full details. Persist SAR record. Log SAR_FILED.

---

## PROJECT STRUCTURE (recommended repo layout)

```
aml-monitoring/
├── backend/
│   ├── src/main/java/com/aml/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── engine/          ← rule engine lives here
│   │   │   ├── FraudRule.java
│   │   │   ├── RuleResult.java
│   │   │   ├── RuleEngineService.java
│   │   │   └── rules/       ← one class per rule
│   │   ├── model/           ← JPA entities
│   │   ├── repository/
│   │   ├── dto/
│   │   └── report/          ← iText SAR PDF generator
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/    ← Flyway SQL scripts
│   └── src/test/
├── frontend/                ← React app
├── docker-compose.yml
├── .github/workflows/ci.yml
└── README.md
```

---

## WHAT IS ALREADY DECIDED — DO NOT CHANGE

- No JWT — HTTP Basic only (intentional, saves time)
- No ML / AI API calls — rule-based scoring only
- No microservices — single monolithic Spring Boot app
- PostgreSQL in production, H2 only for tests
- iText 7 for PDF generation
- React frontend (not Angular, not Thymeleaf)
- Flyway for all schema changes (no manual SQL setup)
- Deploy to Render free tier

---

## TEAM SPLIT (context for who owns what)

- **Developer 1 (me):** Spring Boot backend, rule engine, REST API, JPA entities, Flyway, iText SAR generator, GitHub Actions CI/CD
- **Developer 2 (you):** React frontend dashboard, Docker Compose, integration testing, README documentation

Both collaborate on: GitHub branching strategy, pull request reviews, test writing

---

## HOW TO USE THIS PROMPT

Paste this entire document at the start of your AI chat session as the system context. Then ask specific questions like:

- "Generate the FraudRule interface and RuleResult class"
- "Write the LargeAmountRule implementation"
- "Create the Transaction JPA entity"
- "Write the GitHub Actions CI workflow"
- "Build the React alert review component"
- "Write a JUnit test for StructuringRule"

The AI will have full context of what we are building, the stack, the architecture, and the decisions already made — so answers will be precise and consistent with the rest of the project.
