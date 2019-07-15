package ecosystem.util;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChooser {
    private String initialDirectory;
    private final JFileChooser jFileChooser;

    public FileChooser(String initialDirectory, String filterDescription, String... extensions) {
        this.jFileChooser = new JFileChooser();
        this.initialDirectory = initialDirectory;
        this.jFileChooser.setAcceptAllFileFilterUsed(false);
        this.jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDescription, extensions));
    }

    public void setInitialDirectory(String initialDirectory) {
        this.initialDirectory = initialDirectory;
    }

    public File chooseFile() {
        if (initialDirectory != null)
            jFileChooser.setCurrentDirectory(new File(initialDirectory));

        jFileChooser.showSaveDialog(null);
        return jFileChooser.getSelectedFile();
    }
}