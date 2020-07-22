package arachne.server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ServerFailureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServerFailureException(String cause) {
        super(cause);
    }

}
