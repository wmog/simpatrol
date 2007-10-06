/* SocietyGUI.java */

/* The package of this class. */
package society;

/* Imported classes and/or interfaces. */
import javax.swing.JFrame;
import model.agent.ClosedSociety;
import model.agent.Society;
import model.graph.Graph;

/** Implements a GUI to configure Society objects.
 * 
 *  @see Society */
public class SocietyGUI extends javax.swing.JDialog {
	/* Methods. */
    
    /** Constructor.
     * 
     *  @param owner The GUI that called this one.
	 *  @param society The Society object to be configured by the GUI.
	 *  @param graph The graph of the patrolling simulation. */
    public SocietyGUI(JFrame owner, Society society, Graph graph) {
        super(owner, true);
        this.initComponents();
        this.initComponents2(society, graph);
    }
    
    /** Initiates the components of the GUI.
     *  Generated by NetBeans IDE 3.6. */
    private void initComponents() {//GEN-BEGIN:initComponents
        xml_scroll = new javax.swing.JScrollPane();
        xml_area = new javax.swing.JTextArea();
        society_label = new javax.swing.JLabel();
        tabbed_panel = new javax.swing.JTabbedPane();
        buttons_panel = new javax.swing.JPanel();
        buttons_internal_frame = new javax.swing.JPanel();
        cancel_button = new javax.swing.JButton();
        ok_button = new javax.swing.JButton();

        xml_area.setEditable(false);
        xml_scroll.setViewportView(xml_area);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SimPatrol: Society editor");
        society_label.setText("Society");
        getContentPane().add(society_label, java.awt.BorderLayout.NORTH);

        this.tabbed_panel.addTab("Editor", null);
        this.tabbed_panel.addTab("XML", this.xml_scroll);
        tabbed_panel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbed_panelStateChanged(evt);
            }
        });

        getContentPane().add(tabbed_panel, java.awt.BorderLayout.CENTER);

        buttons_panel.setLayout(new java.awt.BorderLayout());

        buttons_internal_frame.setLayout(new java.awt.GridLayout(1, 2));

        cancel_button.setText("Cancel");
        cancel_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_buttonActionPerformed(evt);
            }
        });

        buttons_internal_frame.add(cancel_button);

        ok_button.setText("Ok");
        ok_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ok_buttonActionPerformed(evt);
            }
        });

        buttons_internal_frame.add(ok_button);

        buttons_panel.add(buttons_internal_frame, java.awt.BorderLayout.EAST);

        getContentPane().add(buttons_panel, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-600)/2, (screenSize.height-550)/2, 600, 550);
    }//GEN-END:initComponents
    
    /** Complements the initiation of the components of the GUI.
     * 
     *  @param society The Society object to be configured.
     *  @param graph The graph of the patrolling simulation. */
    private void initComponents2(Society society, Graph graph) {
    	this.society = society;
    	
    	this.society_panel = new SocietyJPanel(this, society, graph);
    	this.tabbed_panel.setComponentAt(0, this.society_panel);
    	
    	if(society instanceof ClosedSociety) this.society_label.setText("Closed society");
    	else this.society_label.setText("Open society");
    }
    
    /** Executed when the ok button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void ok_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ok_buttonActionPerformed
        // TODO add your handling code here:
    	this.dispose();
    }//GEN-LAST:event_ok_buttonActionPerformed
    
    /** Executed when the cancel button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void cancel_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_buttonActionPerformed
        // TODO add your handling code here:
    	this.dispose();
    }//GEN-LAST:event_cancel_buttonActionPerformed
    
    /** Executed when the tabbed_panel changes.
     *  Generated by NetBeans IDE 3.6. */    
    private void tabbed_panelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbed_panelStateChanged
    	if(this.tabbed_panel.getSelectedIndex() == 1) {
    		this.society = this.society_panel.getSociety();
        	this.xml_area.setText(this.society.fullToXML(0));
    	}
    }//GEN-LAST:event_tabbed_panelStateChanged
    
    /* Attributes. */
    // added manually    
    private Society society;
    private SocietyJPanel society_panel;
    
    // added by Eclipse
	private static final long serialVersionUID = -4113064113160208165L;
    
    // added by NetBeans IDE 3.6    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttons_internal_frame;
    private javax.swing.JPanel buttons_panel;
    private javax.swing.JButton cancel_button;
    private javax.swing.JButton ok_button;
    private javax.swing.JLabel society_label;
    private javax.swing.JTabbedPane tabbed_panel;
    private javax.swing.JTextArea xml_area;
    private javax.swing.JScrollPane xml_scroll;
    // End of variables declaration//GEN-END:variables
}