package org.readium.sdk.android.launcher;

import org.readium.sdk.android.components.navigation.NavigationTable;

public class ListOfTablesActivity extends NavigationTableActivity {

	protected NavigationTable getNavigationTable() {
		NavigationTable navigationTable = null;
        if (pckg != null) {
        	navigationTable = pckg.getListOfTables();
        }
		return (navigationTable != null) ? navigationTable : new NavigationTable("lot", "", "");
	}
}
