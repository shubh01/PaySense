package com.inn.paysense.utility;

import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.List;

public class ParagraphTextSplitter extends TextSplitter {

    private final int maxChunkSizeChars;
    private final int chunkOverLapCharsSize = 500;

    public ParagraphTextSplitter(int maxChunkSizeChars) {
        this.maxChunkSizeChars = maxChunkSizeChars;
    }

    @Override
    protected List<String> splitText(String text) {

        List<String> chunks = new ArrayList<>();

        String[] paragraphs = text.split(Constants.PARAGRAPH_SPLIT_REGEX);

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmedPara = paragraph.trim();
            if (trimmedPara.isEmpty()) {
                continue;
            }

            // 2. If adding this paragraph exceeds max chunk size, flush the current chunk
            if (currentChunk.length() + trimmedPara.length() + chunkOverLapCharsSize > maxChunkSizeChars && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                String overlappingChunk = currentChunk.substring(Math.max(0, currentChunk.length() - 500));
                currentChunk.setLength(0); // Reset buffer
                currentChunk.append(overlappingChunk);
            }

            // 3. Keep complete paragraphs together
            currentChunk.append(trimmedPara).append(Constants.NEW_LINE_REGEX);
        }

        // Add any remaining text
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
