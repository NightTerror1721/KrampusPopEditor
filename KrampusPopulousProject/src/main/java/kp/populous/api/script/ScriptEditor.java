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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import kp.populous.api.script.Script.DecompileResult;
import kp.populous.api.utils.ButtonTabComponent;
import kp.populous.api.utils.CompletionProviderLoader;
import kp.populous.api.utils.FileChooser;
import kp.populous.api.utils.FilterListModel;
import kp.populous.api.utils.Utils;
import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.SizeGripIcon;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

/**
 *
 * @author Marc
 */
public class ScriptEditor extends JFrame implements SearchListener
{
    private CustomCompletionProvider autoProvider;
    private FindDialog findDialog;
    private ReplaceDialog replaceDialog;
    private FindToolBar findToolBar;
    private ReplaceToolBar replaceToolBar;
    private StatusBar statusBar;
    
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
        
        findDialog = new FindDialog(this, this);
        replaceDialog = new ReplaceDialog(this, this);
        
        SearchContext context = findDialog.getSearchContext();
        replaceDialog.setSearchContext(context);
        context.setMarkAll(false);
        
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
        JPanel contentPane = new JPanel(new BorderLayout());
        CollapsibleSectionPanel panel = new CollapsibleSectionPanel(true);
        contentPane.add(panel);
        

        TextArea textArea = new TextArea(panel, title);
        textArea.setSyntaxEditingStyle(Utils.POP_SCRIPT_TEXT_TYPE);
        textArea.setCodeFoldingEnabled(true);
        textArea.setMarkOccurrences(true);
        textArea.addParser(new PopScriptParser());
        textArea.setParserDelay(500);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        textArea.undoLastAction();
        panel.add(sp);
        
        textArea.setSyntaxScheme(ScriptEditorConfig.generateSyntaxScheme());
        
        ErrorStrip errorStrip = new ErrorStrip(textArea);
        contentPane.add(errorStrip, BorderLayout.EAST);
        
        AutoCompletion ac = new AutoCompletion(autoProvider);
        ac.setParameterAssistanceEnabled(true);
        ac.setShowDescWindow(true);
        ac.install(textArea);
        
        pages.addTab(title, contentPane);
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
        
        prov.sortCompletions();
        //prov.printAllCompletions();
        
        return prov;
    }
    
    private void initInfos()
    {
        FilterListModel<CompletionInfo> funcs = (FilterListModel<CompletionInfo>) funcsInfoList.getModel();
        FilterListModel<CompletionInfo> consts = (FilterListModel<CompletionInfo>) constsInfoList.getModel();
        FilterListModel<CompletionInfo> others = (FilterListModel<CompletionInfo>) othersInfoList.getModel();
        autoProvider.getCompletions().forEach((c) -> {
            CompletionInfo info = new CompletionInfo(c);
            if(c instanceof FunctionCompletion)
                funcs.addElement(info);
            else if(c instanceof VariableCompletion)
                consts.addElement(info);
            else if(c instanceof BasicCompletion)
                others.addElement(info);
        });
        
        funcs.sort();
        consts.sort();
        others.sort();
        
        funcs.unfilter();
        consts.unfilter();
        others.unfilter();
    }
    
    private TextArea getTextArea(int index)
    {
        if(index < 0 || index >= pages.getTabCount())
            return null;
        Component c = pages.getComponentAt(index);
        if(c == null)
            return null;
        return (TextArea) ((RTextScrollPane)(((CollapsibleSectionPanel)((JPanel) c).getComponent(0))).getComponent(0)).getTextArea();
    }
    private TextArea getSelectedTextArea()
    {
        Component c = pages.getSelectedComponent();
        if(c == null)
            return null;
        return (TextArea) ((RTextScrollPane)(((CollapsibleSectionPanel)((JPanel) c).getComponent(0))).getComponent(0)).getTextArea();
    }
    private int getTextAreaCount() { return pages.getTabCount(); }
    
    public final void repaintCurrentScript()
    {
        TextArea area = getSelectedTextArea();
        if(area != null)
            area.repaint();
    }
    
    private final class TextArea extends RSyntaxTextArea
    {
        private final CollapsibleSectionPanel base;
        private ButtonTabComponent tabButton;
        private boolean hasChanges;
        private String name;
        private File fromFile;
        
        private TextArea(CollapsibleSectionPanel base, String name)
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
            //super.
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
        
        public final void sortCompletions()
        {
            completions.sort((c0, c1) -> {
                if(c0 == c1)
                    return 0;
                return String.CASE_INSENSITIVE_ORDER.compare(c0.getInputText(), c1.getInputText());
            });
        }
        
        public final void printAllCompletions()
        {
            StringBuilder sb = new StringBuilder(1024);
            getCompletions().forEach((c) -> sb.append(c).append('\n'));
            Utils.printStringToFile(new File("completions.txt"), sb.toString());
        }
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
        FilterListModel<CompletionInfo> model = new FilterListModel<>();
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
    
    private void doSearch()
    {
        FilterListModel<CompletionInfo> funcs = (FilterListModel<CompletionInfo>) funcsInfoList.getModel();
        FilterListModel<CompletionInfo> consts = (FilterListModel<CompletionInfo>) constsInfoList.getModel();
        FilterListModel<CompletionInfo> others = (FilterListModel<CompletionInfo>) othersInfoList.getModel();
        String text = t_search.getText();
        
        funcs.filter(text);
        consts.filter(text);
        others.filter(text);
    }
    
    @Override
    public final void searchEvent(SearchEvent e)
    {
        TextArea area = getSelectedTextArea();
        if(area == null)
            return;
        
        
        SearchEvent.Type type = e.getType();
        SearchContext context = e.getSearchContext();
        SearchResult result;
        
        switch(type)
        {
            default:
            case MARK_ALL:
                result = SearchEngine.markAll(area, context);
                break;
            case FIND:
                result = SearchEngine.find(area, context);
                if(!result.wasFound())
                    UIManager.getLookAndFeel().provideErrorFeedback(area);
                break;
            case REPLACE:
                result = SearchEngine.replace(area, context);
                if(!result.wasFound())
                    UIManager.getLookAndFeel().provideErrorFeedback(area);
                break;
            case REPLACE_ALL:
                result = SearchEngine.replaceAll(area, context);
                JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
                break;
        }
        
        String text = null;
        if(result.wasFound())
            text = "Text found; occurrences marked: " + result.getMarkedCount();
        else if(type == SearchEvent.Type.MARK_ALL)
        {
            if(result.getMarkedCount() > 0)
                text = "Occurrences marked" + result.getMarkedCount();
            else text = "";
        }
        else text = "Text not found";
    }
    
    @Override
    public final String getSelectedText()
    {
        TextArea area = getSelectedTextArea();
        if(area == null)
            return "";
        return area.getSelectedText();
    }
    
    private static final class StatusBar extends JPanel
    {
        private final JLabel label;

        public StatusBar()
        {
            label = new JLabel("Ready");
            setLayout(new BorderLayout());
            add(label, BorderLayout.LINE_START);
            add(new JLabel(new SizeGripIcon()), BorderLayout.LINE_END);
        }
        
        public final void setLabel(String label) { this.label.setText(label); }
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
        jPanel2 = new javax.swing.JPanel();
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
        jPanel7 = new javax.swing.JPanel();
        t_search = new javax.swing.JTextField();
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
        jMenuItem15 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
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
        jMenu3 = new javax.swing.JMenu();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();

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
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
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
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
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
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        infoTabs.addTab("Others", jPanel1);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Search"));

        t_search.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                t_searchFocusLost(evt);
            }
        });
        t_search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                t_searchKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(t_search)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(t_search, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(infoTabs)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoTabs))
        );

        jSplitPane2.setLeftComponent(jPanel2);

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

        jMenuItem15.setText("Properties");
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem15);
        jMenu1.add(jSeparator7);

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

        jMenu3.setText("Search");

        jMenuItem16.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem16.setText("Find...");
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem16);

        jMenuItem17.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem17.setText("Replace...");
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem17);

        jMenuItem18.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem18.setText("Go to line...");
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem18);

        jMenuBar1.add(jMenu3);

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

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        ScriptEditorConfig config = new ScriptEditorConfig(this);
        config.setVisible(true);
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void t_searchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_t_searchKeyReleased
        doSearch();
    }//GEN-LAST:event_t_searchKeyReleased

    private void t_searchFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_t_searchFocusLost
        doSearch();
    }//GEN-LAST:event_t_searchFocusLost

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        TextArea area = getSelectedTextArea();
        if(area == null)
            return;
        
        if(findDialog != null && findDialog.isVisible())
            findDialog.setVisible(false);
        if(replaceDialog != null && replaceDialog.isVisible())
            replaceDialog.setVisible(false);
        
        GoToDialog dialog = new GoToDialog(this);
        dialog.setMaxLineNumberAllowed(area.getLineCount());
        dialog.setVisible(true);
        int line = dialog.getLineNumber();
        if(line < 1)
            return;
        try { area.setCaretPosition(area.getLineStartOffset(line - 1)); }
        catch(BadLocationException ex)
        {
            UIManager.getLookAndFeel().provideErrorFeedback(area);
            ex.printStackTrace(System.err);
        }
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed
        if(findDialog == null)
            return;
        if(replaceDialog != null && replaceDialog.isVisible())
            replaceDialog.setVisible(false);
        findDialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
        if(replaceDialog == null)
            return;
        if(findDialog != null && findDialog.isVisible())
            findDialog.setVisible(false);
        replaceDialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem17ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<CompletionInfo> constsInfoList;
    private javax.swing.JList<CompletionInfo> funcsInfoList;
    private javax.swing.JTextPane helpTerminal;
    private javax.swing.JTabbedPane infoTabs;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
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
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JList<CompletionInfo> othersInfoList;
    private javax.swing.JTabbedPane pages;
    private javax.swing.JTextField t_search;
    private javax.swing.JTextPane terminal;
    private javax.swing.JTabbedPane terminalTabs;
    // End of variables declaration//GEN-END:variables
}
