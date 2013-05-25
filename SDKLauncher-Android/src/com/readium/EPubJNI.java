package com.readium;

public class EPubJNI {
    static {
        System.loadLibrary("epub3");
    }

    public final native int openBook(final String path);
    public final native void closeBook(final int handle);
}
