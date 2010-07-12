/* EmpiricalEventTimeProbabilityDistributionJPanel.java */

/* The package of this class. */
package etpd;

/* Imported classes and/or interfaces. */
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import util.FileReader;
import model.etpd.EmpiricalEventTimeProbabilityDistribution;
import model.etpd.EventTimeProbabilityDistribution;

/** Implements the GUI panel able to configure
 *  EmpiricalEventTimeProbabilityDistribution objects.
 *  
 *  @see EmpiricalEventTimeProbabilityDistribution */
public class EmpiricalEventTimeProbabilityDistributionJPanel extends EventTimeProbabilityDistributionJPanel {    
	/* Methods. */
    /** Constructor.
     * 
     *  @param etpd The EmpiricalEventTimeProbabilityDistribution object to be configured. */
    public EmpiricalEventTimeProbabilityDistributionJPanel(EmpiricalEventTimeProbabilityDistribution etpd) {
        this.initComponents();
        this.initComponents(etpd);
        
    }
    
    /** Initiates the components of the GUI.
     *  Generated by NetBeans IDE 3.6. */
    private void initComponents() {//GEN-BEGIN:initComponents
        distribution_file_chooser = new javax.swing.JFileChooser();
        distribution_panel = new javax.swing.JPanel();
        distribution_scroll = new javax.swing.JScrollPane();
        distribution_table = new javax.swing.JTable();
        buttons_panel = new javax.swing.JPanel();
        buttons_internal_panel = new javax.swing.JPanel();
        add_button = new javax.swing.JButton();
        remove_button = new javax.swing.JButton();
        load_button = new javax.swing.JButton();
        seed_panel = new javax.swing.JPanel();
        seed_label = new javax.swing.JLabel();
        seed_field = new javax.swing.JTextField();
        seed_button = new javax.swing.JButton();

        this.distribution_file_chooser.setDialogTitle("Open file");
        this.distribution_file_chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        this.distribution_file_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        setLayout(new java.awt.BorderLayout());

        distribution_panel.setLayout(new java.awt.BorderLayout());

        distribution_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {new Double(1.0)}
            },
            new String [] {
                "Distribution values"
            }
        ) {			
			private static final long serialVersionUID = -562979772820683357L;
			Class[] types = new Class [] {
                java.lang.Double.class
            };

            @SuppressWarnings("unchecked")
			public Class<Object> getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });
        distribution_scroll.setViewportView(distribution_table);

        distribution_panel.add(distribution_scroll, java.awt.BorderLayout.CENTER);

        buttons_panel.setLayout(new java.awt.BorderLayout());

        buttons_internal_panel.setLayout(new java.awt.GridLayout(3, 0));

        add_button.setText("Add value");
        add_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_buttonActionPerformed(evt);
            }
        });

        buttons_internal_panel.add(add_button);

        remove_button.setText("Remove value");
        remove_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remove_buttonActionPerformed(evt);
            }
        });

        buttons_internal_panel.add(remove_button);

        load_button.setText("Load distribution");
        load_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                load_buttonActionPerformed(evt);
            }
        });

        buttons_internal_panel.add(load_button);

        buttons_panel.add(buttons_internal_panel, java.awt.BorderLayout.NORTH);

        distribution_panel.add(buttons_panel, java.awt.BorderLayout.EAST);

        add(distribution_panel, java.awt.BorderLayout.CENTER);

        seed_panel.setLayout(new java.awt.BorderLayout());

        seed_label.setText("Seed ");
        seed_panel.add(seed_label, java.awt.BorderLayout.WEST);

        seed_field.setEditable(false);
        seed_field.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        this.seed_field.setText(String.valueOf((int) System.currentTimeMillis()));
        seed_panel.add(seed_field, java.awt.BorderLayout.CENTER);

        seed_button.setText("Generate");
        seed_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seed_buttonActionPerformed(evt);
            }
        });

        seed_panel.add(seed_button, java.awt.BorderLayout.EAST);

        add(seed_panel, java.awt.BorderLayout.NORTH);

    }//GEN-END:initComponents
    
    /** Complements the initiation of the components of the GUI.
     * 
     *  @param etpd The EmpiricalEventTimeProbabilityDistribution object to be configured. */
    public void initComponents(EmpiricalEventTimeProbabilityDistribution etpd) {
        this.etpd = etpd;
        
        DefaultTableModel table_model = (DefaultTableModel) this.distribution_table.getModel();
        int row_count = table_model.getRowCount();
        for(int i = 0; i < row_count; i++)
            table_model.removeRow(0);
                    
        double[] distribution = this.etpd.getDistribution();
        for(int i = 0; i < distribution.length; i++) {
            Double[] content = {new Double(distribution[i])};
            table_model.addRow(content);
        }
        
        this.seed_field.setText(String.valueOf(this.etpd.getSeed()));
    }
    
    /** Executed when the seed_button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void seed_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seed_buttonActionPerformed
        this.seed_field.setText(String.valueOf((int) System.currentTimeMillis()));
    }//GEN-LAST:event_seed_buttonActionPerformed
    
    /** Executed when the load_button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void load_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_load_buttonActionPerformed
        if(this.distribution_file_chooser.showOpenDialog(this) == 0)
        	try {
        		LinkedList<Double> probabilities = new LinkedList<Double>();
        		
        		FileReader file_reader = new FileReader(this.distribution_file_chooser.getSelectedFile().getPath());
        		while(!file_reader.isEndOfFile()) {
        			try {
        				probabilities.add(new Double(file_reader.readDouble()));                	
        			}
        			catch(NumberFormatException e) {
        				// do nothing
        			}
        		}        		
        		file_reader.close();
        		
        		DefaultTableModel table_model = (DefaultTableModel) this.distribution_table.getModel();
                int row_count = table_model.getRowCount();
                for(int i = 0; i < row_count; i++)
                	table_model.removeRow(0);
                
                for(int i = 0; i < probabilities.size(); i++) {
                	Double[] current_prob = {probabilities.get(i)};
                    table_model.addRow(current_prob);
                }
        	}
        	catch(IOException e) {
        		JOptionPane.showMessageDialog(this, "Errors ocurred when reading the file", "File error", JOptionPane.ERROR_MESSAGE);
        	}
    }//GEN-LAST:event_load_buttonActionPerformed
    
    /** Executed when the remove_button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void remove_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remove_buttonActionPerformed
    	DefaultTableModel table_model = (DefaultTableModel) this.distribution_table.getModel();
    	
    	int selected_rows_count = this.distribution_table.getSelectedRowCount();
    	int[] selected_rows = this.distribution_table.getSelectedRows();
    	for(int i = 0; i < selected_rows_count; i++) {
    		table_model.removeRow(selected_rows[i]);
    		
    		for(int j = i + 1; j < selected_rows_count; j++)
    			selected_rows[j]--;
       }
    }//GEN-LAST:event_remove_buttonActionPerformed
    
    /** Executed when the add_button is pressed.
     *  Generated by NetBeans IDE 3.6. */
    private void add_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_buttonActionPerformed
        Double[] content = {new Double(1)};
        ((DefaultTableModel) this.distribution_table.getModel()).addRow(content);
    }//GEN-LAST:event_add_buttonActionPerformed
        
    public EventTimeProbabilityDistribution getETPD() {
    	boolean non_zero_value = false;
    	
        double[] values = new double[this.distribution_table.getRowCount()];        
        for(int i = 0; i < values.length; i++) {
        	values[i] = ((Double) this.distribution_table.getValueAt(i, 0)).doubleValue();
            
            if(values[i] != 0)
                non_zero_value = true;
            
            if(values[i] < 0 || values[i] > 1) {
                JOptionPane.showMessageDialog(this, "The probability values must belong to the real interval [0, 1].", "Probability value error.", JOptionPane.ERROR_MESSAGE);
                
                if(this.etpd != null)
                    this.initComponents(this.etpd);
                
                return this.etpd;
            }
        }
        
        if(non_zero_value)
            this.etpd = new EmpiricalEventTimeProbabilityDistribution((int) System.currentTimeMillis(), values);
        else {
            JOptionPane.showMessageDialog(this, "At least one empirical probability value must be higher than zero.", "Probability value error.", JOptionPane.ERROR_MESSAGE);
            
            if(this.etpd != null)
            	this.initComponents(this.etpd);
        }
        
        return this.etpd;
    }
    
    /* Attributes. */    
    // added manually
    private EmpiricalEventTimeProbabilityDistribution etpd;
    
    // added by eclipse
    private static final long serialVersionUID = 6211837510073000481L;
    
    // added by NetBeans IDE 3.6
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add_button;
    private javax.swing.JPanel buttons_internal_panel;
    private javax.swing.JPanel buttons_panel;
    private javax.swing.JFileChooser distribution_file_chooser;
    private javax.swing.JPanel distribution_panel;
    private javax.swing.JScrollPane distribution_scroll;
    private javax.swing.JTable distribution_table;
    private javax.swing.JButton load_button;
    private javax.swing.JButton remove_button;
    private javax.swing.JButton seed_button;
    private javax.swing.JTextField seed_field;
    private javax.swing.JLabel seed_label;
    private javax.swing.JPanel seed_panel;
    // End of variables declaration//GEN-END:variables    
}