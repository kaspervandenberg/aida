package org.vle.aid.taverna;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCreationEvent;
import org.vle.aid.taverna.components.Gui;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.file.translator.ScuflFileType;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

public class AIDEditDataflow {

    
    /**
     * Add string constant processor into current workflow
     * Sample code reference : http://tinyurl.com/aida-plugin-SampleAddProc
     * @param newStringConstantName (obtained from aida plugin, usually the term name)
     * @param newStringConstantValue (obtained from aida plugin, usually the url of the term)
     */
	public static void editDataFlowAddStringConstantProcessor(String newStringConstantName, String newStringConstantValue) {
		
		ModelMap modelMap = ModelMap.getInstance();
		
		//Get the current dataflow from model map
		Dataflow dataFlow =(Dataflow) modelMap.getModel(ModelMapConstants.CURRENT_DATAFLOW);

		// Just in case we're not in taverna
		if(modelMap == null || dataFlow == null) return;
		
		// Create string constant activity
		StringConstantActivity stringConstantActivity = new StringConstantActivity();

		// Create string constant configuration bean and set the value into the url of the term.
		StringConstantConfigurationBean stringConstantConfigBean = new StringConstantConfigurationBean();
		stringConstantConfigBean.setValue(newStringConstantValue);

	
		// Half blindly following steps in GraphViewTransferHandler.java:106 when importing Component
		// - t2workbench/views/graph/src/main/java/net/sf/taverna/t2/workbench/views/graph

		// List of editing steps we're going to do
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		
		// Individual edit steps created with this
		Edits edits = new EditsImpl();
		
		// I hope this configure the string constant with the beans
		editList.add(edits.getConfigureActivityEdit(stringConstantActivity, stringConstantConfigBean));
	
		// Create processor with the new name based on selectedNode term
		Processor p=edits.createProcessor(newStringConstantName);
		
		// I don't know what this is, if I don't need it maybe i'll remove it later
		editList.add(edits.getDefaultDispatchStackEdit(p));
		
		// Add the activity to the processor
		editList.add(edits.getAddActivityEdit(p, stringConstantActivity));
		
		// Add the processor to the dataFlow
		editList.add(edits.getAddProcessorEdit(dataFlow, p));

		// Let the edit manager does the job hopefully it notify the rest of the workbench of what it does.
		EditManager editManager = EditManager.getInstance();
		try {
			editManager.doDataflowEdit(dataFlow, new CompoundEdit(editList));
		} catch (EditException e) {
			Gui.showErrorWarnings("Failed to add string to dataflow, probably existing string processor with the same name already exist", e);
		}
		
	}
	
//	/**
//	 * Given URL (probably from my experiment) load and open the workflow on the taverna.
//	 * First attempt to open assuming its T2 Flow file type, if failed, tries again with SCUFL File type.
//	 * @param workflowURL
//	 */
//	public static void loadRemoteWorkflow(URL workflowURL) {
//	    T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();
//	    ScuflFileType SCUFL_FILE_TYPE = new ScuflFileType();
//	    
//	    FileManager fileManager = FileManager.getInstance();
//	    
//	    boolean success = true;
//	    Exception ee = null;
//	    try {
//		fileManager.openDataflow(T2_FLOW_FILE_TYPE, workflowURL);
//
//	    } catch (OpenException e) {
//		success = false;
//		ee = e;
//	    }
//
//	    if (!success) {
//		try {
//		    success = true;
//		    fileManager.openDataflow(SCUFL_FILE_TYPE, workflowURL);
//
//		} catch (OpenException e) {
//		    success = false;
//		    ee = e;
//		}
//	    }
//	    if (!success)
//		Gui.showErrorWarnings("Failed to open workflow "+workflowURL.toString(), ee);
//	}
//	
//	
//	/**
//	 * Add a remote workflow and inserted it into taverna as a nested workflow
//	 * Based on codes from here : http://tinyurl.com/aida-plugin-nestedWF
//	 * First it opened the remote workflow to be nested, and added it to the current workflow
//	 * @param workflowURL
//	 */
//	public static void addNestedWorkflow(URL workflowURL){
//	    EditManager editManager = EditManager.getInstance();
//	    Edits edits = editManager.getEdits();
//	   
//	    FileManager fileManager = FileManager.getInstance();
//	    
//	    // Save first what we are opening now
//	    Dataflow currentWorkflow = fileManager.getCurrentDataflow();
//	    // If we're not opening anything, make an empty  workflow
//	    if(currentWorkflow == null) {
//		currentWorkflow = edits.createDataflow();
//		fileManager.setCurrentDataflow(currentWorkflow);
//	    }
//	    
//	    // Now open the will be nested workflow in a new window
//	    loadRemoteWorkflow(workflowURL);
//
//	    // This is the thing that we will nested into the current (previous) workflow
//	    Dataflow originalNested = fileManager.getCurrentDataflow();
//	   
//	    // The new nested data flow activity
//	    DataflowActivity nestedDataflowActivity = new DataflowActivity();
//
//	    // Now follow the recipe from http://tinyurl.com/aida-plugin-nestedWF
//	    Processor nestedProc = edits.createProcessor(workflowURL.toString());
//
//	    List<Edit<?>> addEdits = new ArrayList<Edit<?>>();
//	    addEdits.add(edits.getAddProcessorEdit(currentWorkflow, nestedProc));
//	    addEdits.add(edits.getAddActivityEdit(nestedProc, nestedDataflowActivity));
//	    addEdits.add(edits.getMapProcessorPortsForActivityEdit(nestedProc));
//	    addEdits.add(edits.getConfigureActivityEdit(nestedDataflowActivity, originalNested));
//
//	    try {
//		editManager.doDataflowEdit(currentWorkflow, new CompoundEdit(addEdits));
//	    } catch (EditException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    }
//
//	    fileManager.setCurrentDataflow(currentWorkflow);
//
//
//	}
}
