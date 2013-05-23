package com.readium;

import java.util.ArrayList;

public class EPubContainer {

    private String path;
    private ArrayList<EPubPackage> packages;
    
    public final ArrayList<EPubPackage> getPackages() {
        return packages;
    }

    public final void setPackages(ArrayList<EPubPackage> packages) {
        this.packages = packages;
    }

    public EPubContainer(String path) {
        super();
        this.setPath(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
