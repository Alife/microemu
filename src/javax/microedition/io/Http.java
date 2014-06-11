package javax.microedition.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Http implements HttpConnection {
	
	private HttpConnection a;

	protected Http(HttpConnection paramHttpConnection, int paramInt) {
		a = paramHttpConnection;
	}

	public void close() throws IOException {
		a.close();
	}

	public long getDate() throws IOException {
		return a.getDate();
	}

	public String getEncoding() {
		return a.getEncoding();
	}

	public long getExpiration() throws IOException {
		return a.getExpiration();
	}

	public String getFile() {
		return a.getFile();
	}

	public String getHeaderField(int paramInt) throws IOException {
		return a.getHeaderField(paramInt);
	}

	public String getHeaderField(String paramString) throws IOException {
		return a.getHeaderField(paramString);
	}

	public long getHeaderFieldDate(String paramString, long paramLong) throws IOException {
		return a.getHeaderFieldDate(paramString, paramLong);
	}

	public int getHeaderFieldInt(String paramString, int paramInt) throws IOException {
		return a.getHeaderFieldInt(paramString, paramInt);
	}

	public String getHeaderFieldKey(int paramInt) throws IOException {
		return a.getHeaderFieldKey(paramInt);
	}

	public String getHost() {
		return a.getHost();
	}

	public long getLastModified() throws IOException {
		return a.getLastModified();
	}

	public long getLength() {
		return a.getLength();
	}

	public int getPort() {
		return a.getPort();
	}

	public String getProtocol() {
		return a.getProtocol();
	}

	public String getQuery() {
		return a.getQuery();
	}

	public String getRef() {
		return a.getRef();
	}

	public String getRequestMethod() {
		return a.getRequestMethod();
	}

	public String getRequestProperty(String paramString) {
		return a.getRequestProperty(paramString);
	}

	public int getResponseCode() throws IOException {
		return a.getResponseCode();
	}

	public String getResponseMessage() throws IOException {
		return a.getResponseMessage();
	}

	public String getType() {
		return a.getType();
	}

	public String getURL() {
		return a.getURL();
	}

	public DataInputStream openDataInputStream() throws IOException {
		return a.openDataInputStream();
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		return a.openDataOutputStream();
	}

	public InputStream openInputStream() throws IOException {
		return a.openInputStream();
	}

	public OutputStream openOutputStream() throws IOException {
		return a.openDataOutputStream();
	}

	public void setRequestMethod(String paramString) throws IOException {
		a.setRequestMethod(paramString);
	}

	public void setRequestProperty(String paramString1, String paramString2) throws IOException {
		a.setRequestProperty(paramString1, paramString2);
	}
}
