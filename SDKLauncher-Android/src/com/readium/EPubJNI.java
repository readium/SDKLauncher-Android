package com.readium;

public class EPubJNI {
    static {
        System.loadLibrary("epub3");
    }

    public final native EPubContainer openBook(final String path);
    public final native void closeBook(final EPubContainer container);
}
