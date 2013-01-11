/**
 * 
 */
package nl.uva.science.wsdtf.protocols;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.Timer;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.logging.Level;

import nl.uva.science.wsdtf.utilities.Options;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Security;

import org.globus.common.CoGProperties;
import org.globus.gsi.gssapi.net.GssSocket;
import org.globus.gsi.gssapi.net.GssSocketFactory;
import org.globus.net.ServerSocketFactory;
import org.globus.net.SocketFactory;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSContext;

import org.globus.gsi.gssapi.auth.*;

/**
 * @author E. Angelou
 * 
 */
public class RTSPConnection extends Connection implements ActionListener {

	private long chunk = 0;

	private ServerSocket _server = null;

	private Socket _client = null;

	private Socket socket = null;

	private BufferedInputStream inStream = null;

	private BufferedOutputStream outStream = null;

	/**
	 * RTP variables:
	 */

	private DatagramSocket RTPsocket; // socket to be used to send and receive
										// UDP

	private int socketTimeOut = 9000;

	/**
	 * packets
	 */
	DatagramPacket senddp; // UDP packet containing the video frames

	InetAddress ClientIPAddr; // Client IP address

	int RTP_dest_port = 0; // destination port for RTP packets (given by the

	// RTSP Client)

	// Video variables:\
	// ----------------
	int imagenb = 0; // image nb of the image currently transmitted

	// VideoStream video; // VideoStream object used to access video frames

	int MJPEG_TYPE = 26; // RTP payload type for MJPEG video

	int FRAME_PERIOD = 35; // Frame period of the video to stream, in ms

	// int VIDEO_LENGTH = 9999999; // length of the video in frames

	Timer timer; // timer used to send the images at the video frame rate

	byte[] buf;

	// the images to send to
	// the client

	// RTSP variables
	// ----------------
	// rtsp states
	final static int INIT = 0;

	final static int READY = 1;

	final static int PLAYING = 2;

	// rtsp message types
	final static int SETUP = 3;

	final static int PLAY = 4;

	final static int PAUSE = 5;

	final static int TEARDOWN = 6;

	int state; // RTSP Server state == INIT or READY or PLAY

	int request_type;

	BufferedReader RTSPBufferedReader;

	BufferedWriter RTSPBufferedWriter;

	String VideoFileName; // video file requested from the client

	int RTSP_ID = 123456; // ID of the RTSP session

	int RTSPSeqNb = 0; // Sequence number of RTSP messages within the

	// session

	int payload_length;

	byte[] payload = null;

	// Client only variables
	// RTP variables:
	// ----------------
	DatagramPacket rcvdp; // UDP packet received from the server

	static int RTP_RCV_PORT = 25000; // port where the client will receive

	int RTSPid = 0; // ID of the RTSP session (given by the RTSP Server)

	final static String CRLF = "\r\n";

	/**
	 * 
	 * @param options
	 */
	public RTSPConnection(Options options) throws IOException {

		// init Frame
		super();
		buffer = new byte[Constants.MAX_BUFFER_SIZE];
		setLogger();
		if (Constants.MAX_BUFFER_SIZE > Constants.Kbyte * 63) {
			Constants.setMaxBufferSize(Constants.Kbyte * 63);
			logger
					.warning("The RTSP (UDP) protocol cannot set a buffer size grater than 63 Kbytes. The buffer size is set to: 63 kbytes");
		}
		buf = new byte[Constants.MAX_BUFFER_SIZE]; // buffer used to store
		// Create an initial TCP connection to authenticate and receive
		// options, a secure RTSP socket using GSS

		if (options.isServer()) {
			try {
				this._server = ServerSocketFactory.getDefault()
						.createServerSocket(options.getPort());
				logger.info("created new server socket @ " + options.getPort());
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "Uncaught exception", ex);
				// ex.printStackTrace();
			}
		} else {
			try {
				this._client = SocketFactory.getDefault().createSocket(
						options.getHostName(), options.getPort());
				logger.info("connected with server @ " + options.getPort());
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "Uncaught exception", ex);
				// ex.printStackTrace();
			}
		}

		// // Secure the initial socket and authenticate client/server
		logger
				.finest("Secure the initial socket and authenticate client/server");
		try {
			ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
					.getInstance();
			GSSContext context = null;

			if (options.isServer()) {
				/*
				 * Load GSS Credential: 1. Load proxy path from COG properties
				 * 2. Read the proxy file 3. Create a GSS credentail w/ proxy
				 * data
				 */
				CoGProperties cog = CoGProperties.getDefault();
				byte proxyBytes[] = Security.readBinFile(cog.getProxyFile());

				GSSCredential credential = manager.createCredential(proxyBytes,
						ExtendedGSSCredential.IMPEXP_OPAQUE,
						GSSCredential.DEFAULT_LIFETIME, null,
						GSSCredential.INITIATE_AND_ACCEPT);
				logger.finest("Server Credential: " + credential.getName());
				// System.out.println("Server Credential: " +
				// credential.getName());
				//				
				// System.out.println("RTSP @ " + _server.getLocalPort()+ " is
				// waiting for connections");
				logger.info("RTSP @ " + _server.getLocalPort()
						+ " is waiting for connections");
				socket = _server.accept();
				socket.setSendBufferSize(Constants.MAX_BUFFER_SIZE);
				// System.out.println("RTSP @ " + socket.getLocalPort()+ " is
				// connected with"+ socket.getRemoteSocketAddress());
				logger.info("RTSP @ " + socket.getLocalPort()
						+ " is connected with"
						+ socket.getRemoteSocketAddress());

				// /*
				// * Create a GSSContext to receive the incoming request
				// * from the client. Use null for the server credentials
				// * passed in. This tells the underlying mechanism
				// * to use whatever credentials it has available that
				// * can be used to accept this connection.
				// */

				context = manager.createContext(credential);
			} else {
				socket = this._client;
				socket.setReceiveBufferSize(Constants.MAX_BUFFER_SIZE);
				// System.out.println("RTSP @ " + socket.getLocalPort()+ " is
				// connected with"+ socket.getRemoteSocketAddress());

				/*
				 * Create a GSS Credential grid-proxy-init is required 1. Load
				 * COG propertirs 2. Read proxy file 3. Obtain a GSSManager
				 * instance 4. Create a GSSCredential w/ default values
				 */
				CoGProperties cog = CoGProperties.getDefault();
				byte proxyBytes[] = Security.readBinFile(cog.getProxyFile());

				// System.out.println("Client: Loaded proxy file: "+
				// cog.getProxyFile());

				logger.finest("Client: Loaded proxy file: "
						+ cog.getProxyFile());

				GSSCredential credential = manager.createCredential(proxyBytes,
						ExtendedGSSCredential.IMPEXP_OPAQUE,
						GSSCredential.DEFAULT_LIFETIME, null,
						GSSCredential.INITIATE_AND_ACCEPT);

				// System.out.println("Client Credential: " +
				// credential.getName()+ " Remaining life time:"+
				// credential.getRemainingLifetime());

				logger.finest("Client Credential: " + credential.getName()
						+ " Remaining life time:"
						+ credential.getRemainingLifetime());

				context = manager.createContext(null, null, credential,
						GSSContext.DEFAULT_LIFETIME);

				context.requestCredDeleg(false);
				context.requestMutualAuth(true);
			}

			GssSocketFactory factory = GssSocketFactory.getDefault();

			GssSocket gsiSocket = (GssSocket) factory.createSocket(socket,
					null, 0, context);

			gsiSocket.setWrapMode(gsiSocket.GSI_MODE);

			// Server or client socket
			gsiSocket.setUseClientMode(!options.isServer());
			gsiSocket.setAuthorization((Authorization) NoAuthorization
					.getInstance());// options.getAuthorization());

			if (!options.encryptData) {
				gsiSocket.startHandshake();
			} else {
				socket = gsiSocket;
			}

			if (gsiSocket.getContext().isEstablished()) {
				logger.finest("Security context Established!");
				logger.finest("Server is " + context.getTargName());
				logger.finest("Client is " + context.getSrcName());

			}

			socket = gsiSocket;
			// if (options.isServer()) {
			outStream = new BufferedOutputStream(socket.getOutputStream());
			// } else {
			inStream = new BufferedInputStream(socket.getInputStream());
			// }

		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Authentication Failed!!!", ex);
			// ex.printStackTrace();
			// System.exit(0);
			// System.out.println("Client Authentication Failed!!!");
			if (options.isServer()) {
				_server.close();
			} else {
				_client.close();
			}
			socket.close();
			throw new IOException("CONNECTION_EOS");

		}

		// OK, now start the actual server
		if (options.isServer()) {
			try {

				// Get Client IP address
				ClientIPAddr = socket.getInetAddress();

				// Initiate RTSPstate
				state = INIT;

				// Set input and output stream filters:
				RTSPBufferedReader = new BufferedReader(new InputStreamReader(
						inStream));
				RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(
						outStream));

				// Wait for the SETUP message from the client
				boolean done = false;
				while (!done) {
					request_type = parse_RTSP_request(); // blocking
					logger.finer("request_type=" + request_type);
					// System.out.println("request_type=" + request_type);
					if (request_type == SETUP) {
						done = true;

						// update RTSP state
						state = READY;
						// System.out.println("New RTSP state: READY");
						logger.finer("New RTSP state: READY");
						// Send response
						send_RTSP_response();

						// init the VideoStream object:
						// video = new VideoStream(VideoFileName);

						// init RTP socket
						RTPsocket = new DatagramSocket();
						// RTPsocket.setSendBufferSize(Constants.MAX_BUFFER_SIZE);
					}
				}

			} catch (Exception se) {
				logger.log(Level.SEVERE, "Authentication Failed!!!", se);
				se.printStackTrace();
			}

		} else {

			try {
				// Set input and output stream filters:
				RTSPBufferedReader = new BufferedReader(new InputStreamReader(
						inStream));
				RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(
						outStream));
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Uncaught exception", e);
				// e.printStackTrace();
			}

			// init RTSP state:
			state = INIT;

			// System.out.println("Client connected at: "+filename);

			// System.out.println("Setup Button pressed !");
			logger.info("Setup Button pressed !");

			// Send the SETUP message to initiate the transaction

			if (state == INIT) {
				// Init non-blocking RTPsocket that will be used to receive data
				try {
					// construct a new DatagramSocket to receive RTP packets
					// from the server, on port RTP_RCV_PORT
					// RTPsocket = ...

					RTPsocket = new DatagramSocket(RTP_RCV_PORT);
					RTPsocket.setReceiveBufferSize(Constants.MAX_BUFFER_SIZE);
					// set TimeOut value of the socket to 5msec.
					// ....

					RTPsocket.setSoTimeout(socketTimeOut);

					// RTPsocket.bind(new InetSocketAddress(RTP_RCV_PORT));
					// System.out.println("Socket Port:"+RTPsocket.getPort());
				} catch (SocketException se) {
					logger.log(Level.SEVERE, "Uncaught exception", se);
					// se.printStackTrace();
					// System.out.println("Socket exception: " + se);
					// System.exit(0);
				}
				logger.fine("Datagram socket ready!");
				// System.out.println("Datagram socket ready!");

				// init RTSP sequence number
				RTSPSeqNb = 1;

				// Send SETUP message to the server
				send_RTSP_request("SETUP");

				// Wait for the response
				if (parse_server_response() != 200)
					logger.warning("Invalid Server Response");
				// System.out.println("Invalid Server Response");
				else {
					// change RTSP state and print new state
					state = READY;
					logger.finer("New RTSP state: ...." + state);
					// System.out.println("New RTSP state: ...." + state);
				}
			}// else if state != INIT then do nothing

			// Send the PLAY message to begin the transfer

			// System.out.println("Play Button pressed !");
			logger.info("Play Button pressed !");
			if (state == READY) {
				// increase RTSP sequence number
				// .....
				RTSPSeqNb++;

				// Send PLAY message to the server
				send_RTSP_request("PLAY");

				// Wait for the response
				if (parse_server_response() != 200)
					logger.warning("Invalid Server Response");
				// System.out.println("Invalid Server Response");
				else {
					// change RTSP state and print out new state
					// .....

					state = PLAYING;
					logger.finer("New RTSP state: ...." + state);
					// System.out.println("New RTSP state: ..." + state);
				}
			}// else if state != READY then do nothing

		}

	}

	public byte[] read() throws IOException {
		logger.finest("Reading");
		try {
			// reset the buffer
			buffer = null;
			logger.finest("setting  buffer=null");
			// buf = new byte[1024];

			// While the buffer is null, or the file is empty
			while (buffer == null || buffer.length == 0) {
				// try to get the receive buffer

				// Construct a DatagramPacket to receive data from the UDP
				// socket
				rcvdp = new DatagramPacket(buf, buf.length);

				try {
					// receive the DP from the socket:
					RTPsocket.receive(rcvdp);

					// create an RTPpacket object from the DP
					RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp
							.getLength());

					// print important header fields of the RTP packet received:
					// System.out.println("Got RTP packet with SeqNum # "
					// + rtp_packet.getsequencenumber() + " TimeStamp "
					// + rtp_packet.gettimestamp() + " ms, of type "
					// + rtp_packet.getpayloadtype());
					logger.finest("Got RTP packet with SeqNum # "
							+ rtp_packet.getsequencenumber() + " TimeStamp "
							+ rtp_packet.gettimestamp() + " ms, of type "
							+ rtp_packet.getpayloadtype());
					// print header bitstream:
					// rtp_packet.printheader();

					// get the payload bitstream from the RTPpacket object
					payload_length = rtp_packet.getpayload_length();
					if (payload == null || payload_length != payload.length) {
						payload = new byte[payload_length];
					}
					rtp_packet.getpayload(payload);

					// Set the internal buffer to the received payload
					buffer = payload;
					// datarecived = datarecived + buffer.length;
					// System.out.println("RTSP:
					// datarecived="+datarecived/Constants.MB);
				} catch (InterruptedIOException iioe) {
					logger
							.finer("Nothing to read, throwing CONNECTION_EOS exception");
					// System.out.println("Nothing to read");
					// closeConnection();
					throw new IOException("CONNECTION_EOS");

				} catch (IOException ioe) {
					logger.log(Level.SEVERE, "Uncaught exception", ioe);
					// System.out.println("Exception caught: " + ioe);
					// ioe.printStackTrace();
				}

				/*
				 * for (int i = 0; i < buffer.length; i++) {
				 * System.out.print(buffer[i] + " "); } System.out.println();
				 */
			}

			// One more chunk read...
			chunk++;

		} catch (Exception e) {
			// closeConnection();
			logger.finer("Nothing else to read. buffer is :" + buffer
					+ "Exception:" + e + " throwing CONNECTION_EOS exception");
			// System.out.println("Nothing else to read\nCaught Exception:" +
			// e);
			throw new IOException("CONNECTION_EOS");

		}

		return buffer;
	}

	public void write(byte data[], int off, int len) throws IOException {
		logger.finest("writing");
		try {
			request_type = -1;
			Thread.sleep(FRAME_PERIOD);
			// parse the RTSP request so as to know what to do
			if (inStream.available() > 0) {
				request_type = parse_RTSP_request(); // blocking
				logger.finer("Received new request while streaming!!!");
				// System.out.println("Received new request while
				// streaming!!!");
			}
			if ((request_type == PLAY) && (state == READY)) {
				// send back response
				send_RTSP_response();

				// update state
				state = PLAYING;
				// System.out.println("New RTSP state: PLAYING");
				logger.info("New RTSP state: PLAYING");
			} else if ((request_type == PAUSE) && (state == PLAYING)) {
				// send back response
				send_RTSP_response();

				// update state
				state = READY;
				logger.info("New RTSP state: READY");
				// System.out.println("New RTSP state: READY");
			} else if (request_type == TEARDOWN) {
				// System.out.println("New RTSP state: TEARDOWN");
				logger.info("New RTSP state: TEARDOWN");
				// send back response
				send_RTSP_response();

				// close sockets
				_server.close();
				socket.close();
				RTPsocket.close();
				// System.exit(0);
			}

			buf = data;

			// OK, now send the data...
			// if the current image nb is less than the length of the video
			// if (imagenb < VIDEO_LENGTH) {
			// update current imagenb
			imagenb++;

			try {
				// get next frame to send from the video, as well as its
				// size
				int image_length = buf.length; // video.getnextframe(buf);

				// Builds an RTPpacket object containing the frame
				RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb,
						imagenb * FRAME_PERIOD, buf, image_length);

				// get to total length of the full rtp packet to send
				int packet_length = rtp_packet.getlength();

				// retrieve the packet bitstream and store it in an array of
				// bytes
				byte[] packet_bits = new byte[packet_length];
				rtp_packet.getpacket(packet_bits);

				// send the packet as a DatagramPacket over the UDP socket
				senddp = new DatagramPacket(packet_bits, packet_length,
						ClientIPAddr, RTP_dest_port);
				RTPsocket.send(senddp);

				// System.out.println("Send frame #"+imagenb);
				// print the header bitstream
				// rtp_packet.printheader();

			} catch (Exception ex) {
				// ex.printStackTrace();
				logger.log(Level.SEVERE, "Uncaught exception", ex);
				throw new IOException();
				// System.exit(0);
			}
			// } else {
			// logger.finest("RTSPConnection: EOF!!!!!");
			// System.out.println("RTSPConnection: EOF!!!!!");
			//
			// }

			// outStream.write(data);
			// outStream.flush();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Uncaught exception", ex);
			// ex.printStackTrace();
			throw new IOException();
		}
	}

	public Connection SecureConnection(Options options) {
		return this;
	}

	public void closeConnection() {
		logger.finer("closing connection");
		try {
			// Check if you're the client (only the client can close the
			// connection...)
			if (_server == null) {

				// Send a TEARDOWN message to close the connection
				logger.info("Teardown Button pressed !");
				// System.out.println("Teardown Button pressed !");

				// increase RTSP sequence number
				// ..........
				RTSPSeqNb++;

				// Send TEARDOWN message to the server
				send_RTSP_request("TEARDOWN");

				// Wait for the response
				if (parse_server_response() != 200)
					logger.warning("Invalid Server Response");
				// System.out.println("Invalid Server Response");
				else {
					// change RTSP state and print out new state
					state = INIT;
					// close the sockets
					_client.close();
					logger.info("_client is closed");
					socket.close();
					logger.info("socket is closed");
					if (RTPsocket != null) {
						RTPsocket.close();
						logger.info("RTPsocket is closed");
					}

				}
			} else {
				request_type = parse_RTSP_request(); // blocking

				if (request_type == TEARDOWN) {
					// send back response
					send_RTSP_response();

					Thread.sleep(FRAME_PERIOD * 10);

					// close sockets
					_server.close();
					socket.close();
					RTPsocket.close();

				}
			}

			// close the streams
			inStream.close();
			outStream.close();
			logger.info("connection is closed");
			// System.exit(0);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Uncaught exception", ex);
			// ex.printStackTrace();

		}
	}

	public boolean isConnected() {
		boolean val = false;
		if (socket != null && !socket.isClosed()) {
			val = true;
		}
		// System.out.println(socket.getInetAddress());
		return val;
	}

	// ------------------------
	// Handler for timer
	// ------------------------
	public void actionPerformed(ActionEvent e) {

		// // if the current image nb is less than the length of the video
		// if (imagenb < VIDEO_LENGTH) {
		// // update current imagenb
		// imagenb++;
		//
		// try {
		// // get next frame to send from the video, as well as its
		// // size
		// int image_length = buf.length; // video.getnextframe(buf);
		//
		// // Builds an RTPpacket object containing the frame
		// RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb,
		// imagenb * FRAME_PERIOD, buf, image_length);
		//
		// // get to total length of the full rtp packet to send
		// int packet_length = rtp_packet.getlength();
		//
		// // retrieve the packet bitstream and store it in an array of
		// // bytes
		// byte[] packet_bits = new byte[packet_length];
		// rtp_packet.getpacket(packet_bits);
		//
		// // send the packet as a DatagramPacket over the UDP socket
		// senddp = new DatagramPacket(packet_bits, packet_length,
		// ClientIPAddr, RTP_dest_port);
		// RTPsocket.send(senddp);
		//					 
		// // System.out.println("Send frame #"+imagenb);
		// // print the header bitstream
		// rtp_packet.printheader();
		//
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// System.exit(0);
		// }
		// } else {
		// // if we have reached the end of the video file, stop the timer
		// timer.stop();
		// }
	}

	// ------------------------------------
	// Parse RTSP Request
	// ------------------------------------
	private int parse_RTSP_request() {
		int request_type = -1;
		try {
			// parse request line and extract the request_type:
			String RequestLine = RTSPBufferedReader.readLine();
			logger.info("RTSP Server - Received from Client:" + RequestLine);
			// System.out.println("RTSP Server - Received from Client:");
			// System.out.println(RequestLine);

			StringTokenizer tokens = new StringTokenizer(RequestLine);
			String request_type_string = tokens.nextToken();

			// convert to request_type structure:
			if ((new String(request_type_string)).compareTo("SETUP") == 0)
				request_type = SETUP;
			else if ((new String(request_type_string)).compareTo("PLAY") == 0)
				request_type = PLAY;
			else if ((new String(request_type_string)).compareTo("PAUSE") == 0)
				request_type = PAUSE;
			else if ((new String(request_type_string)).compareTo("TEARDOWN") == 0)
				request_type = TEARDOWN;

			if (request_type == SETUP) {
				// extract VideoFileName from RequestLine
				VideoFileName = tokens.nextToken();
			}

			// parse the SeqNumLine and extract CSeq field
			String SeqNumLine = RTSPBufferedReader.readLine();
			// System.out.println(SeqNumLine);
			logger.finest(SeqNumLine);
			tokens = new StringTokenizer(SeqNumLine);
			tokens.nextToken();
			RTSPSeqNb = Integer.parseInt(tokens.nextToken());

			// get LastLine
			String LastLine = RTSPBufferedReader.readLine();
			logger.finest(LastLine);
			// System.out.println(LastLine);

			if (request_type == SETUP) {
				// extract RTP_dest_port from LastLine
				tokens = new StringTokenizer(LastLine);
				for (int i = 0; i < 3; i++)
					tokens.nextToken(); // skip unused stuff
				RTP_dest_port = Integer.parseInt(tokens.nextToken());
			}
			// else LastLine will be the SessionId line ... do not check for
			// now.
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Uncaught exception", ex);
			// System.out.println("Exception caught: " + ex);
			// ex.printStackTrace();
			// System.exit(0);
		}
		return (request_type);
	}

	// ------------------------------------
	// Send RTSP Response
	// ------------------------------------
	private void send_RTSP_response() {
		try {
			RTSPBufferedWriter.write("RTSP/1.0 200 OK" + CRLF);
			RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
			RTSPBufferedWriter.write("Session: " + RTSP_ID + CRLF);
			RTSPBufferedWriter.flush();
			logger.info("RTSP Server - Sent response to Client.");
			// System.out.println("RTSP Server - Sent response to Client.");
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Uncaught exception", ex);
			// System.out.println("Exception caught: " + ex);
			// ex.printStackTrace();
			// System.exit(0);
		}
	}

	// ------------------------------------
	// Handler for timer
	// ------------------------------------

	class timerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// // Construct a DatagramPacket to receive data from the UDP socket
			// rcvdp = new DatagramPacket(buf, buf.length);
			//
			// try {
			// // receive the DP from the socket:
			// RTPsocket.receive(rcvdp);
			//
			// // create an RTPpacket object from the DP
			// RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp
			// .getLength());
			//
			// // print important header fields of the RTP packet received:
			// System.out.println("Got RTP packet with SeqNum # "
			// + rtp_packet.getsequencenumber() + " TimeStamp "
			// + rtp_packet.gettimestamp() + " ms, of type "
			// + rtp_packet.getpayloadtype());
			//
			// // print header bitstream:
			// rtp_packet.printheader();
			//
			// // get the payload bitstream from the RTPpacket object
			// int payload_length = rtp_packet.getpayload_length();
			// byte[] payload = new byte[payload_length];
			// rtp_packet.getpayload(payload);
			//
			// // Set the internal buffer to the received payload
			// buf = payload;
			// } catch (InterruptedIOException iioe) {
			// // System.out.println("Nothing to read");
			// } catch (IOException ioe) {
			// System.out.println("Exception caught: " + ioe);
			// }
		}
	}

	// ------------------------------------
	// Parse Server Response
	// ------------------------------------
	private int parse_server_response() {
		int reply_code = 0;

		try {
			// parse status line and extract the reply_code:
			String StatusLine = RTSPBufferedReader.readLine();
			logger.info("RTSP Client - Received from Server:" + StatusLine);
			// System.out.println("RTSP Client - Received from Server:");
			// System.out.println(StatusLine);

			StringTokenizer tokens = new StringTokenizer(StatusLine);
			tokens.nextToken(); // skip over the RTSP version
			reply_code = Integer.parseInt(tokens.nextToken());

			// if reply code is OK get and print the 2 other lines
			if (reply_code == 200) {
				String SeqNumLine = RTSPBufferedReader.readLine();
				logger.finest(SeqNumLine);
				// System.out.println(SeqNumLine);

				String SessionLine = RTSPBufferedReader.readLine();
				logger.finest(SessionLine);
				// System.out.println(SessionLine);

				// if state == INIT gets the Session Id from the SessionLine
				tokens = new StringTokenizer(SessionLine);
				tokens.nextToken(); // skip over the Session:
				RTSPid = Integer.parseInt(tokens.nextToken());
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Uncaught exception", ex);
			// System.out.println("Exception caught: " + ex);
			ex.printStackTrace();
			// System.exit(0);
		}

		return (reply_code);
	}

	// ------------------------------------
	// Send RTSP Request
	// ------------------------------------

	// .............
	// TO COMPLETE
	// .............

	private void send_RTSP_request(String request_type) {
		try {
			// Use the RTSPBufferedWriter to write to the RTSP socket
			// convert to request_type structure:
			if ((new String(request_type)).compareTo("SETUP") == 0) {
				RTSPBufferedWriter.write("SETUP movie.Mjpeg RTSP/1.0" + CRLF);
				RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
				RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= "
						+ RTP_RCV_PORT + CRLF);
			} else {
				RTSPBufferedWriter.write(request_type + " movie.Mjpeg RTSP/1.0"
						+ CRLF);
				RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
				RTSPBufferedWriter.write("Session: " + RTSP_ID + CRLF);
			}

			RTSPBufferedWriter.flush();
		} catch (Exception ex) {
			// System.out.println("Exception caught: " + ex);
			logger.log(Level.SEVERE, "Uncaught exception", ex);
			// ex.printStackTrace();
			// System.exit(0);
		}
	}
}

// class RTPpacket
class RTPpacket {

	// size of the RTP header:
	static int HEADER_SIZE = 12;

	// Fields that compose the RTP header
	public int Version;

	public int Padding;

	public int Extension;

	public int CC;

	public int Marker;

	public int PayloadType;

	public int SequenceNumber;

	public int TimeStamp;

	public int Ssrc;

	// Bitstream of the RTP header
	public byte[] header;

	// size of the RTP payload
	public int payload_size;

	// Bitstream of the RTP payload
	public byte[] payload;

	// --------------------------
	// Constructor of an RTPpacket object from header fields and payload
	// bitstream
	// --------------------------
	public RTPpacket(int PType, int Framenb, int Time, byte[] data,
			int data_length) {
		// fill by default header fields:
		Version = 2;
		Padding = 0;
		Extension = 0;
		CC = 0;
		Marker = 0;
		Ssrc = 0;

		// fill changing header fields:
		SequenceNumber = Framenb;
		TimeStamp = Time;
		PayloadType = PType;

		// build the header bistream:
		// --------------------------
		header = new byte[HEADER_SIZE];

		// .............
		// TO COMPLETE
		// .............
		// fill the header array of byte with RTP header fields

		// set V,P,X,CC,M = 10000000
		header[0] = (byte) 256;
		header[1] = intToByteArray(PayloadType)[3];

		byte[] framenb = intToByteArray(SequenceNumber);
		header[2] = framenb[2];
		header[3] = framenb[3];

		byte[] time = intToByteArray(TimeStamp);
		header[4] = time[0];
		header[5] = time[1];
		header[6] = time[2];
		header[7] = time[3];

		byte[] serverID = intToByteArray(134454536);
		header[8] = serverID[0];
		header[9] = serverID[1];
		header[10] = serverID[2];
		header[11] = serverID[3];

		// fill the payload bitstream:
		// --------------------------
		payload_size = data_length;
		payload = new byte[data_length];

		// fill payload array of byte from data (given in parameter of the
		// constructor)
		// ......

		for (int i = 0; i < data_length; i++) {
			payload[i] = data[i];
		}

		// payload = data;

		// ! Do not forget to uncomment method printheader() below !

	}

	// --------------------------
	// Constructor of an RTPpacket object from the packet bistream
	// --------------------------
	public RTPpacket(byte[] packet, int packet_size) {
		// fill default fields:
		Version = 2;
		Padding = 0;
		Extension = 0;
		CC = 0;
		Marker = 0;
		Ssrc = 0;

		// check if total packet size is lower than the header size
		if (packet_size >= HEADER_SIZE) {
			// get the header bitsream:
			header = new byte[HEADER_SIZE];
			for (int i = 0; i < HEADER_SIZE; i++)
				header[i] = packet[i];

			// get the payload bitstream:
			payload_size = packet_size - HEADER_SIZE;
			payload = new byte[payload_size];
			for (int i = HEADER_SIZE; i < packet_size; i++)
				payload[i - HEADER_SIZE] = packet[i];

			// interpret the changing fields of the header:
			PayloadType = header[1] & 127;
			SequenceNumber = (int) header[3] + 256 * (int) (header[2]);
			TimeStamp = (int) (header[7]) + 256 * (int) (header[6]) + 65536
					* (int) (header[5]) + 16777216 * (int) (header[4]);
		}
	}

	// --------------------------
	// getpayload: return the payload bistream of the RTPpacket and its size
	// --------------------------
	public int getpayload(byte[] data) {

		for (int i = 0; i < payload_size; i++)
			data[i] = payload[i];

		return (payload_size);
	}

	// --------------------------
	// getpayload_length: return the length of the payload
	// --------------------------
	public int getpayload_length() {
		return (payload_size);
	}

	// --------------------------
	// getlength: return the total length of the RTP packet
	// --------------------------
	public int getlength() {
		return (payload_size + HEADER_SIZE);
	}

	// --------------------------
	// getpacket: returns the packet bitstream and its length
	// --------------------------
	public int getpacket(byte[] packet) {
		// construct the packet = header + payload
		for (int i = 0; i < HEADER_SIZE; i++)
			packet[i] = header[i];
		for (int i = 0; i < payload_size; i++)
			packet[i + HEADER_SIZE] = payload[i];

		// return total size of the packet
		return (payload_size + HEADER_SIZE);
	}

	// --------------------------
	// gettimestamp
	// --------------------------

	public int gettimestamp() {
		return (TimeStamp);
	}

	// --------------------------
	// getsequencenumber
	// --------------------------
	public int getsequencenumber() {
		return (SequenceNumber);
	}

	// --------------------------
	// getpayloadtype
	// --------------------------
	public int getpayloadtype() {
		return (PayloadType);
	}

	// --------------------------
	// print headers without the SSRC
	// --------------------------
	// public void printheader() {
	// // TO DO: uncomment
	//
	// for (int i = 0; i < (HEADER_SIZE - 4); i++) {
	// for (int j = 7; j >= 0; j--) {
	// // if (1<=0)
	// // return(nb);
	// // else
	// // return(256+nb);
	// }
	// }
	// }

	// --------------------------
	// binarize
	// --------------------------
	/*
	 * public byte[] binarize(int num,int digits){ byte[] binary = new
	 * byte[digits]; int i = 0; while (num != 0){ binary[digits-1-i] = (byte)
	 * (num/Math.pow(2, i)%2); num = num/2; i++; } return binary; }
	 * 
	 * public byte toByte(byte[] bits){ byte b = 0; for (int i=bits.length-1;
	 * i>=0; i--){ if(bits[i] == 1){ b+=Math.pow(2, i); } } return b; }
	 */
	private byte[] intToByteArray(int integer) {
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(integer);
			dos.flush();
			bos.close();
			dos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bos.toByteArray();
	}
}

/*
 * ------------------ Client usage: java Client [Server hostname] [Server RTSP
 * listening port] [Video file requested] ----------------------
 */

// class Client {
//
// // ------------------------------------
// // main
// // ------------------------------------
// public static void main(String argv[]) throws Exception {
// // Create a Client object
// Client theClient = new Client();
//
// // get server RTSP port and IP address from the command line
// // ------------------
// int RTSP_server_port = Integer.parseInt(argv[1]);
// String ServerHost = argv[0];
// InetAddress ServerIPAddr = InetAddress.getByName(ServerHost);
//
// // get video filename to request:
// VideoFileName = argv[2];
//
// // Establish a TCP connection with the server to exchange RTSP messages
// // ------------------
//		
// }
//
// // ------------------------------------
// // Handler for buttons
// // ------------------------------------
//
// // .............
// // TO COMPLETE
// // .............
//
// // Handler for Setup button
// // -----------------------
// class setupButtonListener implements ActionListener {
// public void actionPerformed(ActionEvent e) {
//
//			
// }
//
// // Handler for Play button
// // -----------------------
// class playButtonListener implements ActionListener {
// public void actionPerformed(ActionEvent e) {
//
//			
// }
// }
//
// // Handler for Pause button
// // -----------------------
// class pauseButtonListener implements ActionListener {
// public void actionPerformed(ActionEvent e) {
//
// // System.out.println("Pause Button pressed !");
//
// if (state == PLAYING) {
// // increase RTSP sequence number
// // ........
//
// // Send PAUSE message to the server
// send_RTSP_request("PAUSE");
//
// // Wait for the response
// if (parse_server_response() != 200)
// System.out.println("Invalid Server Response");
// else {
// // change RTSP state and print out new state
// // ........
// // System.out.println("New RTSP state: ...");
//
// // stop the timer
// timer.stop();
// }
// }
// // else if state != PLAYING then do nothing
// }
// }
//
// // Handler for Teardown button
// // -----------------------
// class tearButtonListener implements ActionListener {
// public void actionPerformed(ActionEvent e) {
//
//			
// }
//
//
//
// }// end of Class Client
