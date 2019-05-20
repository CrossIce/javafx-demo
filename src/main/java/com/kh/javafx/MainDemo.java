package com.kh.javafx;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;

import com.kh.javafx.gui.main.MainController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainDemo extends Application {

    @FXMLViewFlowContext
    private ViewFlowContext flowContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        Flow flow = new Flow(MainController.class);
        DefaultFlowContainer container = new DefaultFlowContainer();
        flowContext = new ViewFlowContext();
        flowContext.register("Stage", stage);
        flow.createHandler(flowContext).start(container);

        JFXDecorator decorator = new JFXDecorator(stage, container.getView(),false,true,true);
        decorator.setCustomMaximize(true);
        decorator.setGraphic(new SVGGlyph(""));

        stage.setTitle("报价导出excel");
        stage.setFullScreenExitKeyCombination(null);

        double width = 1000;
        double height = 700;
        try {
            Rectangle2D bounds = Screen.getScreens().get(0).getBounds();
            width = bounds.getWidth() / 1.6;
            height = bounds.getHeight() / 1.15;
        }catch (Exception e){ }

        Scene scene = new Scene(decorator, width, height);
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(MainDemo.class.getResource("/css/jfoenix-fonts.css").toExternalForm(),
                           MainDemo.class.getResource("/css/jfoenix-design.css").toExternalForm(),
                           MainDemo.class.getResource("/css/jfoenix-main-demo.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

}
