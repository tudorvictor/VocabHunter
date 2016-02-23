/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.main;

import io.github.vocabhunter.gui.controller.MainController;
import io.github.vocabhunter.gui.event.CommandLineEventSource;
import io.github.vocabhunter.gui.event.ExternalEventBroker;
import io.github.vocabhunter.gui.event.ExternalEventSource;
import io.github.vocabhunter.gui.factory.ControllerAndView;
import io.github.vocabhunter.gui.factory.FileDialogueFactory;
import io.github.vocabhunter.gui.factory.GuiFactory;
import io.github.vocabhunter.gui.settings.SettingsManager;
import io.github.vocabhunter.gui.settings.SettingsManagerImpl;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static io.github.vocabhunter.gui.main.ExecutableLogTool.*;
import static java.util.Collections.singletonList;

public class VocabHunterGuiExecutable extends Application {
    private static final double WINDOW_SIZE_FACTOR = 0.80;

    private final SettingsManager settingsManager;

    private final FileDialogueFactory fileDialogueFactory;

    public VocabHunterGuiExecutable() {
        this(new SettingsManagerImpl());
    }

    private VocabHunterGuiExecutable(final SettingsManager settingsManager) {
        this(settingsManager, new FileDialogueFactoryImpl(settingsManager));
    }

    public VocabHunterGuiExecutable(final SettingsManager settingsManager, final FileDialogueFactory fileDialogueFactory) {
        this.settingsManager = settingsManager;
        this.fileDialogueFactory = fileDialogueFactory;
    }

    @Override
    public void start(final Stage stage) {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> logError(e));
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            GuiFactory factory = new GuiFactoryImpl(settingsManager, fileDialogueFactory, stage, ExternalEventBroker.getInstance());
            ControllerAndView<MainController, Parent> cav = factory.mainWindow();
            double width = screenSize.getWidth() * WINDOW_SIZE_FACTOR;
            double height = screenSize.getHeight() * WINDOW_SIZE_FACTOR;
            Scene scene = new Scene(cav.getView(), width, height);
            MainController controller = cav.getController();

            scene.setOnKeyPressed(controller.getKeyPressHandler());
            stage.setOnCloseRequest(controller.getCloseRequestHandler());
            stage.setScene(scene);
            stage.show();
        } catch (final RuntimeException e) {
            logError(e);
        }
    }

    public static void main(final String... args) {
        CommandLineEventSource eventSource = new CommandLineEventSource(args);

        runApp(args, singletonList(eventSource), a -> launch(a));
    }

    public static void runApp(final String[] args, final List<ExternalEventSource> eventSources, final Consumer<String[]> launcher) {
        logStartup();
        try {
            logSystemDetails();
            eventSources.forEach(s -> s.setListener(ExternalEventBroker.getInstance()));
            launcher.accept(args);
        } finally {
            logShutdown();
        }
    }
}
