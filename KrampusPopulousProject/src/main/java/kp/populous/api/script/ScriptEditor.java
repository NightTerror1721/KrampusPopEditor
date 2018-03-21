/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import kp.populous.api.script.Script.DecompileResult;
import kp.populous.api.utils.ButtonTabComponent;
import kp.populous.api.utils.CompletionProviderLoader;
import kp.populous.api.utils.FileChooser;
import kp.populous.api.utils.Utils;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author Marc
 */
public class ScriptEditor extends JFrame
{
    private CustomCompletionProvider autoProvider;
    
    private ScriptEditor()
    {
        initComponents();
        init();
    }
    
    private void init()
    {
        Utils.focus(this);
        Utils.setIcon(this);
        setTitle("Krampus - PopScript Editor - v" + Utils.VERSION);
        
        autoProvider = createCompletionProvider();
        initInfoList(funcsInfoList);
        initInfoList(constsInfoList);
        initInfoList(othersInfoList);
        initInfos();
        
        //createNewPage("NewScript");
    }
    
    public static final void open()
    {
        new ScriptEditor().setVisible(true);
    }
    
    private void close()
    {
        askSaveAll();
        dispose();
    }
    
    private TextArea createNewPage(String title)
    {
        JPanel panel = new JPanel(new BorderLayout());

        TextArea textArea = new TextArea(panel, title);
        textArea.setSyntaxEditingStyle(Utils.POP_SCRIPT_TEXT_TYPE);
        textArea.setCodeFoldingEnabled(true);
        textArea.addParser(new PopScriptParser());
        textArea.setParserDelay(500);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        textArea.undoLastAction();
        panel.add(sp);
        
        AutoCompletion ac = new AutoCompletion(autoProvider);
        ac.setParameterAssistanceEnabled(true);
        ac.setShowDescWindow(true);
        ac.install(textArea);
        
        pages.addTab(title, panel);
        pages.setTabComponentAt(pages.getTabCount() - 1, textArea.tabButton = new ButtonTabComponent(pages, panel, () -> {
            askSave(textArea);
            ac.uninstall();
        }));
        
        textArea.clearChanges();
        textArea.updateTabTitle();
        //textArea.addParser(parser);
        
        return textArea;
    }
    
    
    private void save() { save(getSelectedTextArea()); }
    private void save(TextArea area)
    {
        if(area == null)
            return;
        
        File file;
        if(area.fromFile == null)
        {
            file = FileChooser.saveScript(this);
            if(file == null)
                return;
        }
        else file = area.fromFile;
        
        save(area, file);
    }
    private void save(TextArea area, File file)
    {
        if(area == null || file == null)
            return;
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            area.write(bw);
            area.fromFile = file;
            area.name =  Utils.getFileName(file);
            area.clearChanges();
            JOptionPane.showMessageDialog(this, "The file has been saved successfully!");
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(this, "There was an error while saving the file.\n" + ex.getMessage(),
                    "Saving Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveAs() { saveAs(getSelectedTextArea()); }
    private void saveAs(TextArea area)
    {
        if(area == null)
            return;
        
        File file = FileChooser.saveScript(this);
        if(file == null)
            return;
        save(area, file);
    }
    
    private void askSave() { askSave(getSelectedTextArea()); }
    private void askSave(TextArea area)
    {
        if(area == null || !area.hasChanges)
            return;
        
        if(JOptionPane.showConfirmDialog(rootPane, "File " + area.name +
                " contains changes without saving. Do you want to save them?",
                "Save file", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
            save(area);
    }
    
    private void askSaveAll()
    {
        int len = getTextAreaCount();
        for(int i=0;i<len;i++)
        {
            TextArea area = getTextArea(i);
            if(area.hasChanges)
                askSave(area);
        }
    }
    
    private void saveAll()
    {
        int len = getTextAreaCount();
        for(int i=0;i<len;i++)
        {
            TextArea area = getTextArea(i);
            if(area.hasChanges)
                save(area);
        }
    }
    
    
    private void load()
    {
        File file = FileChooser.openScript(this);
        if(file == null)
            return;
        
        String name = Utils.getFileName(file);
        TextArea area = createNewPage(name);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
        {
            area.read(br, null);
            area.fromFile = file;
            area.clearChanges();
            if(getTextAreaCount() > 1)
                pages.setSelectedIndex(getTextAreaCount() - 1);
            
        }
        catch(IOException ex)
        {
            area.destroy();
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(this, "An error occurred while loading the file.\n" + ex.getMessage(),
                    "Loading Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private Script compile() { return compile(getSelectedTextArea()); }
    private Script compile(TextArea area)
    {
        if(area == null)
            return null;
        
        terminalTabs.setSelectedIndex(0);
        StringBuilder sb = new StringBuilder();
        sb.append("Compile Script \"").append(area.name).append("\" at ").append(new Date()).append(":\n");
        long t1 = System.currentTimeMillis();
        CompilationResult result = Script.compile(area.getText());
        long t2 = System.currentTimeMillis();
        sb.append("COMPILATION ").append(!result.hasErrors() ? "SUCCESSFUL" : "FAILED")
                .append(" (total time: ").append(t2 - t1).append("ms)");
        if(result.hasErrors())
            sb.append("\n").append(result.getErrorLog());
        if(result.hasMessages())
            sb.append("\nCompiler messages:\n").append(result.getMessageLog());
        
        terminal.setText(sb.toString());
        return result.getScript();
    }
    
    
    private void importCompiled()
    {
        File file = FileChooser.importCompiled(this);
        if(file == null)
            return;
        
        String name = Utils.getFileName(file);
        TextArea area = createNewPage(name);
        StringBuilder sb = new StringBuilder();
        DecompileResult res;
        boolean status;
        String log = "";
        long t1 = System.currentTimeMillis(), t2;
        try
        {
            Script script = new Script();
            script.read(file);
            
            sb.append("Decompile Script \"").append(area.name).append("\" at ").append(new Date()).append(":\n");
            t1 = System.currentTimeMillis();
            res = script.decompile();
            if(res.hasErrors())
            {
                status = false;
                List<String> errors = res.getErrors();
                StringBuilder errlog = new StringBuilder();
                errlog.append("Several errors has been ocurred:");
                for(String err : errors)
                    errlog.append("\n").append(err);
                log = sb.toString();
            }
            else status = true;
            
            area.setText(res.getCode());
            area.setCaretPosition(0);
            area.hasChanges = true;
            area.updateTabTitle();
            if(getTextAreaCount() > 1)
                pages.setSelectedIndex(getTextAreaCount() - 1);
            
        }
        catch(IOException ex)
        {
            area.destroy();
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(this, "An error occurred while importing the file.\n" + ex.getMessage(),
                    "Loading Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch(ScriptIOException ex)
        {
            area.destroy();
            status = false;
            log = "FATAL ERROR:\n" + ex.getMessage();
        }
        t2 = System.currentTimeMillis();
        sb.append("DECOMPILATION ").append(status ? "SUCCESSFUL" : "FAILED")
                .append(" (total time: ").append(t2 - t1).append("ms)");
        if(!status)
            sb.append("\n").append(log);
        terminalTabs.setSelectedIndex(0);
        terminal.setText(sb.toString());
    }
    
    
    private void compileAndExport() { compileAndExport(getSelectedTextArea()); }
    private void compileAndExport(TextArea area)
    {
        if(area == null)
            return;
        
        Script script;
        if((script = compile(area)) == null)
        {
            JOptionPane.showMessageDialog(this, "Cannot export script \"" + area.name +
                    "\" because has compilation errors.",
                    "Compilation Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File file = FileChooser.compileAndExport(this);
        if(file == null)
            return;
        
        try
        {
            script.write(file);
            JOptionPane.showMessageDialog(this, "The file has been exported successfully!");
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(this, "There was an error while exporting the file.\n" + ex.getMessage(),
                    "Saving Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    private CustomCompletionProvider createCompletionProvider()
    {
        CustomCompletionProvider prov = new CustomCompletionProvider();
        
        /*try {
            prov.loadFromXML("PopLangCompletion.xml");
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }*/
        CompletionProviderLoader cpl = new CompletionProviderLoader(prov);
        cpl.loadExtern(Utils.openInnerResource("/PopLangCompletion.json"));
        
        return prov;
    }
    
    private void initInfos()
    {
        DefaultListModel<CompletionInfo> funcs = (DefaultListModel<CompletionInfo>) funcsInfoList.getModel();
        DefaultListModel<CompletionInfo> consts = (DefaultListModel<CompletionInfo>) constsInfoList.getModel();
        DefaultListModel<CompletionInfo> others = (DefaultListModel<CompletionInfo>) othersInfoList.getModel();
        autoProvider.getCompletions().forEach((c) -> {
            CompletionInfo info = new CompletionInfo(c);
            if(c instanceof FunctionCompletion)
                funcs.addElement(info);
            else if(c instanceof VariableCompletion)
                consts.addElement(info);
            else if(c instanceof BasicCompletion)
                others.addElement(info);
        });
    }
    
    private TextArea getTextArea(int index)
    {
        if(index < 0 || index >= pages.getTabCount())
            return null;
        Component c = pages.getComponentAt(index);
        if(c == null)
            return null;
        return (TextArea) ((RTextScrollPane)((JPanel) c).getComponent(0)).getTextArea();
    }
    private TextArea getSelectedTextArea()
    {
        Component c = pages.getSelectedComponent();
        if(c == null)
            return null;
        return (TextArea) ((RTextScrollPane)((JPanel) c).getComponent(0)).getTextArea();
    }
    private int getTextAreaCount() { return pages.getTabCount(); }
    
    private final class TextArea extends RSyntaxTextArea
    {
        private final JPanel base;
        private ButtonTabComponent tabButton;
        private boolean hasChanges;
        private String name;
        private File fromFile;
        
        private TextArea(JPanel base, String name)
        {
            super(20, 60);
            super.getDocument().addDocumentListener(new DocumentListener()
            {
                @Override
                public void insertUpdate(DocumentEvent e)
                {
                    if(!hasChanges)
                    {
                        hasChanges = true;
                        updateTabTitle();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e)
                {
                    if(!hasChanges)
                    {
                        hasChanges = true;
                        updateTabTitle();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e)
                {
                    if(!hasChanges)
                    {
                        hasChanges = true;
                        updateTabTitle();
                    }
                }
            });
            this.base = base;
            this.name = name;
        }
        
        public final void destroy() { tabButton.push(); }
        
        private int getTabIndex()
        {
            int len = pages.getTabCount();
            for(int i=0;i<len;i++)
                if(pages.getComponentAt(i) == base)
                    return i;
            return -1;
        }
        
        public final void clearChanges()
        {
            if(hasChanges)
            {
                hasChanges = false;
                updateTabTitle();
            }
        }
        
        public final void setTitle(String title)
        {
            name = Objects.requireNonNull(title);
            updateTabTitle();
        }
        
        public final void updateTabTitle()
        {
            String title = hasChanges ? name + "*" : name;
            int idx = getTabIndex();
            if(idx >= 0)
            {
                pages.setTitleAt(idx, title);
                pages.updateUI();
            }
        }
    }
    
    private static final class CustomCompletionProvider extends DefaultCompletionProvider
    {
        public final List<Completion> getCompletions() { return Collections.unmodifiableList(completions); }
    }
    
    private static final class CompletionInfo
    {
        private final Completion completion;
        
        private CompletionInfo(Completion c)
        {
            this.completion = Objects.requireNonNull(c);
        }
        
        @Override
        public final String toString() { return completion.getReplacementText(); }
    }
    
    private void initInfoList(JList<CompletionInfo> list)
    {
        DefaultListModel<CompletionInfo> model = new DefaultListModel<>();
        list.setModel(model);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        list.addListSelectionListener(e -> {
            CompletionInfo info = list.getSelectedValue();
            if(info != null)
            {
                terminalTabs.setSelectedIndex(1);
                helpTerminal.setText(info.completion.getSummary());
                helpTerminal.setCaretPosition(0);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        pages = new javax.swing.JTabbedPane();
        infoTabs = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        funcsInfoList = new javax.swing.JList<>();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        constsInfoList = new javax.swing.JList<>();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        othersInfoList = new javax.swing.JList<>();
        terminalTabs = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        terminal = new javax.swing.JTextPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        helpTerminal = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem14 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jSplitPane1.setDividerLocation(380);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setContinuousLayout(true);

        jSplitPane2.setDividerLocation(180);
        jSplitPane2.setContinuousLayout(true);

        pages.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jSplitPane2.setRightComponent(pages);

        funcsInfoList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(funcsInfoList);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
        );

        infoTabs.addTab("Functions", jPanel5);

        constsInfoList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(constsInfoList);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
        );

        infoTabs.addTab("Constants", jPanel6);

        othersInfoList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        othersInfoList.setToolTipText("");
        jScrollPane5.setViewportView(othersInfoList);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
        );

        infoTabs.addTab("Others", jPanel1);

        jSplitPane2.setLeftComponent(infoTabs);

        jSplitPane1.setTopComponent(jSplitPane2);

        terminal.setEditable(false);
        jScrollPane2.setViewportView(terminal);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
        );

        terminalTabs.addTab("Terminal", jPanel3);

        helpTerminal.setEditable(false);
        helpTerminal.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(helpTerminal);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
        );

        terminalTabs.addTab("Help", jPanel4);

        jSplitPane1.setRightComponent(terminalTabs);

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("New Script");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);
        jMenu1.add(jSeparator1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Open Script");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);
        jMenu1.add(jSeparator2);

        jMenuItem7.setText("Import Compiled Script");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem7);

        jMenuItem8.setText("Compile and Export Script");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem8);
        jMenu1.add(jSeparator4);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Save Script");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem4.setText("Save Script As...");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("Save All Scripts");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);
        jMenu1.add(jSeparator3);

        jMenuItem6.setText("Exit");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem6);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem9.setText("Undo");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem9);

        jMenuItem10.setText("Redo");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem10);
        jMenu2.add(jSeparator5);

        jMenuItem11.setText("Cut");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem11);

        jMenuItem12.setText("Copy");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem12);

        jMenuItem13.setText("Paste");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem13);
        jMenu2.add(jSeparator6);

        jMenuItem14.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem14.setText("Compile");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem14);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        createNewPage("NewScript");
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        load();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        save();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        saveAs();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        saveAll();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        close();
    }//GEN-LAST:event_formWindowClosing

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        close();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        TextArea area = getSelectedTextArea();
        if(area == null)
            return;
        if(area.canUndo())
            area.undoLastAction();
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        TextArea area = getSelectedTextArea();
        if(area == null)
            return;
        if(area.canRedo())
            area.redoLastAction();
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        TextArea area = getSelectedTextArea();
        if(area == null)
            return;
        area.copy();
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        TextArea area = getSelectedTextArea();
        if(area == null)
            return;
        area.cut();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        TextArea area = getSelectedTextArea();
        if(area == null)
            return;
        area.paste();
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        compile();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        importCompiled();
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        compileAndExport();
    }//GEN-LAST:event_jMenuItem8ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<CompletionInfo> constsInfoList;
    private javax.swing.JList<CompletionInfo> funcsInfoList;
    private javax.swing.JTextPane helpTerminal;
    private javax.swing.JTabbedPane infoTabs;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JList<CompletionInfo> othersInfoList;
    private javax.swing.JTabbedPane pages;
    private javax.swing.JTextPane terminal;
    private javax.swing.JTabbedPane terminalTabs;
    // End of variables declaration//GEN-END:variables
}
