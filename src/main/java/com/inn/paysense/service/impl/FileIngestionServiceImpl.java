package com.inn.paysense.service.impl;

import com.inn.paysense.exception.ErrorMessageConstants;
import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.model.FileCategory;
import com.inn.paysense.service.FileIngestionService;
import com.inn.paysense.utility.Constants;
import com.inn.paysense.utility.ParagraphTextSplitter;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileIngestionServiceImpl implements FileIngestionService {

    private Logger logger = LoggerFactory.getLogger(FileIngestionServiceImpl.class);

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ParagraphTextSplitter parentSplitter;
    private final TokenTextSplitter childSplitter;
    private final static String PARENT_SUFFIX = "parent_";
    private final static String SEPARATOR = "_";
    private final static String EMPTY_SPACE = " ";

    public FileIngestionServiceImpl(VectorStore vectorStore, EmbeddingModel embeddingModel) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.parentSplitter = new ParagraphTextSplitter(3000);
        this.childSplitter = TokenTextSplitter.builder().withChunkSize(400).withMinChunkSizeChars(50).build();
    }

    @Override
    public void ingestDocument(Resource resource, FileCategory fileCategory) throws PaySenseException {
        logger.info("Starting structural parent-child ingestion for document {} fileCategory {}", resource.getFilename(),fileCategory.getName());
        try {
            PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(resource);
            List<Document> rawPages = pdfDocumentReader.get();

            // Clean PDF text formatting whitespace before splitting
            List<Document> cleanedPages = rawPages.stream()
                    .map(doc -> new Document(doc.getId(), doc.getText().replaceAll(Constants.TAB_REGEX, EMPTY_SPACE), doc.getMetadata()))
                    .toList();

            // 1. Split into intact structural Parent Chunks
            List<Document> parentDocs = parentSplitter.apply(cleanedPages);

            parentDocs = parentDocs.stream().filter(doc -> doc.getText() != null && !doc.getText().trim().isEmpty()).collect(Collectors.toList());

            List<Document> childDocumentToIngest = new ArrayList<>();

            for (int pId = 0; pId < parentDocs.size(); pId++) {
                Document parentDoc = parentDocs.get(pId);
                String parentText = parentDoc.getText();
                String parentId = PARENT_SUFFIX + pId + SEPARATOR + resource.getFilename();

                // 2. Split parent chunk into Child Chunks for vector indexing
                List<Document> childDocs = childSplitter.apply(List.of(parentDoc));

                childDocs = childDocs.stream().filter(doc -> doc.getText() != null && !doc.getText().trim().isEmpty()).collect(Collectors.toList());

                for (int cId = 0; cId < childDocs.size(); cId++) {
                    Document childDoc = childDocs.get(cId);
                    String childText = childDoc.getText();

                    String validQdrantUuid = UUID.nameUUIDFromBytes(
                            (resource.getFilename() + SEPARATOR + childText).getBytes(StandardCharsets.UTF_8)
                    ).toString();

                    childDoc.getMetadata().put(Constants.SOURCE_FILE_NAME, resource.getFilename());
                    childDoc.getMetadata().put(Constants.PARENT_ID, parentId);
                    childDoc.getMetadata().put(Constants.PARENT_TEXT_CONTEXT, parentText);
                    childDoc.getMetadata().put(Constants.CHILD_INDEX, cId);
                    childDoc.getMetadata().put(Constants.FILE_CATEGORY, fileCategory.getName());
                    childDocumentToIngest.add(new Document(validQdrantUuid, childText, childDoc.getMetadata()));
                }
            }
            List<Document> validDocumentsToIngest = getValidDocumentsToIngest(resource, childDocumentToIngest);
            vectorStore.add(validDocumentsToIngest);
            logger.info("Successfully ingested {} child vectors linked to {} structural parent contexts.",
                    childDocumentToIngest.size(), parentDocs.size());

        } catch (Exception e) {
            logger.error("Exception occurred @method ingestDocument", e);
            throw new PaySenseException(ErrorMessageConstants.FILE_INGESTION_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private @NonNull List<Document> getValidDocumentsToIngest(Resource resource, List<Document> childDocumentToIngest) throws PaySenseException {
        // Diagnostic pass: embed each chunk individually so a single bad chunk
        // doesn't silently fail the whole batch upsert with no indication of which one.
        List<Document> validDocumentsToIngest = new ArrayList<>();
        for (Document doc : childDocumentToIngest) {
            String text = doc.getText();
            float[] vector;
            try {
                vector = embeddingModel.embed(text);
            } catch (Exception embedEx) {
                logger.warn("Embedding call threw for chunk id={} textLen={} preview=\"{}\": {}",
                        doc.getId(), text.length(), preview(text), embedEx.getMessage());
                continue;
            }
            if (vector == null || vector.length == 0) {
                logger.warn("Skipping chunk with empty embedding (dim={}). id={} textLen={} preview=\"{}\"",
                        vector == null ? -1 : vector.length, doc.getId(), text.length(), preview(text));
                continue;
            }
            logger.debug("Chunk id={} textLen={} embeddingDim={}", doc.getId(), text.length(), vector.length);
            if(doc.getText() != null && !doc.getText().trim().isBlank() && !doc.getText().isEmpty()) {
                validDocumentsToIngest.add(doc);
            }
        }

        if (validDocumentsToIngest.size() < childDocumentToIngest.size()) {
            logger.warn("{} of {} chunks produced empty/invalid embeddings and were skipped for {}",
                    childDocumentToIngest.size() - validDocumentsToIngest.size(),
                    childDocumentToIngest.size(), resource.getFilename());
        }
        if (validDocumentsToIngest.isEmpty()) {
            throw new PaySenseException(ErrorMessageConstants.FILE_INGESTION_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return validDocumentsToIngest;
    }

    private String preview(String text) {
        if (text == null) {
            return "";
        }
        String collapsed = text.replaceAll("\\s+", " ").trim();
        return collapsed.length() > 80 ? collapsed.substring(0, 80) + "..." : collapsed;
    }
}