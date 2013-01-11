package org.apache.lucene.benchmark.byTask.tasks;

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

import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.QueryMaker;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;



/**
 * Read index (abstract) task.
 * Sub classes implement withSearch(), withWarm(), withTraverse() and withRetrieve()
 * methods to configure the actual action.
 * Other side effects: none.
 */
public abstract class ReadTask extends PerfTask {

  public ReadTask(PerfRunData runData) {
    super(runData);
  }

  public int doLogic() throws Exception {
    int res = 0;
    boolean closeReader = false;
    
    // open reader or use existing one
    IndexReader ir = getRunData().getIndexReader();
    if (ir == null) {
      Directory dir = getRunData().getDirectory();
      ir = IndexReader.open(dir);
      closeReader = true;
      //res++; //this is confusing, comment it out
    }
    
    // optionally warm and add num docs traversed to count
    if (withWarm()) {
      Document doc = null;
      for (int m = 0; m < ir.maxDoc(); m++) {
        if (!ir.isDeleted(m)) {
          doc = ir.document(m);
          res += (doc==null ? 0 : 1);
        }
      }
    }
    
    if (withSearch()) {
      res++;
      IndexSearcher searcher = new IndexSearcher(ir);
      QueryMaker queryMaker = getQueryMaker();
      Query q = queryMaker.makeQuery();
      Hits hits = searcher.search(q);
      //System.out.println("searched: "+q);
      
      if (withTraverse()) {
        Document doc = null;
        if (hits != null && hits.length() > 0) {
          for (int m = 0; m < hits.length(); m++) {
            int id = hits.id(m);
            res++;

            if (withRetrieve()) {
              doc = ir.document(id);
              res += (doc==null ? 0 : 1);
            }
          }
        }
      }
      
      searcher.close();
    }
    
    if (closeReader) {
      ir.close();
    }
    return res;
  }

  /**
   * Return query maker used for this task.
   */
  public abstract QueryMaker getQueryMaker();

  /**
   * Return true if search should be performed.
   */
  public abstract boolean withSearch ();

  /**
   * Return true if warming should be performed.
   */
  public abstract boolean withWarm ();
  
  /**
   * Return true if, with search, results should be traversed.
   */
  public abstract boolean withTraverse ();

  /**
   * Return true if, with search & results traversing, docs should be retrieved.
   */
  public abstract boolean withRetrieve ();

}
