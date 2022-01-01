package cpen221.mp3.server;

import java.util.Objects;

/**
 * Abstraction Function:
 * A Response represents a response sent from a WikiMediatorServer to a client connected to
 * the server. The Response is an object with three instance variables that indicate the
 * status of the Response: id, status, and response.
 */
public class Response {
    private final String id;
    private final String status;
    private final String response;

    /*
     * Representation Invariant:
     *  - id must not be null
     *  - status must either be "success" or "failed"
     */

    /**
     * Check that the representation invariants for Response hold true.
     * @throws RuntimeException if any representation invariants are violated.
     */
    private void checkRep() {
        if (id == null) {
            throw new RuntimeException("id must not be null!");
        }
        else if (!Objects.equals(status, "success") && !Objects.equals(status, "failed")) {
            throw new RuntimeException("status must be either \"success\" or \"failed\"");
        }
    }

    /**
     * Create an instance of Response, initializing all instance variables.
     * @param id the identifier of the Response. id must not be null.
     * @param status the status of the Response, must either be "success" or "failed"
     * @param response the response obtained from handling the request specified by the
     *                 Request object with the same id
     */
    public Response(String id, String status, String response) {
        this.id = id;
        this.status = status;
        this.response = response;
        checkRep();
    }
}
