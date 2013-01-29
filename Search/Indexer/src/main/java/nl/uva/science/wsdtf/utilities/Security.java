package nl.uva.science.wsdtf.utilities;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.common.CoGProperties;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;

/**
 * 
 * @author S. Koulouzis
 */
public class Security {

	public static GSSCredential loadProxyCredential(String path) {
		if (path == null) {
			CoGProperties cog = CoGProperties.getDefault();			
			path = cog.getProxyFile();//"/tmp/x509up_u1000";
		}
		GlobusGSSCredentialImpl cred = null;
		try {
			GlobusCredential gcred = new GlobusCredential(path);
			cred = new GlobusGSSCredentialImpl(gcred,GSSCredential.DEFAULT_LIFETIME);
		} catch (GlobusCredentialException ex) {
			ex.printStackTrace();
			System.err.println("Try runing: 'grid-proxy-init'");
			System.exit(-1);
		} catch (GSSException ex) {		
			ex.printStackTrace();
			System.err.println("Try runing: 'grid-proxy-init'");
			System.exit(-1);				
		}
		return cred;
	}

	/*
	 * protected byte[] readToken() throws IOException { byte[] buf = null; if
	 * (SSLUtil.read(this.in, this.header, 0, this.header.length-1) < 0) {
	 * return null; } if (SSLUtil.isSSLv3Packet(this.header)) { this.mode =
	 * GssSocket.SSL_MODE; // read the second byte of packet length field if
	 * (SSLUtil.read(this.in, this.header, 4, 1) < 0) { return null; } int len =
	 * SSLUtil.toShort(this.header[3], this.header[4]); buf = new
	 * byte[this.header.length + len]; System.arraycopy(this.header, 0, buf, 0,
	 * this.header.length); if (SSLUtil.read(this.in, buf, this.header.length,
	 * len) < 0) { return null; } } else if
	 * (SSLUtil.isSSLv2HelloPacket(this.header)) { this.mode =
	 * GssSocket.SSL_MODE; // SSLv2 - assume 2-byte header // read extra 2 bytes
	 * so subtract it from total len int len = (((header[0] & 0x7f) << 8) |
	 * (header[1] & 0xff)) - 2; buf = new byte[this.header.length-1 + len];
	 * System.arraycopy(this.header, 0, buf, 0, this.header.length-1); if
	 * (SSLUtil.read(this.in, buf, this.header.length-1, len) < 0) { return
	 * null; } } else { this.mode = GssSocket.GSI_MODE; int len =
	 * SSLUtil.toInt(this.header, 0); if (len > MAX_LEN) { throw new
	 * IOException("Token length " + len + " > " + MAX_LEN); } else if (len < 0) {
	 * throw new IOException("Token length " + len + " < 0"); } buf = new
	 * byte[len]; if (SSLUtil.read(this.in, buf, 0, buf.length) < 0) { return
	 * null; } } return buf; }
	 */

	public static int read(InputStream in, byte[] buf, int off, int len)
			throws IOException {
		int n = 0;
		while (n < len) {
			int count = in.read(buf, off + n, len - n);
			if (count < 0) {
				return count;
			}
			n += count;
		}
		return len;
	}

	/**
	 * Read a local binary file
	 * 
	 * @param path
	 * @return
	 */
	public static byte[] readBinFile(String path) {
		if(path==null){
			CoGProperties cog = CoGProperties.getDefault();
			path = cog.getProxyFile();
		}
		byte data[] = null;						
		try {
			RandomAccessFile raf = new RandomAccessFile(path, "r");
			data = new byte[(int) raf.length()];
			raf.readFully(data);
		}catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return data;
	}

}
