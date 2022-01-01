package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.ObjectNotInCacheException;
import org.fastily.jwiki.core.Wiki;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * A WikiMediator represents a mediator service for Wikipedia that accesses Wikipedia pages
 * and other relevant information. Wikipedia pages are cached in the WikiMediator to minimize
 * network accesses. A WikiMediator can only store a finite amount of Wikipedia pages, and a
 * page is only cached for a finite amount of time unless it is accessed again.
 * The mediator service will also collect statistical information about requests, such as the
 * frequency and absolute time of requests.
 *
 * Abstraction Function:
 * A WikiMediator is represented by a FSFTBuffer, which is a user-defined data type that stores
 * a finite number of bufferable objects for a finite period of time. In this case, the objects
 * stored in the FSFTBuffer are instances of the WikiPage class.
 * One ArrayList of Strings, unfilteredSearches, keeps track of the query strings submitted to
 * the WikiMediator through the methods search() and getPage().
 * Two ArrayLists of Longs, timeSearched and requestTimes, keep track of the time at which a query
 * String was submitted to either search() or getPage() and the time any of the WikiMediator methods
 * were called, respectively.
 */
public class WikiMediator {

    /* conversion constant 1 second = 1000 milliseconds */
    public static final int MILLIS = 1000;

    private final FSFTBuffer<WikiPage> wikiBuffer;
    private final List<String> unfilteredSearches = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> timeSearched = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> requestTimes = Collections.synchronizedList(new ArrayList<>());

    /*
     * Representation Invariant:
     *  - unfilteredSearches must not contain any null entries
     *  - timeSearched must not contain any entries less than zero
     *  - requestTimes must not contain any entries less than zero
     */

    /*
     * Thread Safety Arguments:
     *  - MILLIS is static and final
     *  - unfilteredSearches, timeSearched, and requestTimes all point to threadsafe list data type
     *  - non-threadsafe data type's reference is thrown away by directly passing a new ArrayList
     *    directly into synchronizedList()
     *  - methods of the WikiMediator class are synchronized
     */

    /**
     * Check that the representation invariants WikiMediator hold true.
     * @throws RuntimeException if any representation invariants are violated.
     */
    private synchronized void checkRep() {
        for (String s : unfilteredSearches) {
            if (s == null) {
                throw new RuntimeException("unfilteredSearches must not contain a null entry");
            }
        }

        for (Long searchTime : timeSearched) {
            if (searchTime < 0) {
                throw new RuntimeException("timeSearched must not contain any entries less than zero");
            }
        }

        for (Long requestTime : requestTimes) {
            if (requestTime < 0) {
                throw new RuntimeException("requestTimes must not contain any entries less than zero");
            }
        }
    }

    /**
     * Create a WikiMediator with a fixed capacity and a timeout value.
     *
     * @param capacity the maximum amount of objects that can be stored in the WikiMediator
     *                 capacity must be a number greater than zero
     * @param stalenessInterval the maximum period of time an object will be stored in the WikiMediator
     *                          stalenessInterval must be a number greater than zero
     */
    public WikiMediator(int capacity, int stalenessInterval){
        wikiBuffer = new FSFTBuffer<>(capacity, stalenessInterval);
        checkRep();
    }

    /**
     * Helper method that stores the Strings used as input parameters to search or getPage requests
     * in unfilteredSearches. Also stores the time at which the requests were made in timeSearched.
     * @param queryOrPageTitle, the String used as input parameters in search or getPage method calls
     *
     * Frame Condition:
     * - String of query or page title is added to unfilteredSearches
     * - Long corresponding to the time at which the request was made is stored in timeSearched
     */
    private synchronized void addSearch(String queryOrPageTitle){
        unfilteredSearches.add(queryOrPageTitle);
        timeSearched.add(System.currentTimeMillis() / MILLIS);
    }

    /**
     * Given a query, return up to limit page titles that match the query string (per Wikipedia's search service).
     * @param query The query string to search the Wiki with, query must not be null.
     * @param limit The maximum number of page titles to return that match the query string
     * @return Up to limit page titles that match the query string when searched through Wikipedia's search service.
     * In the case that limit <= 0, return an empty list of page titles.
     */
    public synchronized List<String> search(String query, int limit){
        checkRep();
        Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();

        requestTimes.add(System.currentTimeMillis() / MILLIS);
        addSearch(query);

        checkRep();
        if (limit <= 0) {
            return new ArrayList<>();
        }

        return wiki.search(query, limit);
    }

    /**
     * Given a pageTitle, return the text associated with the Wikipedia page that matches pageTitle.
     * @param pageTitle The title of the page to query Wikipedia, pageTitle must not be null.
     * @return The text of the page specified by pageTitle, or an empty string if the page is non-existent
     * or something went wrong.
     */
    public synchronized String getPage(String pageTitle) {
        checkRep();

        requestTimes.add(System.currentTimeMillis() / MILLIS);
        addSearch(pageTitle);

        WikiPage wikiPage;
        String pageText;

        try {
            wikiPage = wikiBuffer.get(pageTitle);
            pageText = wikiPage.getPageContent();
        }
        catch(ObjectNotInCacheException e){
            Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
            pageText = wiki.getPageText(pageTitle);

            wikiPage = new WikiPage(pageTitle, pageText);
            wikiBuffer.put(wikiPage);
        }

        checkRep();
        return pageText;
    }

    /**
     * Given a limit, return the most common Strings used in search and getPage requests, with items being sorted in
     * non-increasing count order. When many requests have been made, return only limit items.
     *
     * @param limit The maximum number of most common Strings to return.
     *              limit must be a number greater than zero.
     * @return Up to limit most common Strings used in search and getPage requests, with items sorted in non-increasing
     * count order based on the frequency of their query requests. In the case of different Strings with the same
     * frequency of total search and getPage requests, the String to return will be chosen arbitrarily.
     */
    public synchronized List<String> zeitgeist(int limit) {
        checkRep();

        requestTimes.add(System.currentTimeMillis() / MILLIS);

        List<String> mostCommonStrings = new ArrayList<>();
        Map<String, Integer> zeigMap = new HashMap<>();

        for (String s : unfilteredSearches){
            int count = zeigMap.getOrDefault(s, 0);
            zeigMap.put(s, count + 1);
        }

        List<Map.Entry<String, Integer>> sortedMapList = new LinkedList<>(zeigMap.entrySet());

        sortedMapList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        if (sortedMapList.size() == 0) {
            checkRep();
            return mostCommonStrings;
        }

        else {
            for (int i = 0; i < Math.min(limit, sortedMapList.size()); i++){
                mostCommonStrings.add(sortedMapList.get(i).getKey());
            }
        }

        checkRep();
        return mostCommonStrings;
    }

    /**
     * Finds and returns up to maxItems most common Strings used in search and getPage requests, with requests
     * all having been made in the last timeLimitInSeconds. Requests are sorted in non-increasing count order
     * according to their frequencies.
     *
     * @param timeLimitInSeconds The number of seconds before the method was called where search or getPage requests
     *                           must have been made in order to have the query Strings be accounted for.
     *                           If the method was called at currentTime, the requests returned would all have been made
     *                           in the interval [currentTime - timeLimitInSeconds, currentTime].
     *                           0 < timeLimitInSeconds < currentTime
     * @param maxItems The maximum number of most common Strings to return.
     *                 maxItems must be a number greater than zero.
     * @return Up to maxItems most common Strings used in search and getPage requests made at most timeLimitInSeconds ago,
     * with items sorted in non-increasing count order based on the frequency of their query requests.  In the case of
     * different Strings with the same frequency of total search and getPage requests, the String to return will be
     * chosen arbitrarily.
     */
    public synchronized List<String> trending(int timeLimitInSeconds, int maxItems) {
        checkRep();
        long callTime = System.currentTimeMillis()/1000;
        requestTimes.add(callTime);

        List<String> trendingStrings = new ArrayList<>();
        List<String> filteredSearches = new ArrayList<>();
        List<Integer> removedIndexes = new ArrayList<>();
        Map<String, Integer> trendingMap = new HashMap<>();

        for(String s :unfilteredSearches){
            filteredSearches.add(s);
        }

        int index = 0;

        for (String s : unfilteredSearches){
            if (callTime - timeLimitInSeconds > timeSearched.get(index)){
                removedIndexes.add(index);
            }
            index++;
        }

        Collections.sort(removedIndexes, Collections.reverseOrder());

        for(int i : removedIndexes) {
            filteredSearches.remove(i);
        }

        for (String s : filteredSearches){
            int count = trendingMap.getOrDefault(s, 0);
            trendingMap.put(s, count + 1);
        }

        List<Map.Entry<String, Integer> > sortedMapList = new LinkedList<>(trendingMap.entrySet());

        sortedMapList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        if (sortedMapList.size() == 0) {
            checkRep();
            return trendingStrings;
        }

        else {
            for (int i = 0; i < Math.min(maxItems, sortedMapList.size()); i++){
                trendingStrings.add(sortedMapList.get(i).getKey());
            }
        }

        checkRep();
        return trendingStrings;
    }

    /**
     * Return the maximum number of valid requests made using the public API of WikiMediator seen in any time window
     * of length timeWindowInSeconds. Valid requests are calls to the methods: search, getPage, zeitgeist, trending,
     * and windowedPeakLoad. This current call to windowedPeakLoad will not be accounted for in the number of valid requests.
     *
     * @param timeWindowInSeconds The size of the time interval within which request frequencies will be considered.
     *                            0 < timeWindowInSeconds < currentTime
     *                            where currentTime is the time at which this method was called.
     * @return The maximum number of requests made to the WikiMediator API during any time interval of size
     * timeWindowInSeconds. This number will not include requests made at the moment this method was called, including
     * calls to this method itself.
     */
    public synchronized int windowedPeakLoad(int timeWindowInSeconds) {
        checkRep();

        long callTime = System.currentTimeMillis() / MILLIS;
        requestTimes.add(callTime);

        int maxRequests = 0;


        long firstRequestTime = requestTimes.stream()
                .min(Comparator.comparing(Long::valueOf))
                .orElse((long) 0);

        do{
            long windowUpperBound = callTime;
            long windowLowerBound = callTime - timeWindowInSeconds;

            int numRequests = (int) requestTimes.stream()
                    .filter(times -> times < windowUpperBound && times >= windowLowerBound)
                    .count();

            if (numRequests > maxRequests){
                maxRequests = numRequests;
            }

            callTime -= 1;
        } while(callTime - timeWindowInSeconds >= firstRequestTime);

        checkRep();
        return maxRequests;
    }

    /**
     * Return the maximum number of valid requests made using the public API of WikiMediator seen in any time window
     * of length 30 seconds. Valid requests are calls to the methods: search, getPage, zeitgeist, trending,
     * and windowedPeakLoad. This current call to windowedPeakLoad will not be accounted for in the number of valid requests.
     *
     * @return The maximum number of requests made to the WikiMediator API during any 30 second time interval.
     * This number will not include requests made at the moment this method was called, including calls to this method
     * itself.
     */
    public synchronized int windowedPeakLoad() {
        return windowedPeakLoad(30);
    }

    /* Task 5 - Could not implement in time */

    /**
     * Finds the shortest path between two Wikipedia pages, where shortest path is defined
     * as the minimum number of link clicks it takes to start from a page, pageTitle1,
     * and reach another page, pageTitle2.
     * @param pageTitle1 The Wikipedia page to start on
     * @param pageTitle2 The Wikipedia page to end on
     * @param timeout The number of seconds that is permitted for this operation before
     *                a TimeoutException is thrown.
     * @return A list of page titles (including the starting and ending pages) on the
     * shortest path between pageTitle1 and pageTitle2, if such a path exists. If there
     * are two or more shortest paths, then the one with the lowest lexicographical
     * value is to be returned. If no path exists between two pages, an empty List
     * will be returned.
     * @throws TimeoutException If the operation takes timeout seconds or longer to
     * execute.
     */
    public synchronized  List<String> shortestPath(String pageTitle1, String pageTitle2, int timeout) throws TimeoutException {
        return new ArrayList<>();
    }
}
