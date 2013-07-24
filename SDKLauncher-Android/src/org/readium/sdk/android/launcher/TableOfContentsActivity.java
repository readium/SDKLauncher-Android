package org.readium.sdk.android.launcher;

import org.readium.sdk.android.components.navigation.NavigationTable;

public class TableOfContentsActivity extends NavigationTableActivity {

	protected NavigationTable getNavigationTable() {
		NavigationTable navigationTable = null;
        if (pckg != null) {
        	navigationTable = pckg.getTableOfContents();
        }
		return (navigationTable != null) ? navigationTable : new NavigationTable("toc", "", "");
	}
}
