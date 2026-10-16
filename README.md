# PaySense

PaySense is a small RAG app I built with Spring AI to answer questions about banking products. You ask something like "what's the late payment fee?" and it looks through the actual T&C documents, finds the relevant parts and answers from those, along with where it found it.

<!-- TODO: add a screenshot or short GIF here -->

## Why I built this

I've been working in Java for 11+ years and wanted to get hands-on with LLMs in a stack I already know well. Banking T&C documents seemed like a good use case. They're long, boring and full of clauses nobody reads, but people still need answers from them. Also a wrong answer there actually matters, so it's a good test for keeping the model grounded in the source instead of letting it make things up.

## What it does

- Loads banking product documents (PDF/text) and splits them into chunks
- Creates embeddings and stores them in a vector store
- Finds the most relevant chunks for a question
- Sends those chunks plus the question to the LLM and tells it to answer only from that context
- Returns the answer with the source document/page
- Says it doesn't know if the answer isn't in the documents
- <!-- TODO: add anything else you built -->

## How it works

1. **Ingestion:** read the documents, split into chunks, embed them, store with metadata (document name, page).
2. **Retrieval:** embed the question, pull the top-k similar chunks.
3. **Generation:** put the chunks in the prompt and ask the model to answer only from them.
4. **Response:** return the answer and the sources used.

## Tech stack

- Java <!-- TODO: version -->, Spring Boot, Spring AI
- LLM: <!-- TODO -->
- Embeddings: <!-- TODO -->
- Vector store: <!-- TODO -->
- Build: <!-- TODO: Maven / Gradle -->

## Running it locally

You'll need JDK <!-- TODO: version -->, Maven and an API key for <!-- TODO: provider -->.

```bash
git clone https://github.com/<your-username>/paysense.git
cd paysense
export OPENAI_API_KEY=your-key   # TODO: change to your provider
./mvnw spring-boot:run
```

Put your documents in `<!-- TODO: path -->`. They get ingested <!-- TODO: on startup / through an endpoint -->.

## Trying it out

```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the fee for late payment?"}'
```

<!-- TODO: replace with your real endpoint and paste an actual response -->

## Choices I made

- **Chunking:** <!-- TODO: size, overlap and why -->
- **Top-k / similarity threshold:** <!-- TODO: values and why -->
- **Prompt:** told the model to answer only from the context and say so when it can't find the answer
- **RAG instead of fine-tuning:** documents change, I wanted citations and I didn't want to retrain anything

## Limitations

- Answers are only as good as the documents and the retrieval. If the right chunk isn't retrieved, the answer will be off.
- This is a personal project. It's not financial or legal advice, always check the official document.
- I only used public/sample documents, no real customer data.

## Things I want to add

- Proper retrieval evaluation
- Hybrid search (keyword + vector) and re-ranking
- Filtering by product type
- Chat memory for follow-up questions
- A simple UI
- Docker setup
