package org.vle.aid.medline;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Files;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.net.SocketException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import org.xml.sax.SAXException;


/**
 * The <code>DownloadMedline</code> command downloads a
 * complete set of XML-formatted MEDLINE citations and checksums from
 * NLM's FTP server.  It is able to download both the yearly baseline
 * files and the daily update files; they only differ in their ftp URL
 * and the format of their checksums.  It only downloads the corrupt
 * or missing files from a server and may be run daily to keep a
 * repository up to date.
 *
 * <P>If this command completes normally, all files will have been
 * downloaded with matching checksums.  If it does not complete
 * normally, it will abort the JVM and provide a non-zero return code.
 * If the command is restarted, it will take off where it leaves off.
 * The completion status is indicated on the last line of output.  If
 * the target directory is complete, the command will still download
 * the file list from the MEDLINE server and verify all of the
 * checksums in the target directory before indicating completion.
 *
 * <P>The 2005 baseline consists of 1000 files, 500 of which are
 * gzip-compressed XML representing citations and 500 of which contain
 * matching MD5 checksums as hex strings.  The total size is roughly
 * 5.6 GB.  Toward the end of the year, the update will contain
 * roughly half as many files as the baseline, but most of them will
 * be much shorter; as of October 2004, the size of the 2004 updates
 * directory was 1.3 GB. Many of the citations contained in these
 * updates are corrections for earlier citations; users are expected
 * to replace citations with those in larger numbered files if their
 * PubMed identifiers match.
 *
 * <P>The time to fully downline the MEDLINE baseline is dominated by
 * network transfer time.  Very little CPU time is required, though
 * the network will be tied up and disks kept spinning at the rate of
 * network transfers.  The Alias-i download took roughly 8 hours, at a
 * download rate of a little over 210 thousand bytes per second (200
 * KB/s).  The initial update versus the daily updates directory will
 * also take a while, depending on the fraction of the year elapsed.
 * In October, it took roughly two hours to catch up with the updates.
 * After that, daily updates will only take a few minutes because they
 * are only two files, a data file and checksum file.  Updates occur
 * Sunday through Thursday.
 *
 * <P>The updates contain notes and statistics, which are not part of
 * MEDLINE and not downloaded by this command.  The updates are also
 * available in zip format, though only the gzipped versions are
 * downloaded.  This command deals with the varying textual format of
 * the hexadecmial string-encoded checksums between the baseline and
 * updated directories.
 *
 * <P>This command is intentionally single threaded so as not to
 * monopolize or stress the NLM data servers.  It is <i>not</i> safe
 * to start multiple copies of this command, either within the same
 * virtual machine or across processes; this will cause duplicate
 * downloads, potentially corrupt results and possibly lead to
 * deadlock depending on how file access is managed across processes.
 *
 * <P>The following arguments are all required:
 *
 * <dl>
 *
 * <dt><code>-domain</code></dt>
 * <dd>Domain name from which to download the citations.  Disclosed
 * to licensees by NLM.
 * </dd>
 *
 * <dt><code>-path</code></dt>
 * <dd>Path on the domain from which to download the citations.  Disclosed
 * to licensees by NLM.
 * </dd>
 *
 * <dt><code>-user</code></dt>
 * <dd>User name assigned by NLM.
 * </dd>
 *
 * <dt><code>-password</code></dt>
 * <dd>Password assigned by NLM.
 * </dd>
 *
 * <dt><code>-targetDir</code></dt>
 * <dd>Name of directory into which citations are written.
 * </dd>
 *
 * </dl>
 *
 * <P>The following arguments are optional:
 *
 * <dl>
 *
 * <dt><code>-maxTries</code></dt>
 * <dd>Maximum number of download attempts per file.  If the number is
 * exceeded without successfully downloading the entire collection, an
 * exception will be thrown before the program exits.</dd>
 *
 * </dl>
 *
 * <P>In order to run, there must be network connectivity.  Note that
 * the completed download of gzip compressed MEDLINE requires on the
 * order of 5GB of free disk space.
 *
 * <P>For information on obtaining the MEDLINE corpus, which is
 * available free for research or commercial purposes, see:
 *
 * <blockquote>
 * <a href="http://www.nlm.nih.gov/pubs/factsheets/medline.html"
 *   >MEDLINE Fact Sheet</a>.
 * </blockquote>
 *
 * Information on obtaining MEDLINE are available from:
 *
 * <blockquote>
 * <a href="http://www.nlm.nih.gov/databases/leased.html"
 *   >Leasing MEDLINE</a>.
 * </blockquote>
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe2.2
 */
public class DownloadMedline extends AbstractCommand {

    private boolean mIsUpdate;

    private int mNumFiles = 0;
    private int mNumFilesOK = 0;

    private long mStartTime;
    private String mDomainName;
    private String mBaselinePath;
    private String mUpdatePath;
    private String mUserName;
    private String mPassword;
    private Boolean doIndex;
    private File mMedlineDir;
    private File mUpdatesTargetDir;
    private File mBaselineTargetDir;
    
    private String mIndexdir;

    private int mMaxTries;

    private FTPClient mFTPClient;

    private HashSet mExistingFileSet;
    private HashSet mToIndexFileSet;
    private String[] mExistingFileNames;
    private String[] mServerFileNames;

    // doesn't need to be public
    private DownloadMedline(String[] args) throws IOException {
        super(args,DEFAULT_PARAMS);
        mStartTime = System.currentTimeMillis();
        mDomainName = getExistingArgument(DOMAIN_NAME_PARAM);
        mBaselinePath = getArgument(BASELINE_PATH_PARAM);
        mUpdatePath = getArgument(UPDATE_PATH_PARAM);
        
        doIndex = hasFlag("index");
        
        mIndexdir = getArgument("indexdir");
        
        if (mBaselinePath == null && mUpdatePath == null) {
            String msg = "Must define one of parameters "
                + BASELINE_PATH_PARAM + " or "
                + UPDATE_PATH_PARAM;
            throw new IllegalArgumentException(msg);
        }
        mIsUpdate = (mUpdatePath != null);
        mMedlineDir = getArgumentDirectory(MEDLINE_DIR_PARAM);
        mBaselineTargetDir = new File(mMedlineDir,"baseline");
        mUpdatesTargetDir = new File(mMedlineDir,"updates");
        ensureExists(mBaselineTargetDir);
        ensureExists(mUpdatesTargetDir);
        mUserName = getExistingArgument(USER_NAME_PARAM);
        mPassword = getExistingArgument(PASSWORD_PARAM);
        mMaxTries = getArgumentInt(MAX_TRIES_PARAM);
        reportParameters();
    }

    private static void ensureExists(File dir) {
        if (dir.isDirectory()) return;
        if (dir.exists()) {
            String msg = "Existing file must be directory."
                + " Found file=" + dir;
            throw new IllegalArgumentException(msg);
        }
        dir.mkdirs();
    }

    private void run(String path, File targetDir) throws IOException {
        setFTPPath(path);
        System.out.println("Reading from server path=" + path);
        System.out.println("Writing to target directory=" + targetDir);
        mExistingFileNames = targetDir.list();
        mExistingFileSet = new HashSet(Arrays.asList(mExistingFileNames));
        mToIndexFileSet = new HashSet();
        System.out.println("Number of existing files="
                           + mExistingFileSet.size());
        System.out.println("Reading list of file names from server.");
        mServerFileNames = mFTPClient.listNames();
        checkReply("Read file names from server.");

        System.out.println("Found server file names. Number of files="
                           + mServerFileNames.length);
        System.out.println("Server files=" + java.util.Arrays.asList(mServerFileNames));
        getChecksums(targetDir);
        System.out.println("Number of citation files, based on checksums="
                           + mNumFiles);
        checkExistingFiles(targetDir);
        for (int i = 0; i < mMaxTries && mNumFiles > mNumFilesOK; ++i) {
            System.out.println();
            System.out.println("Download new files.");
            if (i > 0) System.out.println("   Attempt=" + i);
            try {
                downloadNewFiles(targetDir, doIndex);
            } catch (IOException e) {
                System.out.println("IO Exception getting files. Stack trace follows.");
                e.printStackTrace(System.out);
                exitIncomplete();
            }
        }
    }


    /**
     * Run the command.  See class documentation above for details on
     * arguments and behavior.
     */
    public void run() {
        try {
            System.out.println("Establishing FTP connection.");
            initializeFTPClient();
            if (mBaselinePath != null) {
                System.out.println();
                System.out.println("Checking/Downloading Baseline.");
                run(mBaselinePath,mBaselineTargetDir);
            }
            if (mUpdatePath != null) {
                System.out.println();
                System.out.println("Checking/Downloading Updates.");
                run(mUpdatePath,mUpdatesTargetDir);
            }
            System.out.println();
            System.out.println("Total Time (HH:MM:SS)=" + elapsedTime());
            System.out.println("MEDLINE DOWNLOAD COMPLETE.");
        } catch (Exception e) {
            System.out.println("Unexpected Exception. Stack trace follows.");
            e.printStackTrace(System.out);
            exitIncomplete();
        } finally {
            closeFTPClient();
        }
    }


    private void checkReply(String description) throws IOException {

        int replyCode = mFTPClient.getReplyCode();
        if (FTPReply.isPositiveCompletion(replyCode)) return;
        printLastReply(description);
        exitIncomplete();
    }

    private void printLastReply(String description) {
        String reply = mFTPClient.getReplyString();
        int replyCode = mFTPClient.getReplyCode();
        String msg = description
            + "\n FTP server reply code=" + replyCode
            + "\n FTP reply=" + reply;
        System.out.println(msg);
    }

    private void setFTPPath(String path) throws IOException {
        if (!mFTPClient.changeWorkingDirectory(path)) {
            printLastReply("Server error changing directory to path=" + path);
            exitIncomplete();
        }
        if (!mFTPClient.setFileType(FTP.BINARY_FILE_TYPE)) {
            printLastReply("Server error changing type to binary.");
            exitIncomplete();
        }
    }

    private static final int FIVE_MINUTES_IN_MS = 5 * 60 * 1000;

    private void initializeFTPClient() {
        try {
            mFTPClient = new FTPClient();
            mFTPClient.setDataTimeout(FIVE_MINUTES_IN_MS);
            System.out.println("  Connecting to NLM");
            mFTPClient.connect(mDomainName);
            checkReply("Connecting to server");
            System.out.println("  Connected.");
            System.out.println("  Logging in.");
            mFTPClient.login(mUserName,mPassword);
            checkReply("Login");
            System.out.println("Logged in to FTP Server.");
            mFTPClient.enterLocalPassiveMode();
        } catch (IOException e) {
            System.out.println("Exception initializing FTP Client.");
            System.out.println("Exception stack trace follows.");
            e.printStackTrace(System.out);
            exitIncomplete();
        }
    }

    public void getChecksums(File targetDir) {
        boolean foundError = true;
        for (int k = 0; foundError && (k < mMaxTries); ++k) {
            System.out.println();
            System.out.println("Downloading checksums.");
            if (k > 0) System.out.println("  Attempt=" + (k+1));
            foundError = false;
            try {
                for (int i = 0; i < mServerFileNames.length; ++i) {
                    if (mServerFileNames[i].endsWith(".gz.md5")) {
                        if (!mExistingFileSet.contains(mServerFileNames[i])) {
                            System.out.println("Downloading file=" + mServerFileNames[i]);
                            download(i,targetDir);
                        }
                        if (checkChecksum(i,targetDir)) {
                            ++mNumFiles;
                            System.out.println("  checksum ok.  num files=" + mNumFiles);
                        } else {
                            System.out.println("  checksum failed.  deleting.");
                            new File(targetDir,mServerFileNames[i]).delete();
                            foundError = true;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("I/O Exception getting checksums.  Stack trace follows.");
                e.printStackTrace(System.out);
                foundError = true;
            }
        }
        if (foundError) {
            System.out.println("Failed downloading checksums.");
            exitIncomplete();
        }
        System.out.println("Done downloading checksums.");
        System.out.println("Elapsed time=" + elapsedTime());
    }

    private void exitIncomplete() {
        closeFTPClient();
        System.out.println("Total Time (HH:MM:SS)=" + elapsedTime());
        System.out.println("MEDLINE DOWNLOAD *****NOT***** COMPLETE.");
        System.exit(1);
    }

    private void reconnectFTPClient() {
        closeFTPClient();
        initializeFTPClient();
    }

    private void closeFTPClient() {
        try {
            mFTPClient.disconnect();
        } catch (IOException e) {
            System.out.println("IOException Closing FTP Client. Stack trace follows.");
            e.printStackTrace(System.out);
        }
    }

    private void checkExistingFiles(File targetDir) throws IOException {
        System.out.println();
        System.out.println("Checking existing files.");
        for (int i = 0; i < mServerFileNames.length; ++i) {
            if (mServerFileNames[i].endsWith(".gz")) {
                if (mExistingFileSet.contains(mServerFileNames[i])) {
                    if (!verifyChecksum(i,targetDir)) {
                        new File(targetDir,mServerFileNames[i]).delete();
                        mExistingFileSet.remove(mServerFileNames[i]);
                        System.out.println("Failed checskum. File to delete/reload="
                                           + mServerFileNames[i]);
                    } else {
                        System.out.println(mServerFileNames[i] + " OK");
                       ++mNumFilesOK;
                    }
                }
            }
        }
        System.out.println("Finished existing file check."
                           + " Progress: " + mNumFilesOK + "/" + mNumFiles
                           + " Elapsed time=" + elapsedTime());
    }

    private void downloadNewFiles(File targetDir, Boolean doIndex) throws IOException {
        for (int i = 0; i < mServerFileNames.length; ++i) {
            if (mServerFileNames[i].endsWith(".gz")
                && !mExistingFileSet.contains(mServerFileNames[i])) {
                if (!downloadAndCheck(i,targetDir)) {
                    String msg = "Failed checksum. File to delete/reload="
                        + mServerFileNames[i];
                    System.out.println(msg);
                    mExistingFileSet.remove(mServerFileNames[i]);
                    File fileToDelete
                        = new File(targetDir,mServerFileNames[i]);
                    if (fileToDelete.exists()) fileToDelete.delete();
                } else {
                    ++mNumFilesOK;
                    mExistingFileSet.add(mServerFileNames[i]);
                    
                    mToIndexFileSet.add(new File(targetDir,mServerFileNames[i]).getCanonicalPath());
                    
                    String msg = mServerFileNames[i] + " OK"
                        + " Progress: " + mNumFilesOK + "/" + mNumFiles
                        + " Elapsed time=" + elapsedTime();
                    System.out.println(msg);
                }
            }
        }
        
        if (doIndex) {
          String[] files = (String[]) mToIndexFileSet.toArray(new String[0]);
          Arrays.sort(files);
          for (String file : files) {
            try {
              System.err.println(elapsedTime() + " indexing");
              IndexUpdater.main(new String[]{mIndexdir, file});
            } catch (Exception e) {
              e.printStackTrace();
              System.exit(-1);
            }
          }
        }
    }

    private void reportParameters() {
        System.out.println();
        System.out.println("Downloading MEDLINE");
        System.out.println("  Start time=" + new Date(mStartTime));
        System.out.println("  Domain=" + mDomainName);
        System.out.println("  Baseline Path on Domain=" + mBaselinePath);
        System.out.println("  Update Path on Domain=" + mUpdatePath);
        System.out.println("  MEDLINE Target Directory="
                           + getArgument(MEDLINE_DIR_PARAM));
        System.out.println("  User name=" + mUserName);
        System.out.println("  Password=" + mPassword);
        System.out.println("  Max tries=" + mMaxTries);
    }

    private String elapsedTime() {
        return Strings.msToString(System.currentTimeMillis()-mStartTime);
    }

    private boolean downloadAndCheck(int i, File targetDir)
        throws IOException {

        download(i,targetDir);
        return verifyChecksum(i,targetDir);
    }

    private void download(int i, File targetDir) {
        File targetFile = new File(targetDir,mServerFileNames[i]);
        OutputStream out = null;
        BufferedOutputStream bufOut = null;
        try {
            out = new FileOutputStream(targetFile);
            bufOut = new BufferedOutputStream(out);
            mFTPClient.retrieveFile(mServerFileNames[i],bufOut);
        } catch (SocketException e) {
            System.out.println("SocketException=" + e);
            System.out.println();
            System.out.println("Reconnecting to server.");
            reconnectFTPClient();
            printLastReply("Server reply from Retrieve file="
                           + mServerFileNames[i]);
        } catch (IOException e) {
            // includes connection closed exception
            printLastReply("Server reply from Retrieve file="
                           + mServerFileNames[i]);
            System.out.println("Download IOException. Stack trace follows.");
            e.printStackTrace(System.out);
            try {
                targetFile.delete();
            } catch (SecurityException e2) {
                System.out.println("Could not remove file=" + targetFile);
                System.out.println("Security exception. Stack trace follows.");
                e2.printStackTrace(System.out);
            }
        } finally {
            Streams.closeOutputStream(bufOut);
            Streams.closeOutputStream(out);
        }
    }

    private boolean checkChecksum(int i, File targetDir)
        throws IOException {

        File file = new File(targetDir,mServerFileNames[i]);
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            System.out.println("Checksum file not ordinary file. File=" + file);
            System.out.println("Deleting and rescheduling.");
            file.delete();
            return false;
        }
        try {
            getExpectedMD5String(file);
            return true;
        } catch (IOException e) {
            System.out.println("Checksum file corrupt. File=" + file);
            System.out.println("  Exception=" + e);
            System.out.println("  Deleting and rescheduling.");
            file.delete();
            return false;
        }
    }

    private boolean verifyChecksum(int i, File targetDir)
        throws IOException {

        File checksumFile = new File(targetDir,mServerFileNames[i]+".md5");
        File testFile = new File(targetDir,mServerFileNames[i]);
        if (!checksumFile.isFile()) {
            System.out.println("Could not find checksum file=" + checksumFile);
            System.out.println("Scheduling for retry.");
            return false;
        }
        if (!testFile.isFile()) {
            System.out.println("Could not find downloaded file=" + testFile);
            System.out.println("Scheduling for retry.");
            return false;
        }
        String expectedChecksum = getExpectedMD5String(checksumFile);
        String foundChecksum = getMD5HexString(testFile);
        boolean match = expectedChecksum.equals(foundChecksum);
        if (!match) {
            System.out.println("expected checksum=" + expectedChecksum
                               + " found checksum=" + foundChecksum);
            return false;
        }
        return true;
    }

    private String getMD5HexString(File file) throws IOException {
        byte[] md5Bytes = getMD5Bytes(file);
        return Strings.bytesToHex(md5Bytes);
    }

    private byte[] getMD5Bytes(File file) throws IOException {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Couldn't find MD5 algorithm. Exception=" + e);
        }
        InputStream fileIn = null;
        byte[] buffer = new byte[1024];
        try {
            fileIn = new FileInputStream(file);
            int numRead;
            do {
                numRead = fileIn.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            return digest.digest();
        } finally {
            Streams.closeInputStream(fileIn);
        }
    }

    private String getExpectedMD5String(File file) throws IOException {
        char[] cs = Files.readCharsFromFile(file,"ISO8859-1");
        String s = new String(cs);
        if (s.indexOf(" = ") < 0) {
            String msg = "Bad checksum file (no '='). Found=" + s;
            throw new IOException(msg);
        }
        String checksumString = s.substring(s.indexOf(" = ")+3).trim();
        if (checksumString.length() != 32) {
            String msg = "Bad checksum length. Found=" + s;
            throw new IOException(msg);
        }
        return checksumString;
    }

    private static final String DOMAIN_NAME_PARAM = "domain";
    private static final String BASELINE_PATH_PARAM = "baselinePath";
    private static final String UPDATE_PATH_PARAM = "updatePath";
    private static final String MEDLINE_DIR_PARAM = "medlineDir";
    private static final String USER_NAME_PARAM = "user";
    private static final String PASSWORD_PARAM = "password";
    private static final String TARGET_DIR_PARAM = "targetDir";
    private static final String MAX_TRIES_PARAM = "maxTries";

    private static final Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(MAX_TRIES_PARAM,"8");
    }

    /**
     * Main method to be called from the command-line.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) throws IOException {
        (new DownloadMedline(args)).run();
    }



}
