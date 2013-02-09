package ly.dollar.config;

import java.io.Serializable;

public interface ConfigSvc 
{
	<T extends Serializable> T get(String key);
	
	<T extends Serializable> void set(String key, T value);
	
	// Break these out into a lock svc
	boolean acquireLock(String lockName);
	
	void releaseLock(String lockName);
}

