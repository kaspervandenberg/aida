package org.vle.aid.taverna.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.vle.aid.taverna.build.AIDBuildTreeTableNode;
public class AIDBuildTreeTableNodeTest {
    	private AIDBuildTreeTableNode node;
    
    	@Before
	public void setUp() throws Exception {
		node = new AIDBuildTreeTableNode();
	}

	@Test
	public void testEquals() {
	    	AIDBuildTreeTableNode first = new AIDBuildTreeTableNode("term","url");
	    	AIDBuildTreeTableNode second = new AIDBuildTreeTableNode("term","url");
		assertEquals(first.equals(second), true);
	}
	
	@Test
	public void testAddChild(){
	    	node.addChild("my","child");
	    	assertEquals(node.getChildren().size(), 1);	    	
	}
	
	@Test 
	public void testRemoveChildStrings(){	    	
	    	node.addChild("my","child");
	    	node.removeChild("my","child");	    	
	    	assertEquals(node.getChildren().size() , 0);
	}
	
	@Test
	public void testRemoveChildObject(){
	    	node.addChild("my","child");
	    	AIDBuildTreeTableNode toRemove = new AIDBuildTreeTableNode("my","child");
	    	node.removeChild(toRemove);	    	
	    	assertEquals(node.getChildren().size() , 0);	
	}

	
	
}
