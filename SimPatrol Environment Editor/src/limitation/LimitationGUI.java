/* LimitationGUI.java */

/* The package of this class. */
package limitation;

/* Imported classes and/or interfaces. */
import javax.swing.JDialog;

import model.limitation.AccelerationLimitation;
import model.limitation.DepthLimitation;
import model.limitation.Limitation;
import model.limitation.SpeedLimitation;
import model.limitation.StaminaLimitation;

/** Implements a GUI to configure Limitation objects.
 * 
 *  @see Limitation */
public class LimitationGUI extends javax.swing.JDialog {
	/* Methods. */
    /** Constructor.
     * 
     *  @param owner The GUI that called this one.
     *  @param limitation The Limitation object to be configured. */
    public LimitationGUI(JDialog owner, Limitation limitation) {
    	super(owner, true);
    	this.initComponents();
    	this.initComponents2(limitation);    	
    }
    
    /** Initiates the components of the GUI.
     *  Generated by NetBeans IDE 3.6. */
    private void initComponents() {//GEN-BEGIN:initComponents
        xml_scroll = new javax.swing.JScrollPane();
        xml_area = new javax.swing.JTextArea();
        type_panel = new javax.swing.JPanel();
        type_label = new javax.swing.JLabel();
        editing_panel = new javax.swing.JTabbedPane();
        button_panel = new javax.swing.JPanel();
        button_internal_panel = new javax.swing.JPanel();
        cancel_button = new javax.swing.JButton();
        ok_button = new javax.swing.JButton();

        xml_area.setEditable(false);
        xml_scroll.setViewportView(xml_area);

        setTitle("SimPatrol: Limitation editor");

        type_panel.setLayout(new java.awt.BorderLayout());

        type_label.setText("Limitation ");
        type_panel.add(type_label, java.awt.BorderLayout.CENTER);

        getContentPane().add(type_panel, java.awt.BorderLayout.NORTH);

        this.editing_panel.addTab("Editor", null);
        this.editing_panel.addTab("XML", this.xml_scroll);
        editing_panel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                editing_panelStateChanged(evt);
            }
        });

        getContentPane().add(editing_panel, java.awt.BorderLayout.CENTER);

        button_panel.setLayout(new java.awt.BorderLayout());

        button_internal_panel.setLayout(new java.awt.GridLayout(1, 2));

        cancel_button.setText("Cancel");
        cancel_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_buttonActionPerformed(evt);
            }
        });

        button_internal_panel.add(cancel_button);

        ok_button.setText("Ok");
        ok_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ok_buttonActionPerformed(evt);
            }
        });

        button_internal_panel.add(ok_button);

        button_panel.add(button_internal_panel, java.awt.BorderLayout.EAST);

        getContentPane().add(button_panel, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-400)/2, (screenSize.height-300)/2, 400, 300);
    }//GEN-END:initComponents
    
    /** Complements the initiation of the components of the GUI.
     * 
     *  @param limitation The Limitation object to be configured. */
    private void initComponents2(Limitation limitation) {
    	this.limitation = limitation;
    	
    	if(limitation instanceof DepthLimitation) {
    		this.type_label.setText("Depth limitation");
    		this.limitation_panel = new DepthLimitationJPanel((DepthLimitation) limitation);
    	}
    	else if(limitation instanceof StaminaLimitation) {
    		this.type_label.setText("Stamina limitation");    		
    		this.limitation_panel = new StaminaLimitationJPanel((StaminaLimitation) limitation);
    	}
    	else if(limitation instanceof SpeedLimitation) {
    		this.type_label.setText("Speed limitation");
    		this.limitation_panel = new SpeedLimitationJPanel((SpeedLimitation) limitation);
    	}
    	else {
    		this.type_label.setText("Acceleration limitation");
    		this.limitation_panel = new AccelerationLimitationJPanel((AccelerationLimitation) limitation);
    	}
    	
    	this.editing_panel.setComponentAt(0, this.limitation_panel);
    }
    
    /** Executed when the editing_panel changes.
     *  Generated by NetBeans IDE 3.6. */
    private void editing_panelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_editing_panelStateChanged
    	if(this.editing_panel.getSelectedIndex() == 1) {
    		this.limitation = this.limitation_panel.getLimitation();
        	this.xml_area.setText(this.limitation.fullToXML(0));
    	}
    }//GEN-LAST:event_editing_panelStateChanged
    
    /** Executed when the ok button is pressed.
     *  Generated by NetBeans IDE 3.6. */    
    private void ok_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ok_buttonActionPerformed
    	this.limitation = this.limitation_panel.getLimitation();
    	this.dispose();
    }//GEN-LAST:event_ok_buttonActionPerformed
    
    /** Executed when the cancel button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void cancel_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_buttonActionPerformed
    	this.limitation = null;
    	this.dispose();
    }//GEN-LAST:event_cancel_buttonActionPerformed
    
    /** Returns the limitation configured by the GUI.
     * 
     *  @return The Limitation object configured by this GUI. */
    public Limitation getLimitation() {
    	return this.limitation;
    }
    
    /* Attributes. */
    // added manually
    private Limitation limitation;
    private LimitationJPanel limitation_panel;
    
    // added by Eclipse
	private static final long serialVersionUID = 9017167367729849468L;
    
    // added by NetBeans IDE 3.6   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel button_internal_panel;
    private javax.swing.JPanel button_panel;
    private javax.swing.JButton cancel_button;
    private javax.swing.JTabbedPane editing_panel;
    private javax.swing.JButton ok_button;
    private javax.swing.JLabel type_label;
    private javax.swing.JPanel type_panel;
    private javax.swing.JTextArea xml_area;
    private javax.swing.JScrollPane xml_scroll;
    // End of variables declaration//GEN-END:variables
}