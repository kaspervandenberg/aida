package nl.uva.science.wsdtf.utilities;

import java.util.logging.Level;

/**
 * A class holding all the constants.
 * @author S. Koulouzis
 */
public class Constants {
	public static final int Kbyte = 1024;
	public static final double MB = Kbyte * 1024.0;
	public static final double GB = MB * 1024.0;
	/**
	 * Push the limit of UDP packets that is set to 65,507 bytes, when using diferent protocols you'r pushing the systems memory
	 */
        public static int MAX_BUFFER_SIZE=Constants.Kbyte*63;
	
	public static final int MIDLEMAN = -1;
	public static final int PRODUCE = 0;
	public static final int CONSUME = 1;
        
	public static final int TCP = 2;
	public static final int UDP = 3;
	public static final int STYX = 4;
	public static final int RTSP = 5;
	public static final int GridFTP = 6;
        public static final int HTTP = 7;
	//More protocols...
		
	public static int CONNECTION_COUNT=1;
	public static Level LOG_LEVEL = Level.WARNING;
        public static boolean saveLogs=true;
        public static boolean displaySpeed=false;
//	public static Level CONSOLE_LEVEL = Level.ALL;
	public static String logPath;
        
        public static final String[] CONTROL_CODES = {"BACKEND_EOD","NEW_STREAM"};
        public static final byte[] END_CODE = CONTROL_CODES[0].getBytes();
        public static final byte[] NEW_STREAM_CODE = CONTROL_CODES[1].getBytes();
        
        public static final String[] PRIMITIVES = {
        "java.lang.Boolean",
        "java.lang.Byte",
        "java.lang.Character",
        "java.lang.Double",
        "java.lang.Float",
        "java.lang.Integer",
        "java.lang.Long",
        "java.lang.Short",
        "java.lang.String",        
        "[Z",
        "[B",
        "[C",
        "[D",
        "[F",
        "[I",
        "[J",
        "[S",
        "[L"
        };
                
        public static final String BOOLEAN = PRIMITIVES[0];
        public static final String BYTE = PRIMITIVES[1];
        public static final String CHARACTER = PRIMITIVES[2];
        public static final String DOUBLE = PRIMITIVES[3];
        public static final String FLOAT = PRIMITIVES[4];
        public static final String INTEGER = PRIMITIVES[5];
        public static final String LONG = PRIMITIVES[6];
        public static final String SHORT = PRIMITIVES[7];
        public static final String STRING = PRIMITIVES[8];               
        public static final String BOOLEAN_ARRAY = PRIMITIVES[9]; 
        public static final String BYTE_ARRAY = PRIMITIVES[10];
        public static final String CHARACTER_ARRAY = PRIMITIVES[11];
        public static final String DOUBLE_ARRAY = PRIMITIVES[12];
        public static final String FLOAT_ARRAY = PRIMITIVES[13];
        public static final String INTEGER_ARRAY = PRIMITIVES[14];
        public static final String LONG_ARRAY = PRIMITIVES[15];
        public static final String SHORT_ARRAY = PRIMITIVES[16];
        public static final String STRING_ARRAY = PRIMITIVES[17];
		
        /**
         * Sets the max buffer size of a connection
         * @param size the size of the buffer
         */
	public static void setMaxBufferSize(int size){
            int index=-1;
            for(int i=0;i<CONTROL_CODES.length;i++){
                if( size < CONTROL_CODES[i].length() ){
                    index = i;
                    break;
                }
            }
            if(index!=-1){
                System.err.println("Buffer size of "+ size +"  bytes is not alowd smalest alowed value is "+CONTROL_CODES[index].length()+" seting buffer size to "+CONTROL_CODES[index].length()+" bytes");
                MAX_BUFFER_SIZE = CONTROL_CODES[index].length();
            }else{
                MAX_BUFFER_SIZE = size;
            }

	}
        
	/**
         * Not used. Indented for multithreaded connections, on multiple ports 
         * @param count
         */
	public static void setThreadCont(int count){
		CONNECTION_COUNT = count;
	}	
       
        /**
         * Sets the log level of all the classes 
         * @param level
         */
        public static void setLogLevel(String level){
            if(level!=null){
                LOG_LEVEL = Level.parse(level);
            }else{
                 LOG_LEVEL = Level.WARNING;
            }
        }
	
}
