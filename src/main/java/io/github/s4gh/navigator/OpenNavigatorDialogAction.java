package io.github.s4gh.navigator;

import java.awt.Frame;
import java.lang.ref.WeakReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Tools",
        id = "io.github.s4gh.navigator.OpenNavigatorDialogAction"
)
@ActionRegistration(
        displayName = "#CTL_OpenNavigatorDialogAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 99),
    @ActionReference(path = "Shortcuts", name = "DO-F"),
})
@Messages("CTL_OpenNavigatorDialogAction=Filtering Code Navigator")
public final class OpenNavigatorDialogAction implements ActionListener {

    // Keep only a weak reference to avoid leaks if the window disappears
    private static final Object LOCK = new Object();
    private static WeakReference<NavigatorPopupDialog> DIALOG_REF = new WeakReference<>(null);

    @Override
    public void actionPerformed(ActionEvent e) {
        // Actions run on EDT in NB, but this keeps it explicit and safe:
        SwingUtilities.invokeLater(() -> {
            NavigatorPopupDialog dialog;
            synchronized (LOCK) {
                dialog = DIALOG_REF.get();
                if (dialog == null || !dialog.isDisplayable()) {
                    Frame parent = (Frame) WindowManager.getDefault().getMainWindow();
                    dialog = new NavigatorPopupDialog(parent);
                    dialog.installShortcutBridge(dialog);
                    dialog.setModal(false); // assuming modeless — change if needed

                    dialog.addWindowListener(new WindowAdapter() {
                        @Override public void windowClosed(WindowEvent e)  { clearRef(); }
                        @Override public void windowClosing(WindowEvent e) { clearRef(); }
                    });

                    DIALOG_REF = new WeakReference<>(dialog);
                }
            }

            if (dialog.isVisible()) {
                // Already open → do your toggle
                dialog.toggleShowInherited();
                // Optionally bring to front
                dialog.toFront();
                dialog.requestFocus();
            } else {
                // Not visible yet → show it
                dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                dialog.setVisible(true);
            }
            
            dialog.focusSearchField();
        });
    }

    private static void clearRef() {
        synchronized (LOCK) {
            DIALOG_REF = new WeakReference<>(null);
        }
    }
}