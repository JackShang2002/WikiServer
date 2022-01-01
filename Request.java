package cpen221.mp3.server;

import java.util.Objects;

/**
 * Abstraction Function:
 * A Request represents a request to a WikiMediatorServer that was parsed from a
 * JSON-formatted string sent from a client connected to the WikiMediatorServer
 * with appropriate parameters. Each request has several instance
 * variables that indicate specific information such as the operation that needs to be
 * performed and the parameters for the operations.
 *
 * A null field indicates there was no information for that specific instance variable
 * in the JSON-formatted string that was parsed to create the Request.
 */
public class Request {
    private final String id;
    private final String type;
    private final String query;
    private final String limit;
    private final String pageTitle;
    private final String timeLimitInSeconds;
    private final String maxItems;
    private final String timeWindowInSeconds;
    private final String timeout;

    /*
     * Representation Invariant:
     *  - id must not be null
     */

    /**
     * Check that the representation invariants for Request hold true.
     * @throws RuntimeException if any representation invariants are violated.
     */
    private void checkRep() {
        if (id == null) {
            throw new RuntimeException("id must not be null!");
        }
    }

    /**
     * Create a Request, initializing all instance variables.
     *
     * @param id the identifier of the Request. id must not be null.
     * @param type the type of operation the Request contains.
     * @param query the query parameter the JSON request contained
     * @param limit the limit parameter the JSON request contained
     * @param pageTitle the pageTitle parameter the JSON request contained
     * @param timeLimitInSeconds the timeLimitInSeconds parameter the JSON request contained
     * @param maxItems the maxItems parameter the JSON request contained
     * @param timeWindowInSeconds the timeWindowInSeconds parameter the JSON request contained
     * @param timeout the timeout parameter the JSON request contained
     */
    public Request(String id, String type, String query, String limit, String pageTitle, String timeLimitInSeconds,
                   String maxItems, String timeWindowInSeconds, String timeout) {
        this.id = id;
        this.type = type;
        this.query = query;
        this.limit = limit;
        this.pageTitle = pageTitle;
        this.timeLimitInSeconds = timeLimitInSeconds;
        this.maxItems = maxItems;
        this.timeWindowInSeconds = timeWindowInSeconds;
        this.timeout = timeout;
        checkRep();
    }

    /**
     * Check that the JSON-formatted String parsed to create the current instance
     * of Request did not contain any format errors or missing parameters.
     * @throws JsonFormatException If the JSON request had formatting errors or
     * missing parameters required for the specific operation specified by type.
     */
    public void checkValidRequest() throws JsonFormatException {
        String id = this.getId();
        String type = this.getType();
        String query = this.getQuery();
        String limit = this.getLimit();
        String pageTitle = this.getPageTitle();
        String timeLimitInSeconds = this.getTimeLimitInSeconds();
        String maxItems = this.getMaxItems();
        String timeWindowInSeconds = this.getTimeWindowInSeconds();
        String timeout = this.getTimeout();

        if (id == null) {
            throw new JsonFormatException();
        }

        else if (Objects.equals(type, "search")) {
            if (query == null || limit == null) {
                throw new JsonFormatException();
            }
            else {
                try {
                    int limitValue = Integer.parseInt(limit);
                } catch (NumberFormatException nfe) {
                    throw new JsonFormatException();
                }
            }
        }

        else if (Objects.equals(type, "getPage")) {
            if (pageTitle == null) {
                throw new JsonFormatException();
            }
        }

        else if (Objects.equals(type, "zeitgeist")) {
            if (limit == null) {
                throw new JsonFormatException();
            }
            try {
                int limitValue = Integer.parseInt(limit);
            } catch (NumberFormatException nfe) {
                throw new JsonFormatException();
            }
        }

        else if (Objects.equals(type, "trending")) {
            if (timeLimitInSeconds == null || maxItems == null) {
                throw new JsonFormatException();
            }
            try {
                int timeLimitValue = Integer.parseInt(timeLimitInSeconds);
                int maxItemsValue = Integer.parseInt(maxItems);
            } catch (NumberFormatException nfe) {
                throw new JsonFormatException();
            }
        }

        else if (Objects.equals(type, "windowedPeakLoad")) {
            if (timeWindowInSeconds == null) {
                throw new JsonFormatException();
            }
            try {
                int timeWindowValue = Integer.parseInt(timeWindowInSeconds);
            } catch (NumberFormatException nfe) {
                throw new JsonFormatException();
            }
        }

        else if (timeout != null) {
            try {
                int timeoutValue = Integer.parseInt(timeout);
            } catch (NumberFormatException nfe) {
                throw new JsonFormatException();
            }
        }

        else {
            throw new JsonFormatException();
        }
    }

    /**
     * Get the unique identifier of the Request.
     * @return the unique identifier of the Request
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the type of operation of the Request.
     * @return the operation type of the Request, which corresponds
     * to one of the methods belonging to instances of the WikiMediator
     * class.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the query of the Request, which could be null
     * if the original JSON String did not contain a field
     * for query.
     * @return the query of the Request
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * Get the limit of the Request, which could be null
     * if the original JSON String did not contain a field
     * for limit.
     * @return the limit of the Request
     */
    public String getLimit() {
        return this.limit;
    }

    /**
     * Get the pageTitle of the Request, which could be null
     * if the original JSON String did not contain a field
     * for pageTitle.
     * @return the pageTitle of the Request
     */
    public String getPageTitle() {
        return this.pageTitle;
    }

    /**
     * Get the timeLimitInSeconds of the Request, which could be null
     * if the original JSON String did not contain a field
     * for timeLimitInSeconds.
     * @return the timeLimitInSeconds of the Request
     */
    public String getTimeLimitInSeconds() {
        return this.timeLimitInSeconds;
    }

    /**
     * Get the maxItems of the Request, which could be null
     * if the original JSON String did not contain a field
     * for maxItems.
     * @return the maxItems of the Request
     */
    public String getMaxItems() {
        return this.maxItems;
    }

    /**
     * Get the timeWindowInSeconds of the Request, which could be null
     * if the original JSON String did not contain a field
     * for timeWindowInSeconds.
     * @return the timeWindowInSeconds of the Request
     */
    public String getTimeWindowInSeconds() {
        return this.timeWindowInSeconds;
    }

    /**
     * Get the timeout of the Request, which could be null
     * if the original JSON String did not contain a field
     * for timeout.
     * @return the timeout of the Request
     */
    public String getTimeout() {
        return this.timeout;
    }
}
