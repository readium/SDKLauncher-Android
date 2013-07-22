package org.readium.sdk.android.launcher;

import org.readium.sdk.android.components.navigation.NavigationTable;

public class ListOfFiguresActivity extends NavigationTableActivity {

	protected NavigationTable getNavigationTable() {
		NavigationTable navigationTable = null;
        if (pckg != null) {
        	navigationTable = pckg.getListOfFigures();
        }
		return (navigationTable != null) ? navigationTable : new NavigationTable("lof", "", "");
	}
}
