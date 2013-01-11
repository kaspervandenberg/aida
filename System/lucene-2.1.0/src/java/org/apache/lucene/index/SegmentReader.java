package org.apache.lucene.index;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BitVector;

import java.io.IOException;
import java.util.*;

/**
 * @version $Id: SegmentReader.java 496851 2007-01-16 20:24:52Z mikemccand $
 */
class SegmentReader extends IndexReader {
  private String segment;
  private SegmentInfo si;

  FieldInfos fieldInfos;
  private FieldsReader fieldsReader;

  TermInfosReader tis;
  TermVectorsReader termVectorsReaderOrig = null;
  ThreadLocal termVectorsLocal = new ThreadLocal();

  BitVector deletedDocs = null;
  private boolean deletedDocsDirty = false;
  private boolean normsDirty = false;
  private boolean undeleteAll = false;

  private boolean rollbackDeletedDocsDirty = false;
  private boolean rollbackNormsDirty = false;
  private boolean rollbackUndeleteAll = false;

  IndexInput freqStream;
  IndexInput proxStream;

  // Compound File Reader when based on a compound file segment
  CompoundFileReader cfsReader = null;

  private class Norm {
    public Norm(IndexInput in, int number, long normSeek)
    {
      this.in = in;
      this.number = number;
      this.normSeek = normSeek;
    }

    private IndexInput in;
    private byte[] bytes;
    private boolean dirty;
    private int number;
    private long normSeek;
    private boolean rollbackDirty;

    private void reWrite(SegmentInfo si) throws IOException {
      // NOTE: norms are re-written in regular directory, not cfs

      String oldFileName = si.getNormFileName(this.number);
      if (oldFileName != null && !oldFileName.endsWith("." + IndexFileNames.NORMS_EXTENSION)) {
        // Mark this file for deletion.  Note that we don't
        // actually try to delete it until the new segments files is
        // successfully written:
        deleter.addPendingFile(oldFileName);
      }

      si.advanceNormGen(this.number);
      IndexOutput out = directory().createOutput(si.getNormFileName(this.number));
      try {
        out.writeBytes(bytes, maxDoc());
      } finally {
        out.close();
      }
      this.dirty = false;
    }
  }

  private Hashtable norms = new Hashtable();

  /** The class which implements SegmentReader. */
  private static Class IMPL;
  static {
    try {
      String name =
        System.getProperty("org.apache.lucene.SegmentReader.class",
                           SegmentReader.class.getName());
      IMPL = Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("cannot load SegmentReader class: " + e, e);
    } catch (SecurityException se) {
      try {
        IMPL = Class.forName(SegmentReader.class.getName());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("cannot load default SegmentReader class: " + e, e);
      }
    }
  }

  protected SegmentReader() { super(null); }

  public static SegmentReader get(SegmentInfo si) throws IOException {
    return get(si.dir, si, null, false, false);
  }

  public static SegmentReader get(SegmentInfos sis, SegmentInfo si,
                                  boolean closeDir) throws IOException {
    return get(si.dir, si, sis, closeDir, true);
  }

  public static SegmentReader get(Directory dir, SegmentInfo si,
                                  SegmentInfos sis,
                                  boolean closeDir, boolean ownDir)
    throws IOException {
    SegmentReader instance;
    try {
      instance = (SegmentReader)IMPL.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("cannot load SegmentReader class: " + e, e);
    }
    instance.init(dir, sis, closeDir, ownDir);
    instance.initialize(si);
    return instance;
  }

  private void initialize(SegmentInfo si) throws IOException {
    segment = si.name;
    this.si = si;

    boolean success = false;

    try {
      // Use compound file directory for some files, if it exists
      Directory cfsDir = directory();
      if (si.getUseCompoundFile()) {
        cfsReader = new CompoundFileReader(directory(), segment + ".cfs");
        cfsDir = cfsReader;
      }

      // No compound file exists - use the multi-file format
      fieldInfos = new FieldInfos(cfsDir, segment + ".fnm");
      fieldsReader = new FieldsReader(cfsDir, segment, fieldInfos);

      // Verify two sources of "maxDoc" agree:
      if (fieldsReader.size() != si.docCount) {
        throw new IllegalStateException("doc counts differ for segment " + si.name + ": fieldsReader shows " + fieldsReader.size() + " but segmentInfo shows " + si.docCount);
      }

      tis = new TermInfosReader(cfsDir, segment, fieldInfos);
      
      // NOTE: the bitvector is stored using the regular directory, not cfs
      if (hasDeletions(si)) {
        deletedDocs = new BitVector(directory(), si.getDelFileName());

        // Verify # deletes does not exceed maxDoc for this segment:
        if (deletedDocs.count() > maxDoc()) {
          throw new IllegalStateException("number of deletes (" + deletedDocs.count() + ") exceeds max doc (" + maxDoc() + ") for segment " + si.name);
        }
      }

      // make sure that all index files have been read or are kept open
      // so that if an index update removes them we'll still have them
      freqStream = cfsDir.openInput(segment + ".frq");
      proxStream = cfsDir.openInput(segment + ".prx");
      openNorms(cfsDir);

      if (fieldInfos.hasVectors()) { // open term vector files only as needed
        termVectorsReaderOrig = new TermVectorsReader(cfsDir, segment, fieldInfos);
      }
      success = true;
    } finally {

      // With lock-less commits, it's entirely possible (and
      // fine) to hit a FileNotFound exception above.  In
      // this case, we want to explicitly close any subset
      // of things that were opened so that we don't have to
      // wait for a GC to do so.
      if (!success) {
        doClose();
      }
    }
  }

  protected void doCommit() throws IOException {
    if (deletedDocsDirty) {               // re-write deleted
      String oldDelFileName = si.getDelFileName();
      if (oldDelFileName != null) {
        // Mark this file for deletion.  Note that we don't
        // actually try to delete it until the new segments files is
        // successfully written:
        deleter.addPendingFile(oldDelFileName);
      }

      si.advanceDelGen();

      // We can write directly to the actual name (vs to a
      // .tmp & renaming it) because the file is not live
      // until segments file is written:
      deletedDocs.write(directory(), si.getDelFileName());
    }
    if (undeleteAll && si.hasDeletions()) {
      String oldDelFileName = si.getDelFileName();
      if (oldDelFileName != null) {
        // Mark this file for deletion.  Note that we don't
        // actually try to delete it until the new segments files is
        // successfully written:
        deleter.addPendingFile(oldDelFileName);
      }
      si.clearDelGen();
    }
    if (normsDirty) {               // re-write norms
      si.setNumFields(fieldInfos.size());
      Enumeration values = norms.elements();
      while (values.hasMoreElements()) {
        Norm norm = (Norm) values.nextElement();
        if (norm.dirty) {
          norm.reWrite(si);
        }
      }
    }
    deletedDocsDirty = false;
    normsDirty = false;
    undeleteAll = false;
  }

  protected void doClose() throws IOException {
    if (fieldsReader != null) {
      fieldsReader.close();
    }
    if (tis != null) {
      tis.close();
    }

    if (freqStream != null)
      freqStream.close();
    if (proxStream != null)
      proxStream.close();

    closeNorms();

    if (termVectorsReaderOrig != null)
      termVectorsReaderOrig.close();

    if (cfsReader != null)
      cfsReader.close();
  }

  static boolean hasDeletions(SegmentInfo si) throws IOException {
    return si.hasDeletions();
  }

  public boolean hasDeletions() {
    return deletedDocs != null;
  }

  static boolean usesCompoundFile(SegmentInfo si) throws IOException {
    return si.getUseCompoundFile();
  }

  static boolean hasSeparateNorms(SegmentInfo si) throws IOException {
    return si.hasSeparateNorms();
  }

  protected void doDelete(int docNum) {
    if (deletedDocs == null)
      deletedDocs = new BitVector(maxDoc());
    deletedDocsDirty = true;
    undeleteAll = false;
    deletedDocs.set(docNum);
  }

  protected void doUndeleteAll() {
      deletedDocs = null;
      deletedDocsDirty = false;
      undeleteAll = true;
  }

  Vector files() throws IOException {
    Vector files = new Vector(16);

    if (si.getUseCompoundFile()) {
      String name = segment + ".cfs";
      if (directory().fileExists(name)) {
        files.addElement(name);
      }
    } else {
      for (int i = 0; i < IndexFileNames.INDEX_EXTENSIONS.length; i++) {
        String name = segment + "." + IndexFileNames.INDEX_EXTENSIONS[i];
        if (directory().fileExists(name))
          files.addElement(name);
      }
    }

    if (si.hasDeletions()) {
      files.addElement(si.getDelFileName());
    }

    boolean addedNrm = false;
    for (int i = 0; i < fieldInfos.size(); i++) {
      String name = si.getNormFileName(i);
      if (name != null && directory().fileExists(name)) {
        if (name.endsWith("." + IndexFileNames.NORMS_EXTENSION)) {
          if (addedNrm) continue; // add .nrm just once
          addedNrm = true;
        }
        files.addElement(name);
      }
    }
    return files;
  }

  public TermEnum terms() {
    return tis.terms();
  }

  public TermEnum terms(Term t) throws IOException {
    return tis.terms(t);
  }

  public synchronized Document document(int n, FieldSelector fieldSelector) throws IOException {
    if (isDeleted(n))
      throw new IllegalArgumentException
              ("attempt to access a deleted document");
    return fieldsReader.doc(n, fieldSelector);
  }

  public synchronized boolean isDeleted(int n) {
    return (deletedDocs != null && deletedDocs.get(n));
  }

  public TermDocs termDocs() throws IOException {
    return new SegmentTermDocs(this);
  }

  public TermPositions termPositions() throws IOException {
    return new SegmentTermPositions(this);
  }

  public int docFreq(Term t) throws IOException {
    TermInfo ti = tis.get(t);
    if (ti != null)
      return ti.docFreq;
    else
      return 0;
  }

  public int numDocs() {
    int n = maxDoc();
    if (deletedDocs != null)
      n -= deletedDocs.count();
    return n;
  }

  public int maxDoc() {
    return si.docCount;
  }

  /**
   * @see IndexReader#getFieldNames(IndexReader.FieldOption fldOption)
   */
  public Collection getFieldNames(IndexReader.FieldOption fieldOption) {

    Set fieldSet = new HashSet();
    for (int i = 0; i < fieldInfos.size(); i++) {
      FieldInfo fi = fieldInfos.fieldInfo(i);
      if (fieldOption == IndexReader.FieldOption.ALL) {
        fieldSet.add(fi.name);
      }
      else if (!fi.isIndexed && fieldOption == IndexReader.FieldOption.UNINDEXED) {
        fieldSet.add(fi.name);
      }
      else if (fi.isIndexed && fieldOption == IndexReader.FieldOption.INDEXED) {
        fieldSet.add(fi.name);
      }
      else if (fi.isIndexed && fi.storeTermVector == false && fieldOption == IndexReader.FieldOption.INDEXED_NO_TERMVECTOR) {
        fieldSet.add(fi.name);
      }
      else if (fi.storeTermVector == true &&
               fi.storePositionWithTermVector == false &&
               fi.storeOffsetWithTermVector == false &&
               fieldOption == IndexReader.FieldOption.TERMVECTOR) {
        fieldSet.add(fi.name);
      }
      else if (fi.isIndexed && fi.storeTermVector && fieldOption == IndexReader.FieldOption.INDEXED_WITH_TERMVECTOR) {
        fieldSet.add(fi.name);
      }
      else if (fi.storePositionWithTermVector && fi.storeOffsetWithTermVector == false && fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_POSITION) {
        fieldSet.add(fi.name);
      }
      else if (fi.storeOffsetWithTermVector && fi.storePositionWithTermVector == false && fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_OFFSET) {
        fieldSet.add(fi.name);
      }
      else if ((fi.storeOffsetWithTermVector && fi.storePositionWithTermVector) &&
                fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_POSITION_OFFSET) {
        fieldSet.add(fi.name);
      }
    }
    return fieldSet;
  }


  public synchronized boolean hasNorms(String field) {
    return norms.containsKey(field);
  }

  static byte[] createFakeNorms(int size) {
    byte[] ones = new byte[size];
    Arrays.fill(ones, DefaultSimilarity.encodeNorm(1.0f));
    return ones;
  }

  private byte[] ones;
  private byte[] fakeNorms() {
    if (ones==null) ones=createFakeNorms(maxDoc());
    return ones;
  }

  // can return null if norms aren't stored
  protected synchronized byte[] getNorms(String field) throws IOException {
    Norm norm = (Norm) norms.get(field);
    if (norm == null) return null;  // not indexed, or norms not stored
    if (norm.bytes == null) {                     // value not yet read
      byte[] bytes = new byte[maxDoc()];
      norms(field, bytes, 0);
      norm.bytes = bytes;                         // cache it
    }
    return norm.bytes;
  }

  // returns fake norms if norms aren't available
  public synchronized byte[] norms(String field) throws IOException {
    byte[] bytes = getNorms(field);
    if (bytes==null) bytes=fakeNorms();
    return bytes;
  }

  protected void doSetNorm(int doc, String field, byte value)
          throws IOException {
    Norm norm = (Norm) norms.get(field);
    if (norm == null)                             // not an indexed field
      return;
    norm.dirty = true;                            // mark it dirty
    normsDirty = true;

    norms(field)[doc] = value;                    // set the value
  }

  /** Read norms into a pre-allocated array. */
  public synchronized void norms(String field, byte[] bytes, int offset)
    throws IOException {

    Norm norm = (Norm) norms.get(field);
    if (norm == null) {
      System.arraycopy(fakeNorms(), 0, bytes, offset, maxDoc());
      return;
    }

    if (norm.bytes != null) {                     // can copy from cache
      System.arraycopy(norm.bytes, 0, bytes, offset, maxDoc());
      return;
    }

    IndexInput normStream = (IndexInput) norm.in.clone();
    try {                                         // read from disk
      normStream.seek(norm.normSeek);
      normStream.readBytes(bytes, offset, maxDoc());
    } finally {
      normStream.close();
    }
  }


  private void openNorms(Directory cfsDir) throws IOException {
    long nextNormSeek = SegmentMerger.NORMS_HEADER.length; //skip header (header unused for now)
    int maxDoc = maxDoc();
    for (int i = 0; i < fieldInfos.size(); i++) {
      FieldInfo fi = fieldInfos.fieldInfo(i);
      if (fi.isIndexed && !fi.omitNorms) {
        Directory d = directory();
        String fileName = si.getNormFileName(fi.number);
        if (!si.hasSeparateNorms(fi.number)) {
          d = cfsDir;
        }
        long normSeek = (fileName.endsWith("." + IndexFileNames.NORMS_EXTENSION) ? nextNormSeek : 0);
        norms.put(fi.name, new Norm(d.openInput(fileName), fi.number, normSeek));
        nextNormSeek += maxDoc; // increment also if some norms are separate
      }
    }
  }

  private void closeNorms() throws IOException {
    synchronized (norms) {
      Enumeration enumerator = norms.elements();
      while (enumerator.hasMoreElements()) {
        Norm norm = (Norm) enumerator.nextElement();
        norm.in.close();
      }
    }
  }
  
  /**
   * Create a clone from the initial TermVectorsReader and store it in the ThreadLocal.
   * @return TermVectorsReader
   */
  private TermVectorsReader getTermVectorsReader() {
    TermVectorsReader tvReader = (TermVectorsReader)termVectorsLocal.get();
    if (tvReader == null) {
      tvReader = (TermVectorsReader)termVectorsReaderOrig.clone();
      termVectorsLocal.set(tvReader);
    }
    return tvReader;
  }
  
  /** Return a term frequency vector for the specified document and field. The
   *  vector returned contains term numbers and frequencies for all terms in
   *  the specified field of this document, if the field had storeTermVector
   *  flag set.  If the flag was not set, the method returns null.
   * @throws IOException
   */
  public TermFreqVector getTermFreqVector(int docNumber, String field) throws IOException {
    // Check if this field is invalid or has no stored term vector
    FieldInfo fi = fieldInfos.fieldInfo(field);
    if (fi == null || !fi.storeTermVector || termVectorsReaderOrig == null) 
      return null;
    
    TermVectorsReader termVectorsReader = getTermVectorsReader();
    if (termVectorsReader == null)
      return null;
    
    return termVectorsReader.get(docNumber, field);
  }


  /** Return an array of term frequency vectors for the specified document.
   *  The array contains a vector for each vectorized field in the document.
   *  Each vector vector contains term numbers and frequencies for all terms
   *  in a given vectorized field.
   *  If no such fields existed, the method returns null.
   * @throws IOException
   */
  public TermFreqVector[] getTermFreqVectors(int docNumber) throws IOException {
    if (termVectorsReaderOrig == null)
      return null;
    
    TermVectorsReader termVectorsReader = getTermVectorsReader();
    if (termVectorsReader == null)
      return null;
    
    return termVectorsReader.get(docNumber);
  }

  /**
   * Return the name of the segment this reader is reading.
   */
  String getSegmentName() {
    return segment;
  }

  void setSegmentInfo(SegmentInfo info) {
    si = info;
  }

  void startCommit() {
    super.startCommit();
    rollbackDeletedDocsDirty = deletedDocsDirty;
    rollbackNormsDirty = normsDirty;
    rollbackUndeleteAll = undeleteAll;
    Enumeration values = norms.elements();
    while (values.hasMoreElements()) {
      Norm norm = (Norm) values.nextElement();
      norm.rollbackDirty = norm.dirty;
    }
  }

  void rollbackCommit() {
    super.rollbackCommit();
    deletedDocsDirty = rollbackDeletedDocsDirty;
    normsDirty = rollbackNormsDirty;
    undeleteAll = rollbackUndeleteAll;
    Enumeration values = norms.elements();
    while (values.hasMoreElements()) {
      Norm norm = (Norm) values.nextElement();
      norm.dirty = norm.rollbackDirty;
    }
  }
}
