/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import io.github.dheid.fontchooser.FontDialog;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTextField;
import kp.populous.api.script.ScriptEditorStylePool.StyleToken;
import kp.populous.api.utils.Utils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author Marc
 */
public class ScriptEditorConfig extends JDialog
{
    private static final ScriptEditorStylePool STYLE_POOL = new ScriptEditorStylePool();
    
    private final ScriptEditor parent;
    private DefaultListModel<StyleToken> stylesModel;
    private final ScriptEditorStylePool styles = STYLE_POOL.copy();
    private RSyntaxTextArea codeExampleArea;
    
    public ScriptEditorConfig(ScriptEditor parent)
    {
        super(parent, true);
        this.parent = parent;
        initComponents();
        init();
    }
    
    private void init()
    {
        Utils.focus(this);
        setResizable(false);
        
        stylesList.setModel(stylesModel = new DefaultListModel<>());
        for(StyleToken token : StyleToken.getAvailableTokens())
            stylesModel.addElement(token);
        
        codeExampleArea = new RSyntaxTextArea(loadExampleScript(), 8, 10);
        codeExampleArea.setSyntaxEditingStyle(Utils.POP_SCRIPT_TEXT_TYPE);
        codeExampleArea.setEditable(false);
        codeExampleArea.setCaretPosition(0);
        codeExampleArea.setSyntaxScheme(generateSyntaxScheme(styles));
        RTextScrollPane sp = new RTextScrollPane(codeExampleArea);
        jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.X_AXIS));
        jPanel3.add(sp);
        
        pack();
    }
    
    private void apply()
    {
        styles.copyTo(STYLE_POOL);
        STYLE_POOL.save();
        parent.repaintCurrentScript();
    }
    
    public static final SyntaxScheme generateSyntaxScheme() { return generateSyntaxScheme(STYLE_POOL); }
    private static SyntaxScheme generateSyntaxScheme(ScriptEditorStylePool styles)
    {
        SyntaxScheme scheme = new SyntaxScheme(ScriptEditorStylePool.DEFAULT_FONT);
        styles.fillScheme(scheme);
        return scheme;
    }
    
    private void showSyntaxToken()
    {
        StyleToken token = stylesList.getSelectedValue();
        if(token == null)
        {
            l_font.setText("");
            l_fore.setText("");
            l_back.setText("");
            
            fixFieldColor(l_fore, null);
            fixFieldColor(l_back, null);
            
            b_font.setEnabled(false);
            b_fore.setEnabled(false);
            b_back.setEnabled(false);
            return;
        }
        
        Style s = styles.getStyle(token);
        b_font.setEnabled(true);
        b_fore.setEnabled(true);
        b_back.setEnabled(true);
        
        l_font.setText(s.font.getName() + " " + s.font.getSize());
        
        Color color = s.foreground == null ? ScriptEditorStylePool.DEFAULT_FG_COLOR : s.foreground;
        l_fore.setText(colorToString(color));
        fixFieldColor(l_fore, color);
        
        color = s.background == null ? ScriptEditorStylePool.DEFAULT_BG_COLOR : s.background;
        l_back.setText(colorToString(color));
        fixFieldColor(l_back, color);
    }
    
    
    private static String colorToString(Color color)
    {
        if(color == null)
            return "[r=255, g=255, b=255]"; 
        return "[r=" + color.getRed() + ", " +
               "g=" + color.getBlue() + ", " +
               "b=" + color.getGreen() + "]";
    }
    
    private static void fixFieldColor(JTextField field, Color color)
    {
        if(color == null)
        {
            field.setBackground(Color.WHITE);
            field.setForeground(Color.BLACK);
            return;
        }
        Color text = getContrastColor(color);
        field.setBackground(color);
        field.setForeground(text);
    }
    
    private static  Color getContrastColor(Color color)
    {
        double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
        return y > 128 ? Color.black : Color.white;
    }
    
    private void chooseColor(boolean foreground)
    {
        StyleToken token = stylesList.getSelectedValue();
        if(token == null)
            return;
        Style s = styles.getStyle(token);
        if(s == null)
            return;
        
        Color color = foreground
                ? s.foreground == null ? ScriptEditorStylePool.DEFAULT_FG_COLOR : s.foreground
                : s.background == null ? ScriptEditorStylePool.DEFAULT_BG_COLOR : s.background;
        
        color = JColorChooser.showDialog(this, "Color Chooser", color);
        if(color == null)
            return;
        
        if(foreground)
            s.foreground = color;
        else s.background = color;
        showSyntaxToken();
        codeExampleArea.repaint();
    }
    
    private static String loadExampleScript()
    {
        StringBuilder sb = new StringBuilder(128);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(Utils.openInnerResource("/kp/populous/api/script/PopScriptExample.popscr"))))
        {
            char[] buffer = new char[128];
            int len;
            while((len = br.read(buffer)) > 0)
                sb.append(buffer, 0, len);
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
            return "";
        }
        return sb.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        stylesList = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        l_font = new javax.swing.JTextField();
        l_fore = new javax.swing.JTextField();
        l_back = new javax.swing.JTextField();
        b_font = new javax.swing.JButton();
        b_fore = new javax.swing.JButton();
        b_back = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("ScriptEditor - Properties");

        stylesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        stylesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                stylesListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(stylesList);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Properties", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabel1.setText("Font:");

        jLabel2.setText("Foreground:");

        jLabel3.setText("Background:");

        l_font.setEditable(false);

        l_fore.setEditable(false);

        l_back.setEditable(false);

        b_font.setText("...");
        b_font.setEnabled(false);
        b_font.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_fontActionPerformed(evt);
            }
        });

        b_fore.setText("...");
        b_fore.setEnabled(false);
        b_fore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_foreActionPerformed(evt);
            }
        });

        b_back.setText("...");
        b_back.setEnabled(false);
        b_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_backActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(l_back, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .addComponent(l_fore, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(l_font, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(b_font, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(b_fore, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(b_back, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(l_font, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(b_font))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(l_fore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(b_fore))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(l_back, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(b_back))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Preview", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 176, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Syntax", jPanel1);

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Apply");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Accept");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void stylesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_stylesListValueChanged
        showSyntaxToken();
    }//GEN-LAST:event_stylesListValueChanged

    private void b_fontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_fontActionPerformed
        StyleToken token = stylesList.getSelectedValue();
        if(token == null)
            return;
        
        Style s = styles.getStyle(token);
        if(s == null)
            return;
        FontDialog dialog = new FontDialog(this, "Font Chooser", true);
        Utils.focus(dialog);
        dialog.setDefaultCloseOperation(FontDialog.DISPOSE_ON_CLOSE);
        dialog.setSelectedFont(s.font);
        dialog.setVisible(true);
        
        if(!dialog.isCancelSelected())
        {
            s.font = dialog.getSelectedFont();
            showSyntaxToken();
            codeExampleArea.repaint();
        }
    }//GEN-LAST:event_b_fontActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        apply();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        apply();
        dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void b_foreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_foreActionPerformed
        chooseColor(true);
    }//GEN-LAST:event_b_foreActionPerformed

    private void b_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_backActionPerformed
        chooseColor(false);
    }//GEN-LAST:event_b_backActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton b_back;
    private javax.swing.JButton b_font;
    private javax.swing.JButton b_fore;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField l_back;
    private javax.swing.JTextField l_font;
    private javax.swing.JTextField l_fore;
    private javax.swing.JList<StyleToken> stylesList;
    // End of variables declaration//GEN-END:variables
}
