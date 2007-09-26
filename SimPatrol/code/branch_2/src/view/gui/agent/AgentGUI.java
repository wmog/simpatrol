/* AgentGUI.java */

/* The package of this class. */
package view.gui.agent;

/* Imported classes and/or interfaces. */
import javax.swing.JDialog;
import javax.swing.JFrame;
import model.agent.Agent;
import model.agent.PerpetualAgent;
import model.graph.Graph;

/** Implements a GUI to configure Agent objects.
 * 
 *  @see Agent*/
public class AgentGUI extends JDialog {	
	/* Methods. */
	/** Constructor.
	 * 
	 *  @param owner The GUI that called this one.
	 *  @param agent The Agent object to be configured by the GUI.
	 *  @param graph The graph of the patrolling simulation. */
    public AgentGUI(JFrame owner, Agent agent, Graph graph) {
        super(owner, true);
    	this.initComponents();
    	this.initComponents2(agent, graph);                
    }
    
    /** Initiates the components of the GUI.
     *  Generated by NetBeans IDE 3.6. */
    private void initComponents() {//GEN-BEGIN:initComponents
        xml_scroll = new javax.swing.JScrollPane();
        xml_area = new javax.swing.JTextArea();
        type_label = new javax.swing.JLabel();
        editing_panel = new javax.swing.JTabbedPane();
        south_panel = new javax.swing.JPanel();
        copies_panel = new javax.swing.JPanel();
        copies_label = new javax.swing.JLabel();
        copies_spinner = new javax.swing.JSpinner();
        buttons_panel = new javax.swing.JPanel();
        cancel_button = new javax.swing.JButton();
        ok_button = new javax.swing.JButton();

        xml_area.setEditable(false);
        xml_scroll.setViewportView(xml_area);
        
        setTitle("SimPatrol: Agent editor");

        type_label.setText("Perpetual agent");
        getContentPane().add(type_label, java.awt.BorderLayout.NORTH);

        this.editing_panel.addTab("Edit", null);
        this.editing_panel.addTab("XML", this.xml_scroll);
        editing_panel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                editing_panelStateChanged(evt);
            }
        });

        getContentPane().add(editing_panel, java.awt.BorderLayout.CENTER);

        south_panel.setLayout(new java.awt.BorderLayout());

        copies_panel.setLayout(new java.awt.BorderLayout());

        copies_label.setText("Number of copies ");
        copies_panel.add(copies_label, java.awt.BorderLayout.WEST);
        
        copies_spinner.setValue(new Integer(1));
        copies_spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                copies_spinnerStateChanged(evt);
            }
        });

        copies_panel.add(copies_spinner, java.awt.BorderLayout.CENTER);

        south_panel.add(copies_panel, java.awt.BorderLayout.CENTER);

        buttons_panel.setLayout(new java.awt.GridLayout(1, 2));

        cancel_button.setText("Cancel");
        cancel_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_buttonActionPerformed(evt);
            }
        });

        buttons_panel.add(cancel_button);

        ok_button.setText("Ok");
        ok_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ok_buttonActionPerformed(evt);
            }
        });

        buttons_panel.add(ok_button);

        south_panel.add(buttons_panel, java.awt.BorderLayout.EAST);

        getContentPane().add(south_panel, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-580)/2, (screenSize.height-500)/2, 580, 500);
    }//GEN-END:initComponents
    
    /** Complements the initiation of the components of the GUI.
     * 
     *  @param agent The Agent object to be configured.
     *  @param graph The graph of the patrolling simulation. */
    private void initComponents2(Agent agent, Graph graph) {
    	this.agent = agent;
    	this.agent_panel = new AgentJPanel(this, agent, graph);
    	this.editing_panel.setComponentAt(0, this.agent_panel);
    	
    	if(agent instanceof PerpetualAgent) this.type_label.setText("Perpetual agent");
    	else this.type_label.setText("Seasonal agent");    	
    }
    
    /** Executed when the copies_spinner changes.
     *  Generated by NetBeans IDE 3.6. */
    private void copies_spinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_copies_spinnerStateChanged
        if(((Integer) this.copies_spinner.getValue()).intValue() < 1)
        	this.copies_spinner.setValue(new Integer(1));
    }//GEN-LAST:event_copies_spinnerStateChanged
    
    /** Executed when the copies_spinner changes.
     *  Generated by NetBeans IDE 3.6. */
    private void editing_panelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_editing_panelStateChanged
    	if(this.editing_panel.getSelectedIndex() == 1) {
    		this.agent = this.agent_panel.getAgent();
        	this.xml_area.setText(this.agent.fullToXML(0));
    	}
    }//GEN-LAST:event_editing_panelStateChanged
    
    /** Executed when the ok button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void ok_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ok_buttonActionPerformed
        // TODO completar...
    	this.dispose();    	
    }//GEN-LAST:event_ok_buttonActionPerformed
    
    /** Executed when the cancel button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void cancel_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_buttonActionPerformed
        // TODO completar...
    	this.dispose();
    }//GEN-LAST:event_cancel_buttonActionPerformed
    
    /* Attributes. */
    // added manually
    private Agent agent;
    private AgentJPanel agent_panel;
    
    // added by Eclipse
	private static final long serialVersionUID = -4619949647276957848L;
	
	// added by NetBeans IDE 3.6    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttons_panel;
    private javax.swing.JButton cancel_button;
    private javax.swing.JLabel copies_label;
    private javax.swing.JPanel copies_panel;
    private javax.swing.JSpinner copies_spinner;
    private javax.swing.JTabbedPane editing_panel;
    private javax.swing.JButton ok_button;
    private javax.swing.JPanel south_panel;
    private javax.swing.JLabel type_label;
    private javax.swing.JTextArea xml_area;
    private javax.swing.JScrollPane xml_scroll;
    // End of variables declaration//GEN-END:variables    
}
