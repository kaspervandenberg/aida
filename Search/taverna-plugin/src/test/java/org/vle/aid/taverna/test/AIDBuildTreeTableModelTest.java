package org.vle.aid.taverna.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.vle.aid.taverna.build.AIDBuildTreeTableModel;
import org.vle.aid.taverna.build.AIDBuildTreeTableNode;

public class AIDBuildTreeTableModelTest {
    private AIDBuildTreeTableModel model;
    
	@Before
	public void setUp() throws Exception {
		model = new AIDBuildTreeTableModel();
	}

	@Test
	public void testGetChildrenIndex() {
	        AIDBuildTreeTableNode root = new AIDBuildTreeTableNode();
	        
	    	for(int i =0 ;i < 10; i++){
	    	       AIDBuildTreeTableNode child= new AIDBuildTreeTableNode("TestNode"+i, "TestNode"+i);
	    	       root.addChild(child);
	    	       assertEquals(model.getIndexOfChild(root, child), i);
	    	}
	        
	    	for(int i=9;i>=0;i--){
	    	      assertEquals(model.getIndexOfChild(root, new AIDBuildTreeTableNode("TestNode"+i, "TestNode"+i)), i);
	    	}
	    		
	}
	@Test
	public void testGetChild() {
	        AIDBuildTreeTableNode root = new AIDBuildTreeTableNode();
	        
	    	for(int i =0 ;i < 10; i++){
	    	       AIDBuildTreeTableNode child= new AIDBuildTreeTableNode("TestNode"+i, "TestNode"+i);
	    	       root.addChild(child);	    	       
	    	}
	        
	    	for(int i=9;i>=0;i--){
	    	        AIDBuildTreeTableNode expectedChild = new AIDBuildTreeTableNode("TestNode"+i, "TestNode"+i);
	    	     	assert(model.getChild(root, i).equals(expectedChild));
	    	}
	    		
	}

}
