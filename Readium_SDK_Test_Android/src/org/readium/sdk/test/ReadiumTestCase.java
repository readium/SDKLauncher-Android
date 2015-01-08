/**
 * readium test case data class definition
 * 
 */
package org.readium.sdk.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author chtian@anfengde.com
 * 
 */
public class ReadiumTestCase {

    /**
     * <ul>
     * <li>xml definition: <code>
     * <testcase name="open bad format file">
     * 
     * <function name="openBook"/>
     * 
     * <file name="a.epub3" url="http://google.com/a-christmas-carol3.epub" />
     * 
        <assert
            expression="container.name==='a-christmas-carol3.epub'"
            msg="open a-christmas-carol3.epub file failed" />
     * 
     * </testcase>
     * </code>
     * <li>expression: normally like "container.name==='a-christmas-carol3.epub'"
     * </ul>
     */

    private String name;
    private List<String> functions = new ArrayList<String>();
    private String file;
    private String url;
    private List<String> assertMessag = new ArrayList<String>();
    private List<String> assertExpression = new ArrayList<String>(); 

    public ReadiumTestCase() {
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final List<String> getFunctions() {
        return functions;
    }

    public final void addFunction(String function) {
        this.functions.add(function);
    }

    public final String getFile() {
        return file;
    }

    public final void setFile(String file) {
        this.file = file;
    }

    public final String getUrl() {
        return url;
    }

    public final void setUrl(String url) {
        this.url = url;
    }

    public final List<String> getAssertMessag() {
        return assertMessag;
    }

    public final void addAssertMessag(String assertMessag) {
        this.assertMessag.add(assertMessag);
    }

    public final List<String> getAssertExpression() {
        return assertExpression;
    }

    public final void addAssertExpression(String assertExpression) {
        this.assertExpression.add(assertExpression);
    }

    private final String getExprJson() {
        String json = "[";
        for (Iterator<String> i = assertExpression.iterator(); i.hasNext();) {
            String expr = i.next();
            json = json + "\"" + expr + "\"";
            if (i.hasNext()) json += ",";
        }
        json += "]";
        return json;
    }

    public final String getJson() {
        return "{\"testName\":\"" + name + "\",\"testExpr\":" + getExprJson() + "}";
    }

}
