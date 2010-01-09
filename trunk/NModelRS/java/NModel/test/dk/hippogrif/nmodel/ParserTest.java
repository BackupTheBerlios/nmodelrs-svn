/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.hippogrif.nmodel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jgortz
 */
public class ParserTest {

    public ParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of static parse method, of class Parser.
     */
    @Test
    public void testStaticParse() {
        System.out.println("static parse");
        parse("A(5,Set<string>(\"c\",\"b\"))");
        parse("a(b(),\"c,)(g\",h,e(f))");
    }

    void parse(String s) {
        System.out.println(s);
        Term result = Parser.parse(s);
        System.out.println(result);
        assertTrue(s.equals(result.toString()));
    }

    /**
     * Test of parse method, of class Parser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        Parser instance = new Parser("action()");
        Term result = instance.parse();
        assertTrue("action()".equals(result.toString()));
        instance = new Parser("a(b(),c)");
        result = instance.parse();
        assertTrue("a(b(),c)".equals(result.toString()));
    }

    /**
     * Test of error method, of class Parser.
     */
    @Test(expected=NModelException.class)
    public void testError() {
        System.out.println("error");
        String msg = "";
        Parser instance = new Parser("abc");
        instance.error(msg);
    }

    /**
     * Test of next method, of class Parser.
     */
    @Test
    public void testNext() {
        System.out.println("next");
        Parser instance = new Parser("abc");
        assertEquals(instance.pos,0);
        assertEquals(instance.c,'a');
        instance.next();
        assertEquals(instance.pos,1);
        assertEquals(instance.c,'b');
        instance.next();
        instance.next();
        assertEquals(instance.pos,3);
        assertEquals(instance.c,0);
        instance.next();
        assertEquals(4,instance.pos);
        assertEquals(0,instance.c);
    }

    /**
     * Test of next method, of class Parser.
     */
    @Test(expected=NModelException.class)
    public void testNextc() {
        System.out.println("nextc");
        Parser instance = new Parser("abc");
        instance.nextc();
        instance.nextc();
        assertEquals(2,instance.pos);
        assertEquals('c',instance.c);
        instance.nextc();
    }

}