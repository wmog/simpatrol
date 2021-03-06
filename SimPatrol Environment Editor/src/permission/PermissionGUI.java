/* PermissionGUI.java */

/* The package of this class. */
package permission;

/* Imported classes and/or interfaces. */
import javax.swing.JDialog;
import model.action.ActionTypes;
import model.perception.PerceptionTypes;
import model.permission.ActionPermission;
import model.permission.PerceptionPermission;
import model.permission.Permission;

/** Implements a GUI to configure Permission objects.
 * 
 *  @see Permission */
public class PermissionGUI extends JDialog {
	/* Methods. */        
	/** Construtor.
     * 
     *  @param owner The GUI that called this one.
     *  @param permission The Permission object to be configured by the GUI. */
    public PermissionGUI(JDialog owner, Permission permission) {
    	super(owner, true);
        this.initComponents();
        this.initComponents2(permission);
    }
    
    /** Initiates the components of the GUI.
     *  Generated by NetBeans IDE 3.6. */
    private void initComponents() {//GEN-BEGIN:initComponents
        xml_scroll = new javax.swing.JScrollPane();
        xml_area = new javax.swing.JTextArea();
        type_panel = new javax.swing.JPanel();
        type_label = new javax.swing.JLabel();
        editor_panel = new javax.swing.JTabbedPane();
        button_panel = new javax.swing.JPanel();
        button_internal_panel = new javax.swing.JPanel();
        cancel_button = new javax.swing.JButton();
        ok_button = new javax.swing.JButton();

        xml_area.setEditable(false);
        xml_scroll.setViewportView(xml_area);

        setTitle("SimPatrol: Permission editor");

        type_panel.setLayout(new java.awt.BorderLayout());

        type_label.setText("Permission type");
        type_panel.add(type_label, java.awt.BorderLayout.CENTER);

        getContentPane().add(type_panel, java.awt.BorderLayout.NORTH);

        this.editor_panel.addTab("Editor", null);
        this.editor_panel.addTab("XML", this.xml_scroll);
        editor_panel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                editor_panelStateChanged(evt);
            }
        });

        getContentPane().add(editor_panel, java.awt.BorderLayout.CENTER);

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
        setBounds((screenSize.width-500)/2, (screenSize.height-350)/2, 500, 350);
    }//GEN-END:initComponents
    
    /** Complements the initiation of the components of the GUI.
     * 
     *  @param perception The Perception object to be configured. */
    private void initComponents2(Permission permission) {
    	this.permission = permission;
    	
    	if(permission instanceof PerceptionPermission)
    		switch(((PerceptionPermission) permission).getPerception_type()) {
    			case PerceptionTypes.AGENTS: {
    				this.type_label.setText("Agents perception permission");
    				this.permission_panel = new PermissionJPanel(this, permission, true, true, false, false);
    				break;
    			}
    			case PerceptionTypes.BROADCAST: {
    				this.type_label.setText("Broadcast perception permission");
    				this.permission_panel = new PermissionJPanel(this, permission, false, true, false, false);
    				break;
    			}
    			case PerceptionTypes.GRAPH: {
    				this.type_label.setText("Graph perception permission");
    				this.permission_panel = new PermissionJPanel(this, permission, true, true, false, false);
    				break;
    			}
    			case PerceptionTypes.STIGMAS: {
    				this.type_label.setText("Stigmas perception permission");
    				this.permission_panel = new PermissionJPanel(this, permission, true, true, false, false);
    				break;
    			}
    			case PerceptionTypes.SELF: {
    				this.type_label.setText("Self perception permission");
    				this.permission_panel = new PermissionJPanel(this, permission, false, true, false, false);
    				break;
    			}
    		}
    	else switch(((ActionPermission) permission).getAction_type()) {
    		case ActionTypes.BROADCAST: {
    			this.type_label.setText("Broadcast action permission");
    			this.permission_panel = new PermissionJPanel(this, permission, true, true, false, false);
    			break;
    		}
    		case ActionTypes.GOTO: {
    			this.type_label.setText("Goto action permission");
    			this.permission_panel = new PermissionJPanel(this, permission, true, true, true, true);
    			break;
    		}
    		case ActionTypes.ATOMIC_RECHARGE: {
    			this.type_label.setText("Recharge action permission");
    			this.permission_panel = new PermissionJPanel(this, permission, false, true, true, false);
    			break;
    		}
    		case ActionTypes.STIGMATIZE: {
    			this.type_label.setText("Stigmatize action permission");
    			this.permission_panel = new PermissionJPanel(this, permission, false, true, false, false);
    			break;
    		}
    		case ActionTypes.VISIT: {
    			this.type_label.setText("Visit action permission");
    			this.permission_panel = new PermissionJPanel(this, permission, false, true, false, false);
    			break;
    		}
    	}
    	
    	this.editor_panel.setComponentAt(0, this.permission_panel);
    }
    
    /** Executed when the ok button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void ok_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ok_buttonActionPerformed
        this.permission = this.permission_panel.getPermission();
    	this.dispose();
    }//GEN-LAST:event_ok_buttonActionPerformed
    
    /** Executed when the cancel button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void cancel_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_buttonActionPerformed
    	this.permission = null;
    	this.dispose();
    }//GEN-LAST:event_cancel_buttonActionPerformed
    
    /** Executed when the editing_panel changes.
     *  Generated by NetBeans IDE 3.6. */
    private void editor_panelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_editor_panelStateChanged
    	if(this.editor_panel.getSelectedIndex() == 1) {
    		this.permission = this.permission_panel.getPermission();
        	this.xml_area.setText(this.permission.fullToXML(0));
    	}
    }//GEN-LAST:event_editor_panelStateChanged
    
    /** Returns the Permission object configured by the GUI.
     * 
     *  @return The Permission object configured by the GUI. */
    public Permission getPermission() {
    	return this.permission;
    }
    
    /* Attributes. */
    // added manually
    private Permission permission;
    private PermissionJPanel permission_panel;
    
    // added by Eclipse
    private static final long serialVersionUID = 9222045718104814343L;
	
	// added by NetBeans IDE 3.6    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel button_internal_panel;
    private javax.swing.JPanel button_panel;
    private javax.swing.JButton cancel_button;
    private javax.swing.JTabbedPane editor_panel;
    private javax.swing.JButton ok_button;
    private javax.swing.JLabel type_label;
    private javax.swing.JPanel type_panel;
    private javax.swing.JTextArea xml_area;
    private javax.swing.JScrollPane xml_scroll;
    // End of variables declaration//GEN-END:variables    
}