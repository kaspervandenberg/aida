package org.vle.aid.thesaurus.client;

import java.util.Collection;
import java.util.Vector;

/**
 * 
 * Class which defines a Thesaurus Concept.
 * 
 * @author emeij
 *
 */
public class ThesaurusConceptNode {
  
  /**
   * Children
   */
  public final Collection<ThesaurusConceptNode> subnodes = new Vector<ThesaurusConceptNode>();
  
  /**
   * The id 
   */
  public String id;
  
  /**
   * The label
   */
  public String text;
  
  // extjs
  /** 
   * ExtJS quicktip
   */
  public String qtip;
  
  /**
   * ExtJS icon class
   */
  public String iconCls;
  
  /**
   * @param id
   * @param label
   * @param qtip
   * @param iconCls
   */
  public ThesaurusConceptNode(String id, String label, String qtip,
      String iconCls) {

    this.id = id;
    this.text = label;
    this.qtip = qtip;
    this.iconCls = iconCls;
  }
  
  /**
   * @param id
   * @param label
   */
  public ThesaurusConceptNode(String id, String label) {

    this.id = id;
    this.text = label;
    this.qtip = "";
    this.iconCls = "";
  }
  
  /**
   * Adds a child
   * @param n child to add
   */
  public void addChild(ThesaurusConceptNode n) {
    this.subnodes.add(n);
  }
  
}
