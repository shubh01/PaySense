package com.inn.paysense.utility;

public class Constants {

    public final static String PARAGRAPH_SPLIT_REGEX = "\\n\\s*\\n";
    public final static String NEW_LINE_REGEX = "\n\n";
    public final static String TAB_REGEX = "[ \\t]+";
    public final static String SOURCE_FILE_NAME = "source_filename";
    public final static String PARENT_ID = "parent_id";
    public final static String PARENT_TEXT_CONTEXT = "parent_text_context";
    public final static String CHILD_INDEX = "child_index";
    public final static String FILE_CATEGORY = "file_category";

    public final static String FILE_PROCESSED_SUCCESSFULLY = "File processed successfully";
    public final static String QUESTION_ANSWER_CONTEXT = "question_answer_context";
    public final static String SAFE_RESPONSE = "safe";
    public final static String DELETE_DOCUMENT_SUCCESS_MESSAGE = "document deleted successfully";
    public final static String DELETE_DOCUMENT_FAILURE_MESSAGE = "error occurred while deleting the document";

    public final static String GENERAL_QUERY_RESPONSE = "I am the PaySense AI Assistant. I can answer questions using my knowledge base.";
    public final static String GENERAL_QUERY_PATTERN_1 = "(?i).*\\b(who (created|made|built) you|your creator|who is your author)\\b.*";
    public final static String GENERAL_QUERY_PATTERN_2 = "(?i).*\\b(what is your name|who are you|what should i call you)\\b.*";
    public final static String GENERAL_QUERY_PATTERN_3 = "(?i).*\\b(how can you help|what can you do|your capabilities|what are your features)\\b.*";
    public final static String GENERAL_QUERY_PATTERN_4 = "(?i).*\\b(how are you|how was your day|what are you doing)\\b.*";
    public final static String INCORRECT_PROMPT_RESPONSE = "Please enter correct a valid question.";
    public final static String SENSITIVE_INFO_PROMPT_RESPONSE = "Sensitive information provided. Please remove sensitive information and re-enter the question.";

    public final static String PROCESS_FILE_URL = "processFile";
    public final static String RAG_CHAT_URL = "ragChat";
}
