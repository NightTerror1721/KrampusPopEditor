/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.utils;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Asus
 */
public final class FileChooser
{
    private FileChooser() {}
    
    private static JFileChooser SCRIPTS;
    private static JFileChooser scripts()
    {
        if(SCRIPTS == null)
        {
            SCRIPTS = new JFileChooser(Utils.getUserDirFile());
            SCRIPTS.setAcceptAllFileFilterUsed(true);
            SCRIPTS.setFileSelectionMode(JFileChooser.FILES_ONLY);
            SCRIPTS.setMultiSelectionEnabled(false);
            SCRIPTS.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f) { return f.isDirectory() || f.getName().endsWith(".popscr"); }

                @Override
                public final String getDescription() { return "PopScript (.popscr)"; }

            });
        }
        return SCRIPTS;
    }
    
    private static JFileChooser DC_SCRIPTS;
    private static JFileChooser dcscripts()
    {
        if(DC_SCRIPTS == null)
        {
            DC_SCRIPTS = new JFileChooser(Utils.getUserDirFile());
            DC_SCRIPTS.setAcceptAllFileFilterUsed(true);
            DC_SCRIPTS.setFileSelectionMode(JFileChooser.FILES_ONLY);
            DC_SCRIPTS.setMultiSelectionEnabled(false);
            DC_SCRIPTS.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f) { return f.isDirectory() || f.getName().endsWith(".dat"); }

                @Override
                public final String getDescription() { return "Compiled Script (.dat)"; }

            });
        }
        return DC_SCRIPTS;
    }
    
    public static final File openScript(Component parent)
    {
        JFileChooser chooser = scripts();
        if(chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
            return null;
        return chooser.getSelectedFile();
    }
    
    public static final File saveScript(Component parent)
    {
        JFileChooser chooser = scripts();
        if(chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION)
            return null;
        File file = chooser.getSelectedFile();
        if(!file.getName().endsWith(".popscr"))
            file = new File(file.getAbsolutePath() + ".popscr");
        return file;
    }
    
    public static final File importCompiled(Component parent)
    {
        JFileChooser chooser = dcscripts();
        if(chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
            return null;
        return chooser.getSelectedFile();
    }
    
    public static final File compileAndExport(Component parent)
    {
        JFileChooser chooser = dcscripts();
        if(chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION)
            return null;
        File file = chooser.getSelectedFile();
        if(!file.getName().endsWith(".dat"))
            file = new File(file.getAbsolutePath() + ".dat");
        return file;
    }
}
