/**
 * readium test case data class definition
 * 
 */
package org.readium.sdk.test;

import java.util.List;
/**
 * @author chtian@anfengde.com
 * 
 */
public class ReadiumTestCase {

    /**
     * <ul>
     * <li>xml definition:
     * 
     * <testcase name="open bad format file">
     * 
     * <function name="openBook"/>
     * 
     * <file name="a.epub3" url="http://google.com/a.epub3" />
     * 
     * <assert expression="=-1" msg="bad zip format"/>
     * 
     * </testcase>
     * 
     * <li> expression: if first char is '='(=-1), that mean check last function return value
     * <li> expression: normally like "MetaData.title=='Alice'" 
     * </ul>
     */

    private String name;
    private List<String> functions;
    private String file;
    private List<String> assertMessag;
    private List<String> assertExpression;

    public ReadiumTestCase() {
    }
}
