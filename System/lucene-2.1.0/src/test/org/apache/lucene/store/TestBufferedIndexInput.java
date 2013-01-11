package org.apache.lucene.store;

import java.io.IOException;

import junit.framework.TestCase;

public class TestBufferedIndexInput extends TestCase {
	// Call readByte() repeatedly, past the buffer boundary, and see that it
	// is working as expected.
	// Our input comes from a dynamically generated/ "file" - see
	// MyBufferedIndexInput below.
    public void testReadByte() throws Exception {
    	MyBufferedIndexInput input = new MyBufferedIndexInput(); 
    	for(int i=0; i<BufferedIndexInput.BUFFER_SIZE*10; i++){
     		assertEquals(input.readByte(), byten(i));
    	}
    }
 
	// Call readBytes() repeatedly, with various chunk sizes (from 1 byte to
    // larger than the buffer size), and see that it returns the bytes we expect.
	// Our input comes from a dynamically generated "file" -
    // see MyBufferedIndexInput below.
    public void testReadBytes() throws Exception {
    	MyBufferedIndexInput input = new MyBufferedIndexInput();
    	int pos=0;
    	// gradually increasing size:
    	for(int size=1; size<BufferedIndexInput.BUFFER_SIZE*10; size=size+size/200+1){
    		checkReadBytes(input, size, pos);
    		pos+=size;
    	}
    	// wildly fluctuating size:
    	for(long i=0; i<1000; i++){
    		// The following function generates a fluctuating (but repeatable)
    		// size, sometimes small (<100) but sometimes large (>10000)
    		int size1 = (int)( i%7 + 7*(i%5)+ 7*5*(i%3) + 5*5*3*(i%2));
    		int size2 = (int)( i%11 + 11*(i%7)+ 11*7*(i%5) + 11*7*5*(i%3) + 11*7*5*3*(i%2) );
    		int size = (i%3==0)?size2*10:size1; 
    		checkReadBytes(input, size, pos);
    		pos+=size;
    	}
    	// constant small size (7 bytes):
    	for(int i=0; i<BufferedIndexInput.BUFFER_SIZE; i++){
    		checkReadBytes(input, 7, pos);
    		pos+=7;
    	}
    }
   private void checkReadBytes(BufferedIndexInput input, int size, int pos) throws IOException{
	   // Just to see that "offset" is treated properly in readBytes(), we
	   // add an arbitrary offset at the beginning of the array
	   int offset = size % 10; // arbitrary
	   byte[] b = new byte[offset+size];
	   input.readBytes(b, offset, size);
	   for(int i=0; i<size; i++){
		   assertEquals(b[offset+i], byten(pos+i));
	   }
   }
   
   // This tests that attempts to readBytes() past an EOF will fail, while
   // reads up to the EOF will succeed. The EOF is determined by the
   // BufferedIndexInput's arbitrary length() value.
   public void testEOF() throws Exception {
	   MyBufferedIndexInput input = new MyBufferedIndexInput(1024);
	   // see that we can read all the bytes at one go:
	   checkReadBytes(input, (int)input.length(), 0);  
	   // go back and see that we can't read more than that, for small and
	   // large overflows:
	   int pos = (int)input.length()-10;
	   input.seek(pos);
	   checkReadBytes(input, 10, pos);  
	   input.seek(pos);
	   try {
		   checkReadBytes(input, 11, pos);
           fail("Block read past end of file");
       } catch (IOException e) {
           /* success */
       }
	   input.seek(pos);
	   try {
		   checkReadBytes(input, 50, pos);
           fail("Block read past end of file");
       } catch (IOException e) {
           /* success */
       }
	   input.seek(pos);
	   try {
		   checkReadBytes(input, 100000, pos);
           fail("Block read past end of file");
       } catch (IOException e) {
           /* success */
       }
  }

    // byten emulates a file - byten(n) returns the n'th byte in that file.
    // MyBufferedIndexInput reads this "file".
    private static byte byten(long n){
    	return (byte)(n*n%256);
    }
    private static class MyBufferedIndexInput extends BufferedIndexInput {
    	private long pos;
    	private long len;
    	public MyBufferedIndexInput(long len){
    		this.len = len;
    		this.pos = 0;
    	}
    	public MyBufferedIndexInput(){
    		// an infinite file
    		this(Long.MAX_VALUE);
    	}
		protected void readInternal(byte[] b, int offset, int length) throws IOException {
			for(int i=offset; i<offset+length; i++)
				b[i] = byten(pos++);
		}

		protected void seekInternal(long pos) throws IOException {
			this.pos = pos;
		}

		public void close() throws IOException {
		}

		public long length() {
			return len;
		}
    }
}
