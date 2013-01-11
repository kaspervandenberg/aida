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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.benchmark.byTask.PerfRunData;

/**
 * Sequence of parallel or sequential tasks.
 */
public class TaskSequence extends PerfTask {
  private ArrayList tasks;
  private int repetitions = 1;
  private boolean parallel;
  private TaskSequence parent;
  private boolean letChildReport = true;
  private int rate = 0;
  private boolean perMin = false; // rate, if set, is, by default, be sec.
  private String seqName; 
  
  public TaskSequence (PerfRunData runData, String name, TaskSequence parent, boolean parallel) {
    super(runData);
    name = (name!=null ? name : (parallel ? "Par" : "Seq"));
    setName(name);
    setSequenceName();
    this.parent = parent;
    this.parallel = parallel;
    tasks = new ArrayList();
  }

  /**
   * @return Returns the parallel.
   */
  public boolean isParallel() {
    return parallel;
  }

  /**
   * @return Returns the repetitions.
   */
  public int getRepetitions() {
    return repetitions;
  }

  /**
   * @param repetitions The repetitions to set.
   */
  public void setRepetitions(int repetitions) {
    this.repetitions = repetitions;
    setSequenceName();
  }

  /**
   * @return Returns the parent.
   */
  public TaskSequence getParent() {
    return parent;
  }

  /*
   * (non-Javadoc)
   * @see org.apache.lucene.benchmark.byTask.tasks.PerfTask#doLogic()
   */
  public int doLogic() throws Exception {
    return ( parallel ? doParallelTasks() : doSerialTasks());
  }

  private int doSerialTasks() throws Exception {
    if (rate > 0) {
      return doSerialTasksWithRate();
    }
    
    int count = 0;
    for (int k=0; k<repetitions; k++) {
      for (Iterator it = tasks.iterator(); it.hasNext();) {
        PerfTask task = (PerfTask) it.next();
        count += task.runAndMaybeStats(letChildReport);
      }
    }
    return count;
  }

  private int doSerialTasksWithRate() throws Exception {
    long delayStep = (perMin ? 60000 : 1000) /rate;
    long nextStartTime = System.currentTimeMillis();
    int count = 0;
    for (int k=0; k<repetitions; k++) {
      for (Iterator it = tasks.iterator(); it.hasNext();) {
        PerfTask task = (PerfTask) it.next();
        long waitMore = nextStartTime - System.currentTimeMillis();
        if (waitMore > 0) {
          //System.out.println("wait: "+waitMore+" for rate: "+ratePerMin+" (delayStep="+delayStep+")");
          Thread.sleep(waitMore);
        }
        nextStartTime += delayStep; // this aims at avarage rate. 
        count += task.runAndMaybeStats(letChildReport);
      }
    }
    return count;
  }

  private int doParallelTasks() throws Exception {
    final int count [] = {0};
    Thread t[] = new Thread [repetitions * tasks.size()];
    // prepare threads
    int indx = 0;
    for (int k=0; k<repetitions; k++) {
      for (int i = 0; i < tasks.size(); i++) {
        final PerfTask task = (PerfTask) ((PerfTask) tasks.get(i)).clone();
        t[indx++] = new Thread() {
          public void run() {
            int n;
            try {
              n = task.runAndMaybeStats(letChildReport);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            synchronized (count) {
              count[0] += n;
            }
          }
        };
      }
    }
    // run threads
    startThreads(t);
    // wait for all threads to complete
    for (int i = 0; i < t.length; i++) {
      t[i].join();
    }
    // return total count
    return count[0];
  }

  // run threads
  private void startThreads(Thread[] t) throws InterruptedException {
    if (rate > 0) {
      startlThreadsWithRate(t);
      return;
    }
    for (int i = 0; i < t.length; i++) {
      t[i].start();
    }
  }

  // run threadsm with rate
  private void startlThreadsWithRate(Thread[] t) throws InterruptedException {
    long delayStep = (perMin ? 60000 : 1000) /rate;
    long nextStartTime = System.currentTimeMillis();
    for (int i = 0; i < t.length; i++) {
      long waitMore = nextStartTime - System.currentTimeMillis();
      if (waitMore > 0) {
        //System.out.println("thread wait: "+waitMore+" for rate: "+ratePerMin+" (delayStep="+delayStep+")");
        Thread.sleep(waitMore);
      }
      nextStartTime += delayStep; // this aims at avarage rate of starting threads. 
      t[i].start();
    }
  }

  public void addTask(PerfTask task) {
    tasks.add(task);
    task.setDepth(getDepth()+1);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    String padd = getPadding();
    StringBuffer sb = new StringBuffer(super.toString());
    sb.append(parallel ? " [" : " {");
    sb.append(NEW_LINE);
    for (Iterator it = tasks.iterator(); it.hasNext();) {
      PerfTask task = (PerfTask) it.next();
      sb.append(task.toString());
      sb.append(NEW_LINE);
    }
    sb.append(padd);
    sb.append(!letChildReport ? ">" : (parallel ? "]" : "}"));
    if (repetitions>1) {
      sb.append(" * " + repetitions);
    }
    if (rate>0) {
      sb.append(",  rate: " + rate+"/"+(perMin?"min":"sec"));
    }
    return sb.toString();
  }

  /**
   * Execute child tasks in a way that they do not reprt their time separately.
   * Current implementation if child tasks has child tasks of their own, those are not affected by this call. 
   */
  public void setNoChildReport() {
    letChildReport  = false;
    for (Iterator it = tasks.iterator(); it.hasNext();) {
      PerfTask task = (PerfTask) it.next();
      if (task instanceof TaskSequence) {
        ((TaskSequence)task).setNoChildReport();
  }
    }
  }

  /**
   * Returns the rate per minute: how many operations should be performed in a minute.
   * If 0 this has no effect.
   * @return the rate per min: how many operations should be performed in a minute.
   */
  public int getRate() {
    return (perMin ? rate : 60*rate);
  }

  /**
   * @param rate The rate to set.
   */
  public void setRate(int rate, boolean perMin) {
    this.rate = rate;
    this.perMin = perMin;
    setSequenceName();
  }

  private void setSequenceName() {
    seqName = super.getName();
    if (repetitions>1) {
      seqName += "_"+repetitions;
    }
    if (rate>0) {
      seqName += "_" + rate + (perMin?"/min":"/sec"); 
    }
    if (parallel && seqName.toLowerCase().indexOf("par")<0) {
      seqName += "_Par";
    }
  }

  public String getName() {
    return seqName; // overide to include more info 
  }

  /**
   * @return Returns the tasks.
   */
  public ArrayList getTasks() {
    return tasks;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException {
    TaskSequence res = (TaskSequence) super.clone();
    res.tasks = new ArrayList();
    for (int i = 0; i < tasks.size(); i++) {
      res.tasks.add(((PerfTask)tasks.get(i)).clone());
    }
    return res;
  }
  
}
