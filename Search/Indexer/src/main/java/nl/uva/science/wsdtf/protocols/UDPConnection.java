/*
 * Datagram sockets don't open connections, so how is security ensured 
 * for these sockets? When an inbound packet is received, the host name 
 * is checked. If the packet did not originate from the server, a 
 * SecurityException is immediately thrown. Obviously, sending comes 
 * under the same scrutiny. If a datagram socket tries to send to any 
 * destination except the server, a SecurityException is thrown. These 
 * restrictions apply only to the address, not the port number. Any port 
 * number on the host may be used.
 */
package nl.uva.science.wsdtf.protocols;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.logging.Level;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSContext;
// import org.ietf.jgss.MessageProp;

import org.gridforum.jgss.ExtendedGSSManager;
import org.gridforum.jgss.ExtendedGSSCredential;

import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.globus.gsi.gssapi.net.GssSocket;
import org.globus.gsi.gssapi.net.GssSocketFactory;
import org.globus.net.ServerSocketFactory;
import org.globus.net.SocketFactory;

import nl.uva.science.wsdtf.utilities.Options;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Security;

/**
 *  An implementation of a UDP connection. 
 * @author S. Koulouzis
 */
public class UDPConnection extends Connection {
	private DatagramSocket socket;

	private DatagramPacket packet;

	private InetAddress address;

	private int port;

	private GSSContext context;

	private Socket tcp_socket = null;

	private BufferedInputStream inStream = null;

	private BufferedOutputStream outStream = null;

	private ServerSocket _server = null;

	private DataOutputStream out_control;

	// private MessageProp prop;
	private DataInputStream in_control;

	private int pcketCount = 0;

	// private int index = 0;

	private int speed = 30;

	private boolean confirmPacket = false;

	private long start;

	private long stop;

	private int syncDelay = 1000;

	private int meanDelay = 0;

	private int accumulDelay = 0;

	private int counter = 0;

	private int diff = 0;

	private int socketTimeOut = 5000;

	// private Vector packetsBuffer = new Vector();

        /**
         * Creates a new instance of a UDP connection.
         * @param options
         */
	public UDPConnection(Options options) {
		setLogger();
		this.options = options;
		if (Constants.MAX_BUFFER_SIZE > Constants.Kbyte * 63) {
			Constants.setMaxBufferSize(Constants.Kbyte * 63);
			logger
					.warning("The UDP protocol cannot set a buffer size grater than 63 Kbytes. The buffer size is set to: 63 kbytes");
		}
		buffer = new byte[Constants.MAX_BUFFER_SIZE];
		try {
			address = InetAddress.getByName(options.getHostName());
			port = options.getPort();
			if (options.isServer()) {
				socket = new DatagramSocket(options.port);
				socket.setSendBufferSize(Constants.MAX_BUFFER_SIZE);
				// speed = options.getUdpSpeed();
			} else {
				socket = new DatagramSocket();
				socket.setReceiveBufferSize(Constants.MAX_BUFFER_SIZE);
				// Must set time out otherwise the client blcks for ever
				// @TO DO calculate round trip times and set time out not
				// hardcoded!!!!
				socket.setSoTimeout(socketTimeOut);
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Uncaught exception", ex);
		}
	}

	/**
         * Secures this UDP connection., by using a proxy certificate, and stating a separated TCP connection, which after authentication is closed.
         * This method doesn't encrypt the data send/received
         * @param options the options 
         * @return the authenticated connection.
         * @throws java.io.IOException
         */
	public UDPConnection SecureConnection(Options options) throws IOException {
            //calling socket.getInput(Output) stream(), starts an SSL handshake.
		ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
				.getInstance();
		try {
			if (options.isServer()) {
				byte proxyBytes[] = Security.readBinFile(null);

				GSSCredential credential = manager.createCredential(proxyBytes,
						ExtendedGSSCredential.IMPEXP_OPAQUE,
						GSSCredential.DEFAULT_LIFETIME, null,
						GSSCredential.INITIATE_AND_ACCEPT);

				logger.info(this.getName() + " new tcp socket@" + port);
				_server = ServerSocketFactory.getDefault().createServerSocket(
						port);
				logger.info("UDP: waiting fro incoming connections..");
				// System.out.println("UDP: waiting fro incoming
				// connections..:");
				tcp_socket = _server.accept();
				// tcp_socket.setSendBufferSize(Constants.MB);
				// System.out.println("UDP: got new connecton
				// from:"+tcp_socket.getInetAddress());
				logger.info("UDP: got new connecton from:"
						+ tcp_socket.getInetAddress());
				context = manager.createContext(credential);

				connected = true;
			} else {
				Socket _client = SocketFactory.getDefault().createSocket(
						options.getHostName(), port);
				tcp_socket = _client;
				logger.info("UDP: connected with:"
						+ tcp_socket.getInetAddress());
				// tcp_socket.setSendBufferSize(Constants.MB);
				/*
				 * Create a GSS Credential grid-proxy-init is required 1. Load
				 * COG propertirs 2. Read proxy file 3. Obtain a GSSManager
				 * instance 4. Create a GSSCredential w/ default values
				 */
				byte proxyBytes[] = Security.readBinFile(null);

				GSSCredential credential = manager.createCredential(proxyBytes,
						ExtendedGSSCredential.IMPEXP_OPAQUE,
						GSSCredential.DEFAULT_LIFETIME, null,
						GSSCredential.INITIATE_AND_ACCEPT);

				context = manager.createContext(null, null, credential,
						GSSContext.DEFAULT_LIFETIME);

				// GSSName targetName =
				// manager.createName("/O=Grid/OU=GlobusTest/OU=simpleCA-vagos.no-ip.org/CN=Globus",
				// null);
				// context = (ExtendedGSSContext)manager.createContext(null,
				// GSSConstants.MECH_OID,
				// credential,
				// GSSContext.DEFAULT_LIFETIME);

				context.requestCredDeleg(false);
				context.requestMutualAuth(true);

				connected = true;
			}

			GssSocketFactory factory = GssSocketFactory.getDefault();

			GssSocket gsiSocket = (GssSocket) factory.createSocket(tcp_socket,
					null, 0, context);
			gsiSocket.setWrapMode(gsiSocket.GSI_MODE);

			// Server or client socket
			gsiSocket.setUseClientMode(!options.isServer());
			gsiSocket.setAuthorization(NoAuthorization.getInstance());

			// if the data are not encrypted just authenticate the other side
			if (!options.encryptData) {
				gsiSocket.startHandshake();
			} else {
				tcp_socket = gsiSocket;
			}
			if (options.isServer()) {
				inStream = new BufferedInputStream(tcp_socket.getInputStream());
				in_control = new DataInputStream(inStream);
			} else {
				outStream = new BufferedOutputStream(tcp_socket
						.getOutputStream());
				out_control = new DataOutputStream(outStream);
			}

			if (gsiSocket.getContext().isEstablished()) {
				logger.finest("Mutual authentication took place!");
				logger.finest("Security context Established!");
				logger.finest("Client is " + context.getSrcName());
				logger.finest("Server is " + context.getTargName());
				secured = true;
			}

			if (context.getConfState())
				logger.finest("Confidentiality (i.e., privacy) is available");
			// System.out.println("Confidentiality (i.e., privacy) is
			// available");
			if (context.getIntegState())
				logger.finest("Integrity is available");
			// System.out.println("Integrity is available");

			if (options.isServer()) {
				// prop = new MessageProp(0, context.getConfState());
				// tcp_socket.setReceiveBufferSize(1);
				packet = new DatagramPacket(buffer, buffer.length);
				start = System.currentTimeMillis();
				socket.receive(packet);

				// byte[] dd = context.unwrap(packet.getData(), 0,
				// packet.getLength(), prop);
				// System.out.println("UDP: "+new String(dd));

				stop = System.currentTimeMillis();
				syncDelay = (int) (stop - start);
				address = tcp_socket.getInetAddress();
				port = packet.getPort();

			} else {
				// tcp_socket.setSendBufferSize(1);
				// prop = new MessageProp(0, context.getConfState());
				// String str = "This is a test message";
				// byte []tok = context.wrap(str.getBytes(), 0,
				// str.getBytes().length, prop);

				packet = new DatagramPacket(buffer, buffer.length, address,
						port);
				socket.send(packet);
			}

			socket.connect(address, port);

			// gsiSocket.close();
			// if (_server != null) {
			// _server.close();
			// }
		} catch (GSSException ex) {
			secured = false;
			logger.log(Level.SEVERE, "Uncaught exception", ex);
		}
		return this;
	}

        /**
         * Reads data from a datagram packet 
         * @return the data read 
         * @throws java.io.IOException
         */
	public byte[] read() throws IOException {
		try {
			packet = new DatagramPacket(buffer, buffer.length);
			start = System.currentTimeMillis();
			socket.receive(packet);
			stop = System.currentTimeMillis();

			if (confirmPacket && pcketCount % speed == 0) {
				diff = (int) ((stop - start) - syncDelay);
				logger.finest("diff=" + diff + " currDelay=" + (stop - start)
						+ " syncDelay=" + syncDelay + " speed=" + speed);
				// System.out.println("diff="+diff+" currDelay="+(stop-start)+"
				// syncDelay="+syncDelay+" speed="+speed);
				if (diff != 0) {
					if (diff < 0) {
						syncDelay = (int) (stop - start);
					}
					if (diff > 10) {
						syncDelay++;
					}
					out_control.writeInt(syncDelay);
					out_control.flush();
				}
				if (diff == 0) {
					speed = speed + 5;
				} else if (diff > 0 && diff <= 6) {
					speed++;
				}
				// socket.setSoTimeout(syncDelay+20);
			}
			pcketCount++;
		} catch (SocketTimeoutException ex) {
			if (ex.getMessage().equals("Receive timed out")) {
				logger.finer("Stream is finshed " + ex.toString());
				throw new IOException("CONNECTION_EOS");
			} else {
				logger.severe(ex.toString());
				ex.printStackTrace();
			}
		} catch (SocketException ex) {
			if (ex.getMessage().equals("Broken pipe")) {
				logger.finer("Stream is finshed " + ex.toString());
				throw new IOException("CONNECTION_EOS");
			} else {
				logger.log(Level.SEVERE, "Uncaught exception", ex);
				// ex.printStackTrace();
			}
		}
		return buffer;
	}

        /**
         * Writes data to a datagram packet.
         * @param b the byte array 
         * @param off the offset
         * @param len, the length 
         * @throws java.io.IOException
         */
	public void write(byte b[], int off, int len) throws IOException {
		packet = new DatagramPacket(b, len, address, port);
		if (in_control.available() > 0) {
			syncDelay = in_control.readInt();
			counter++;
			accumulDelay = accumulDelay + syncDelay;
			meanDelay = (accumulDelay / counter) + 10;
			logger.finest(this.getName() + " syncDelay=" + syncDelay
					+ " meanDelay=" + meanDelay + " accumulDelay="
					+ accumulDelay);
			if ((counter % 50) == 0 && counter > 0 && meanDelay > 5) {
				meanDelay = meanDelay - 3;
			}
			// System.out.println(this.getName()+" syncDelay=" + syncDelay+"
			// meanDelay="+meanDelay +" accumulDelay="+accumulDelay);
		}
		try {
			Thread.sleep(meanDelay);
		} catch (InterruptedException e) {
			// e.printStackTrace();
			logger.log(Level.SEVERE, "Uncaught exception", e);
			closeConnection();
			// return;
		}
		socket.send(packet);
		pcketCount++;
	}

        /**
         * Cleses this connection.
         */
	public void closeConnection() {
		try {
			if (out_control != null) {
				out_control.flush();
				out_control.close();
				outStream.flush();
				outStream.close();
			}
			if (in_control != null) {
				in_control.close();
				inStream.close();
			}
			if (_server != null) {
				_server.close();
			}
			if (tcp_socket != null) {
				tcp_socket.close();
			}
		} catch (IOException ex) {
			// ex.printStackTrace();
		} finally {
			try {
				if (out_control != null) {
					out_control.close();
					outStream.close();
				}
				if (in_control != null) {
					in_control.close();
					inStream.close();
				}
				if (_server != null) {
					_server.close();
				}
				if (tcp_socket != null) {
					tcp_socket.close();
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Uncaught exception", e);
				// e.printStackTrace();
			}
		}
		// System.out.println("UDP: Connectig socket@"+socket.getLocalPort());
		if (socket != null) {
			socket.disconnect();
			socket.close();
		}
		socket = null;
		connected = false;
		secured = false;
		// System.out.println("UDP: Connection closed");
	}

        /**
         * Checks if connection is up
         * @return true if connection is up
         */
	public boolean isConnected() {
		// System.out.println("TCPConnection IN ISCONNECTED:--------------");
		// System.out.println("TCPCnnection: address= " + address);
		// System.out.println("socket:"+socket);
		// System.out.println("connected:"+connected);
		// System.out.println("secured:"+secured);
		// System.out.println("socket.isConnected():"+tcp_socket.isConnected());
		// System.out.println("socket.isBound():"+tcp_socket.isBound());
		// System.out.println("socket.isClosed():"+tcp_socket.isClosed());
		// System.out.println("socket.isInputShutdown():"+tcp_socket.isInputShutdown());
		// System.out.println("socket.isOutputShutdown():"+tcp_socket.isOutputShutdown());
		// System.out.println("socket.isBound():"+tcp_socket.isBound());
		// System.out.println("socket.getInetAddress():"+tcp_socket.getInetAddress());
		// System.out.println("socket.getLocalSocketAddress():"+tcp_socket.getLocalSocketAddress());
		// System.out.println("socket.isBound():"+tcp_socket.isBound());
		// System.out.println("TCPConnection IN ISCONNECTED:--------------");
		if (socket == null || tcp_socket == null) {
			return false;
		}
		if (socket.isConnected()) {
			connected = true;
		} else {
			connected = false;
		}
		return connected;
	}

        
	public byte[] getBuffer() {
		return null;
	}

	public BufferedOutputStream OutputStream() throws IOException {
		return null;
	}
}
