package arachne.server.domain;

public enum JobFeedbackContentType {

    TEXT, JSON, BINARY;

    public static JobFeedbackContentType valueOf(int value) {
        switch (value) {
            case 0:
                return TEXT;
            case 1:
                return JSON;
            case 2:
                return BINARY;
            default:
                throw new IllegalArgumentException();
        }
    }

}
