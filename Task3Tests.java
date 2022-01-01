package cpen221.mp3;

import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


public class Task3Tests {
    /* search */
    @Test
    public void testNonExistentSearch() {
        WikiMediator testWiki = new WikiMediator(10, 5);
        List<String> expected = new ArrayList<>();

        Assertions.assertEquals(expected, testWiki.search("%^0-=3kvneo4r9u@@@", 5));
    }
    @Test
    public void testSearch() {
        WikiMediator testWiki = new WikiMediator(10, 5);
        List<String> expected = new ArrayList<>();
        List<String> expected1 = new ArrayList<>();
        List<String> expected2 = new ArrayList<>();


        expected.add("Spoon");
        expected.add("Talk:Spoon");
        expected.add("Wooden spoon (award)");

        expected1.add("Avocado");
        expected1.add("Filmco");
        expected1.add("UAA Films");
        expected1.add("Dreams of Bali");

        List<String> actual = testWiki.search("spoon", 3);
        List<String> actual1 = testWiki.search("avacado", 4);
        List<String> actual2 = testWiki.search("video games", -1);

        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(expected1, actual1);
        Assertions.assertEquals(expected2, actual2);
    }

    @Test
    public void testLimitZero() {
        WikiMediator testWiki = new WikiMediator(10, 5);
        List<String> expected = new ArrayList<>();

        Assertions.assertEquals(expected, testWiki.search("Canada Goose", 0));
    }

    /* getPage */
    //TODO: WRITE ASSERTIONS FOR THIS
    @Test
    public void testnonexistentPage() {
        WikiMediator testWiki = new WikiMediator(10, 5);
        String expected = "";
        String actual = testWiki.getPage("ldsjf;lasj;lve");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testgetPage() {
        WikiMediator testWiki = new WikiMediator(10, 5);
        String string1 = testWiki.getPage("Donald Trump");
        String string2 = testWiki.getPage("Mark Van Raamsdonk");
        String string3 = testWiki.getPage("Coke"); //General page that can refer to multiple things
        String string4 = testWiki.getPage("University of British Columbia");
        String string5 = testWiki.getPage("lego");
        System.out.println(string5);


        Assertions.assertTrue(string1.contains("Trump expanded the company's operations to building and renovating skyscrapers, hotels, casinos, and golf courses."));
        Assertions.assertTrue(string2.contains("Van Raamsdonk is a member of the \"It from Qubit\" collaboration, which was formed in 2015."));
        Assertions.assertTrue(string3.contains("===Soft drinks===\n" +
                "* [[Coca-Cola]], a brand of soft drink\n" +
                "**[[The Coca-Cola Company]], makers of Coca-Cola, Sprite, Fanta, and many other drinks\n" +
                "*[[Cola]], any soft drink similar to Coca-Cola\n" +
                "*[[Names for soft drinks in the_United States#Coke|Generic name for a soft drink]]"));
        Assertions.assertTrue(string4.contains("The Henry Marshall Tory Medal was established in 1941 by Tory, founding president of the University of Alberta and of the National Research Council of Canada, and a co-founder of Carleton University."));
        Assertions.assertTrue(string5.contains("Lego's popularity is demonstrated by its wide representation and usage in many forms of cultural works, including books, films and art work. It has even been used in the classroom as a teaching tool."));
    }

    /* Zeitgeist */
    @Test
    public void testEmptyZG() {
        WikiMediator testWiki = new WikiMediator(10, 50);
        List<String> expected = new ArrayList<>();

        Assertions.assertEquals(expected, testWiki.zeitgeist(1));
        Assertions.assertEquals(expected, testWiki.zeitgeist(2));
        Assertions.assertEquals(expected, testWiki.zeitgeist(5));
    }

    @Test
    public void testCorrectOrderZG() {
        WikiMediator testWiki = new WikiMediator(5, 50);
        List<String> expected = new ArrayList<>();
        expected.add("Chicken");
        expected.add("Beef");
        expected.add("Dog");

        for (int i = 0; i < 3; i++) {
            testWiki.search("Chicken", 3);
        }

        for (int i = 0; i < 2; i++) {
            testWiki.search("Beef", 3);
        }

        testWiki.getPage("Dog");

        Assertions.assertEquals(expected, testWiki.zeitgeist(3));
        Assertions.assertEquals(expected, testWiki.zeitgeist(3));

        expected.remove(expected.size() - 1);
        Assertions.assertEquals(expected, testWiki.zeitgeist(2));

        expected.remove(expected.size() - 1);
        Assertions.assertEquals(expected, testWiki.zeitgeist(1));
    }

    @Test
    public void testTieBreakerZG() {
        WikiMediator testWiki = new WikiMediator(3, 50);
        List<String> expected1 = new ArrayList<>();
        List<String> expected2 = new ArrayList<>();

        expected1.add("CPEN 221");
        expected1.add("CPEN 211");
        expected2.add("Sathish");
        expected2.add("Tor");

        for (int i = 0; i < 2; i++) {
            testWiki.getPage("CPEN 221");
            testWiki.getPage("CPEN 211");
        }

        testWiki.getPage("Sathish");
        testWiki.getPage("Tor");

        List<String> actual1 = new ArrayList<>();
        actual1.add("CPEN 221");
        actual1.add("CPEN 211");

        List<String> actual2 = new ArrayList<>();
        actual2.add("Sathish");
        actual2.add("Tor");

        Assertions.assertEquals(expected1, actual1);
        Assertions.assertEquals(expected2, actual2);
    }

    @Test
    public void testTieBreakerZG2() {
        WikiMediator testWiki = new WikiMediator(3, 50);
        List<String> expected1 = new ArrayList<>();
        List<String> expected2 = new ArrayList<>();

        expected1.add("Easter");
        expected1.add("BBBBBBBBBBB");
        expected1.add("Christmas");
        expected1.add("AAA");
        expected1.add("Sathish");
        expected1.add("Tor");

        expected2.add("Easter");
        expected2.add("BBBBBBBBBBB");
        expected2.add("Christmas");

        testWiki.getPage("AAA");

        for (int i = 0; i < 2; i++) {
            testWiki.getPage("Easter");
            testWiki.getPage("Christmas");
            testWiki.getPage("BBBBBBBBBBB");
        }

        testWiki.getPage("Sathish");
        testWiki.getPage("Tor");

        Assertions.assertEquals(expected1, testWiki.zeitgeist(7));
        Assertions.assertEquals(expected2, testWiki.zeitgeist(3));
    }

    @Test
    public void testZG () {
        WikiMediator testWiki = new WikiMediator(10, 5000);
        List<String> expected = new ArrayList<>();
        List<String> expected1 = new ArrayList<>();

        expected.add("Green");
        expected.add("Red");
        expected.add("Yellow");
        expected.add("Brown");
        expected.add("Blue");
        expected.add("Orange");

        expected1.add("Green");
        expected1.add("Red");
        expected1.add("Yellow");

        testWiki.getPage("Blue");
        testWiki.getPage("Red");
        testWiki.getPage("Red");
        testWiki.search("Green", 2);
        testWiki.search("Green", 2);
        testWiki.getPage("Green");
        testWiki.getPage("Yellow");
        testWiki.search("Yellow", 2);
        testWiki.getPage("Brown");
        testWiki.getPage("Red");
        testWiki.getPage("Orange");
        testWiki.getPage("Green");

        Assertions.assertEquals(expected, testWiki.zeitgeist(10000));
        Assertions.assertEquals(expected1, testWiki.zeitgeist(3));
    }

    /* Trending */
    @Test
    public void testEmptyTrending() {
        WikiMediator testWiki = new WikiMediator(10, 50);
        List<String> expected = new ArrayList<>();

        Assertions.assertEquals(expected, testWiki.trending(1, 3));
        Assertions.assertEquals(expected, testWiki.trending(2, 4));
        Assertions.assertEquals(expected, testWiki.trending(5, 10));
    }

    @Test
    public void testTrending() throws InterruptedException {
        WikiMediator testWiki = new WikiMediator(10, 50);
        List<String> expected = new ArrayList<>();
        List<String> expected1 = new ArrayList<>();


        expected.add("Canada");
        expected.add("Vietnam");
        expected.add("Russia");
        expected.add("India");

        expected1.add("Canada");
        expected1.add("Vietnam");

        testWiki.getPage("Canada");
        testWiki.getPage("United States");
        testWiki.getPage("Germany");

        Thread.sleep(1000);

        testWiki.zeitgeist(3);

        testWiki.search("China", 2);
        testWiki.getPage("Germany");
        testWiki.getPage("France");
        testWiki.search("Germany", 3);

        Thread.sleep(3000);
        testWiki.getPage("Canada");
        testWiki.search("Russia", 5);
        testWiki.getPage("India");
        testWiki.getPage("Vietnam");
        testWiki.getPage("Vietnam");
        testWiki.getPage("Canada");
        testWiki.getPage("Canada");


        List<String> result = testWiki.trending(3, 6);
        List<String> result1 = testWiki.trending(3, 2);

        Assertions.assertEquals(expected, result);
        Assertions.assertEquals(expected1, result1);

    }

    @Test
    public void testTrending1() throws InterruptedException {
        WikiMediator testWiki = new WikiMediator(10, 50);
        List<String> expected = new ArrayList<>();
        List<String> expected1 = new ArrayList<>();


        expected.add("Coca-Cola");
        expected.add("Sprite");
        expected.add("Pepsi");
        expected.add("Mountain Dew");

        expected1.add("Coca-Cola");
        expected1.add("Sprite");


        testWiki.getPage("Coca-Cola");
        testWiki.getPage("Pepsi");
        testWiki.getPage("7-Up");
        testWiki.getPage("Pepsi");

        Thread.sleep(1000);

        testWiki.search("Fanta", 2);
        testWiki.getPage("Fuitopia");
        testWiki.getPage("Coca-Cola");
        testWiki.getPage("Coca-Cola");


        Thread.sleep(1000);

        testWiki.search("Mountain Dew", 3);
        testWiki.getPage("Coca-Cola");
        testWiki.getPage("Mountain Dew");
        testWiki.search("Mountain Dew", 3);
        testWiki.getPage("Mountain Dew");


        Thread.sleep(7000);
        testWiki.getPage("Coca-Cola");
        testWiki.search("Coca-Cola", 5);
        testWiki.getPage("Pepsi");
        testWiki.getPage("Coca-Cola");
        testWiki.getPage("Sprite");
        testWiki.getPage("Sprite");
        testWiki.getPage("Mountain Dew");

        List<String> result = testWiki.trending(7, 50);
        List<String> result1 = testWiki.trending(7, 2);

        Assertions.assertEquals(expected, result);
        Assertions.assertEquals(expected1, result1);
    }

    /* windowedPeakL7oad */
    @Test
    public void testNoRequestWPL() {
        WikiMediator testWiki = new WikiMediator(10, 50);
        Assertions.assertEquals(0, testWiki.windowedPeakLoad(20));
        Assertions.assertEquals(0, testWiki.windowedPeakLoad(0));
        Assertions.assertEquals(0, testWiki.windowedPeakLoad());
    }

    @Test
    public void testWPL() throws InterruptedException {
        WikiMediator testWiki = new WikiMediator(10, 50);

        testWiki.search("Leonardo Dicaprio", 5);
        Thread.sleep(3000);

        testWiki.search("Attack on Titan", 1);
        Thread.sleep(3000);

        testWiki.getPage("Attack on Titan");
        Thread.sleep(3000);

        testWiki.zeitgeist(3);
        testWiki.trending(10, 5);
        testWiki.getPage("Orange");
        testWiki.getPage("Apple");


        Thread.sleep(3000);

        Assertions.assertEquals(4, testWiki.windowedPeakLoad(2));
        Assertions.assertEquals(7, testWiki.windowedPeakLoad());
    }

    @Test
    public void testWPL1() throws InterruptedException {
        WikiMediator testWiki = new WikiMediator(10, 5000);

        testWiki.search("Orange", 2);
        testWiki.getPage("Fruit");
        testWiki.getPage("Grape");
        Thread.sleep(6000);

        testWiki.search("Pear", 1);
        testWiki.search("Tangerine", 2);
        testWiki.getPage("Banana");

        Thread.sleep(6000);
        testWiki.search("Tangerine", 2);
        testWiki.getPage("Cherry");
        testWiki.zeitgeist(2);
        testWiki.windowedPeakLoad(0);
        testWiki.getPage("Blueberry");

        Thread.sleep(6000);
        testWiki.getPage("Pitaya");

        Assertions.assertEquals(5, testWiki.windowedPeakLoad(6));
    }
}
