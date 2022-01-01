package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.Bufferable;

/**
 * A WikiPage is a data type that implements the Bufferable interface. It represents a
 * Wikipedia page with a page title and the corresponding page content.
 *
 * Abstraction Function:
 * A WikiPage is represented by two Strings: pageTitle and pageContent. The pageTitle
 * is the title of the Wikipedia page it represents, while the pageContent is all the
 * text found on the Wikipedia page specified by pageTitle.
 */
public class WikiPage implements Bufferable {
    private final String pageTitle;
    private final String pageContent;

    /*
     * Representation Invariant:
     *  - pageTitle must be the title of a valid Wikipedia page
     *  - pageContent must be the text found on the Wikipedia page specified by pageTitle
     */

    /**
     * Create a WikiPage with a page title and page content fetched from Wikipedia.
     * @param pageTitle The title of the Wikipedia page.
     * @param pageContent The text found on the Wikipedia page specified by pageTitle.
     */
    WikiPage(String pageTitle, String pageContent){
        this.pageTitle = pageTitle;
        this.pageContent = pageContent;
    }

    /**
     * Return the unique identifier of the WikiPage.
     * @return The unique identifier of the WikiPage, which corresponds to its page title.
     */
    @Override
    public String id(){
        return pageTitle;
    }

    /**
     * Get the page title of the WikiPage.
     * @return The page title of the WikiPage.
     */
    public String getPageTitle() {
        return pageTitle;
    }

    /**
     * Get the page content of the WikiPage.
     * @return The page content found on the WikiPage.
     */
    public String getPageContent() {
        return pageContent;
    }

}
