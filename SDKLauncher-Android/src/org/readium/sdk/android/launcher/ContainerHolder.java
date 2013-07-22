package org.readium.sdk.android.launcher;

import java.util.HashMap;
import java.util.Map;

import org.readium.sdk.android.Container;

public class ContainerHolder {

	private static final ContainerHolder INSTANCE = new ContainerHolder();
	
	private final Map<Long, Container> containers = new HashMap<Long, Container>();
	
	public static ContainerHolder getInstance() {
		return INSTANCE;
	}

	public Container get(Object arg0) {
		return containers.get(arg0);
	}

	public Container remove(Object arg0) {
		return containers.remove(arg0);
	}

	public Container put(Long key, Container value) {
		return containers.put(key, value);
	}
	
	
}
