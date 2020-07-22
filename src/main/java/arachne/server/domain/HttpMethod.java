package arachne.server.domain;

public enum HttpMethod {

    GET(0), POST(1), PUT(2), DELETE(3);

    private int value;

    private HttpMethod(final int value) {
        this.value = value;
    }

    public static HttpMethod valueOf(int value) {
        switch (value) {
            case 0:
                return GET;
            case 1:
                return POST;
            case 2:
                return PUT;
            case 3:
                return DELETE;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getValue() {
        return this.value;
    }

}
