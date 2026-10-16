package com.inn.paysense.exception;

public final class ErrorMessageConstants {

    private ErrorMessageConstants(){

    }

    public static final String EMPTY_NULL_FILE_ERROR_MESSAGE = "Uploaded file cannot be empty or null";
    public static final String FAILED_TO_PROCESS_FILE_ERROR_MESSAGE = "Failed to process temp file during ingestion";
    public static final String FILE_INGESTION_ERROR_MESSAGE = "Failed to ingest the file. Please try again later";
    public static final String DELETE_FILE_ERROR_MESSAGE = "Error while deleting the file.";
    public static final String ERROR_IN_FILE_PROCESSING_MESSAGE = "Error in file processing. Please try again later";
    public static final String GENERIC_EXCEPTION_MESSAGE = "An unexpected error occurred in PaySense:";

}
