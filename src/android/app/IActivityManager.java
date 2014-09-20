package android.app;

import android.content.res.Configuration;
import android.os.RemoteException;

public abstract interface IActivityManager {
	public abstract Configuration getConfiguration() throws RemoteException;

	public abstract void updateConfiguration(Configuration paramConfiguration)
			throws RemoteException;
}