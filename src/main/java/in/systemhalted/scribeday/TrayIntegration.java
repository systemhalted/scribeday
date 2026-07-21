package in.systemhalted.scribeday;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.URL;
import java.util.Locale;
import javafx.application.Platform;

/**
 * The only class that touches AWT: an optional system-tray icon with a small
 * menu, plus balloon notifications. Every AWT call happens on the AWT event
 * thread and every callback hops back to the FX thread. Tray support varies by
 * desktop (GNOME needs an extension, macOS ignores balloon messages), so
 * callers must treat everything here as best-effort and keep an in-app
 * fallback.
 */
public final class TrayIntegration {

    private volatile TrayIcon trayIcon;

    /** True when a system tray can be used here; never throws. */
    public static boolean isAvailable() {
        try {
            return !GraphicsEnvironment.isHeadless() && SystemTray.isSupported();
        } catch (RuntimeException | Error e) {
            return false;
        }
    }

    /**
     * Add the tray icon and menu. Callbacks run on the FX thread.
     */
    public void install(Runnable openApp, Runnable newEntryToday, Runnable quit) {
        System.setProperty("java.awt.headless", "false");
        EventQueue.invokeLater(() -> {
            try {
                PopupMenu menu = new PopupMenu();
                menu.add(menuItem("Open ScribeDay", openApp));
                menu.add(menuItem("New Entry (Today)", newEntryToday));
                menu.addSeparator();
                menu.add(menuItem("Quit", quit));

                TrayIcon icon = new TrayIcon(loadIcon(), "ScribeDay", menu);
                icon.setImageAutoSize(true);
                icon.addActionListener(e -> Platform.runLater(openApp));
                SystemTray.getSystemTray().add(icon);
                trayIcon = icon;
            } catch (Exception e) {
                trayIcon = null;   // tray refused (e.g. GNOME without AppIndicator): fall back silently
            }
        });
    }

    /** Remove the tray icon (no-op when it never installed). */
    public void uninstall() {
        TrayIcon icon = trayIcon;
        trayIcon = null;
        if (icon != null) {
            EventQueue.invokeLater(() -> SystemTray.getSystemTray().remove(icon));
        }
    }

    /**
     * Show a balloon notification. Returns false when the platform cannot show
     * one (no icon installed, or macOS where displayMessage is a no-op) so the
     * caller can use its in-app fallback.
     */
    public boolean notify(String title, String message) {
        TrayIcon icon = trayIcon;
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (icon == null || os.contains("mac") || os.contains("darwin")) {
            return false;
        }
        EventQueue.invokeLater(() -> icon.displayMessage(title, message, TrayIcon.MessageType.NONE));
        return true;
    }

    private static MenuItem menuItem(String label, Runnable action) {
        MenuItem item = new MenuItem(label);
        item.addActionListener(e -> Platform.runLater(action));
        return item;
    }

    private static Image loadIcon() {
        URL url = TrayIntegration.class.getResource("/in/systemhalted/scribeday/tray-icon.png");
        return Toolkit.getDefaultToolkit().getImage(url);
    }
}
