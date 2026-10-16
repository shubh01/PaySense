# PaySense — Credit Card & Loan Terms Assistant

## 1. Project Overview

PaySense is a Retrieval-Augmented Generation (RAG) system that lets everyday customers (and bank support staff) ask plain-English questions about **credit card and loan terms and conditions** — things like late payment fees, EMI calculation, foreclosure charges, interest rate resets, and annual fees — and get grounded, source-cited answers instead of digging through a 40-page T&C PDF.

It's built on **Spring AI + Azure OpenAI + pgvector**, deliberately chosen to sit inside a Spring Boot/enterprise Java stack rather than a Python ML pipeline — the same stack UBS runs production services on.

**Problem it solves:** Everyone has hit this wall — you get a credit card or a loan, and the terms document is long and dense. Questions like "What happens if I pay my credit card bill late?" or "Can I close my personal loan early, and does it cost extra?" are universally relatable, which is exactly why this domain works well for a demo: no banking background needed to follow along, but it's still a real use case a bank would actually build.

---

## 2. Functional Requirements

### FR1 — Document Ingestion (one-time / batch pipeline)
- Ingest publicly available credit card and loan Most Important Terms & Conditions (MITC) documents, e.g. public sample T&Cs from major card issuers, personal loan agreements, and RBI's "Most Important Terms and Conditions" customer-facing guidelines.
- Chunk documents preserving semantic boundaries (clause/section level, not arbitrary character splits) — e.g. "Late Payment Charges" as one chunk, "Foreclosure Charges" as another.
- Generate embeddings per chunk (`text-embedding-3-large`) and persist to pgvector with metadata (product type, issuer, section, effective date).
- Support re-ingestion when a T&C document is updated (versioning, so stale fee schedules don't answer queries).

### FR2 — Conversational Query (real-time)
- Accept natural-language questions via REST/WebFlux endpoint (and optionally a simple chat UI) — e.g. "What's the late payment fee on my credit card?"
- Retrieve top-k relevant chunks via similarity search, assemble context, call GPT-4o via Spring AI's `ChatClient`.
- Return an answer **with citations** — which document/section/clause backed the answer.
- Support multi-turn conversation (follow-up questions retain context via Spring AI's Advisor/memory pattern) — e.g. a follow-up like "and if I only pay the minimum due?"

### FR3 — Query Routing / Multi-Agent Behavior
- Classify incoming query type (e.g., "credit card fees question" vs "loan/EMI question" vs "interest rate question") and route to the right retrieval namespace/collection.
- This is the "past-tutorial-stage" differentiator — a simple router agent, not just flat RAG.

### FR4 — Structured Output for Specific Use Cases
- For certain query types (e.g., "list all charges on this credit card"), return structured JSON mapped to a Java record (fee name, amount, condition) instead of free text, so it could plug into a downstream UI like an app screen.

### FR5 — Feedback & Evaluation Loop
- Log query, retrieved chunks, and answer for offline review.
- Basic relevance/accuracy scoring (even manual, spreadsheet-based, for a portfolio project) to demonstrate you understand RAG evaluation, not just RAG construction.

### FR6 (stretch) — Tool-Calling for Live Lookups
- A `@Tool`-annotated function for something like "calculate my EMI given principal, rate, and tenure" that runs actual math via a config/service call, showing Spring AI function-calling alongside retrieval.

---

## 3. Technical Requirements

### 3.1 Architecture (two-phase model)
```
                     ┌───────────────────────┐
   [Regulatory PDFs] │   INGESTION PIPELINE   │ (batch, one-time / on-update)
   ─────────────────>│  Parse → Chunk →       │
                      │  Embed → Store         │
                      └───────────┬────────────┘
                                  │
                                  v
                          ┌───────────────┐
                          │   pgvector    │  (vector store + metadata)
                          └───────┬───────┘
                                  │
                     ┌────────────┴─────────────┐
   [User Query] ────>│      QUERY PIPELINE        │ (real-time)
                      │  Route → Retrieve (top-k) │
                      │  → Augment prompt         │
                      │  → GPT-4o via ChatClient  │
                      │  → Cited answer           │
                      └───────────────────────────┘
```

### 3.2 Core Stack
| Layer | Choice | Notes |
|---|---|---|
| Language/Framework | Java 21, Spring Boot, Spring WebFlux | Reactive endpoints for query API |
| AI Orchestration | Spring AI | ChatClient, prompt templates, Advisors |
| LLM | Azure OpenAI GPT-4o | Chat/completion |
| Embeddings | Azure OpenAI text-embedding-3-large | Chunk embeddings |
| Vector Store | pgvector (Postgres) | Similarity search + metadata filtering |
| Chunking | Spring AI TextSplitter (or LangChain4j splitter if gaps exist) | Section-aware chunking |
| Deployment | Kubernetes / OpenShift | Matches your production UBS context |
| Observability | Basic structured logging + maybe Micrometer | Query latency, retrieval hit-rate |

### 3.3 Non-Functional Requirements
- **Latency:** target sub-3s for a query round trip (retrieval + LLM call) — reasonable for a demo, and gives you a concrete number to quote in the IJP writeup.
- **Accuracy/grounding:** answers must include citations; no un-cited free-form answers for fee/charge questions (getting a fee number wrong is exactly the kind of mistake a real bank can't afford, so this constraint is deliberate).
- **Idempotent ingestion:** re-running ingestion on unchanged docs shouldn't duplicate vectors.
- **Config-driven:** model names, chunk size, top-k, all externalized via `application.yml`, not hardcoded.
- **Security:** no real customer/account data — corpus is public/sample T&C text only (worth stating explicitly in any review, since data handling is a real concern in banking AI projects).

### 3.4 Data Corpus
- Sample/public Most Important Terms & Conditions (MITC) documents for credit cards (fees, charges, interest rates)
- Sample personal/personal-loan agreement terms (EMI, foreclosure, penal interest)
- RBI's customer-facing MITC guidelines for credit cards and loans
- (Optional) A sample FD/savings account T&C for breadth, if time allows

---

## 4. Suggested Demo Scenarios (for IJP presentation)
1. "What are all the fees on this credit card?" → structured JSON output (fee name, amount, condition).
2. "Can I foreclose my personal loan early, and does it cost extra?" → cited free-text answer.
3. Follow-up: "What if I foreclose after 12 months instead of 6?" → tests multi-turn memory.
4. A query that spans two documents (e.g., credit card late fee + loan penal interest comparison) → tests routing/retrieval quality.
5. "Calculate my EMI for a ₹5,00,000 loan at 10% for 3 years" → tests the tool-calling stretch goal.

---

## 5. Fit With Your 12-Week Plan
- **Weeks 1–8:** ingestion pipeline, core query pipeline, routing logic, structured outputs.
- **Weeks 9–12:** hardening (error handling, idempotent ingestion, config externalization), OpenShift/Kubernetes deployment, documentation, and packaging this into the IJP writeup (problem → architecture → demo → metrics).

---

## 6. What Makes This IJP-Ready (not just a tutorial project)
- Universally relatable domain (everyone understands credit card fees and EMIs) — easy for any panel, technical or not, to follow the demo without banking background.
- Grounded in real, if simplified, financial documents — still shows domain judgment, just without requiring the audience to know SWIFT/FATF jargon.
- Multi-agent-style query routing, not flat RAG — shows you're past the "hello world RAG" stage.
- Structured output to Java records — shows enterprise integration thinking, not just chatbot output.
- Built on Spring AI inside your existing Spring Boot/K8s/OpenShift context — directly transferable to UBS production systems, which is the strongest argument for a Staff/Senior-level UK transfer.
