package com.mycompany.fxmltableview.gui;

/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import javafx.css.StyleableBooleanProperty;
import javafx.css.CssMetaData;

import com.sun.javafx.css.converters.BooleanConverter;

import java.util.*;

import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.ClosePath;


/**
 * Line Chart plots a line connecting the data points in a series. The data points
 * themselves can be represented by symbols optionally. Line charts are usually used
 * to view data trends over time or category. 
 * @since JavaFX 2.0
 */
public class AreaChartnoSymbol<X,Y> extends AreaChart<X,Y> {

    // -------------- PRIVATE FIELDS ------------------------------------------

    /** A multiplier for the Y values that we store for each series, it is used to animate in a new series */
    private Map<XYChart.Series<X,Y>, DoubleProperty> seriesYMultiplierMap = new HashMap<>();
    private Legend legend = new Legend();
    private Timeline dataRemoveTimeline;
    private XYChart.Series<X,Y> seriesOfDataRemoved = null;
    private XYChart.Data<X,Y> dataItemBeingRemoved = null;
    private FadeTransition fadeSymbolTransition = null;
    private Map<XYChart.Data<X,Y>, Double> XYValueMap = 
                                new HashMap<XYChart.Data<X,Y>, Double>();
    private Timeline seriesRemoveTimeline = null;
    
   private BooleanProperty createSymbols = new StyleableBooleanProperty(true) {
        @Override protected void invalidated() {
            for (int seriesIndex=0; seriesIndex < getData().size(); seriesIndex ++) {
                Series<X,Y> series = getData().get(seriesIndex);
                for (int itemIndex=0; itemIndex < series.getData().size(); itemIndex ++) {
                    Data<X,Y> item = series.getData().get(itemIndex);
                    Node symbol = item.getNode();
                    if(get() && symbol == null) { // create any symbols
                        symbol = createSymbol(series, getData().indexOf(series), item, itemIndex);
                        if (null != symbol) {
                            getPlotChildren().add(symbol);
                        }
                    } else if (!get() && symbol != null) { // remove symbols
                        getPlotChildren().remove(symbol);
                        symbol = null;
                        item.setNode(null);
                    }
                }
            }
            requestChartLayout();
        }

        public Object getBean() {
            return this;
        }

        public String getName() {
            return "createSymbols";
        }

        public CssMetaData<AreaChartnoSymbol<?, ?>,Boolean> getCssMetaData() {
            return AreaChartnoSymbol.StyleableProperties.CREATE_SYMBOLS;
        }
    };
    // -------------- PUBLIC PROPERTIES ----------------------------------------

    public AreaChartnoSymbol(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
        System.out.println("new chart");
    }

      private Node createSymbol(Series<X,Y> series, int seriesIndex, final Data<X,Y> item, int itemIndex) {
        Node symbol = item.getNode();
        return symbol;
    }
      
        @Override protected void dataItemAdded(Series<X,Y> series, int itemIndex, Data<X,Y> item) {
        final Node symbol = createSymbol(series, getData().indexOf(series), item, itemIndex);
        if (shouldAnimate()) {
            boolean animate = false;
            if (itemIndex > 0 && itemIndex < (series.getData().size()-1)) {
                animate = true;
                Data<X,Y> p1 = series.getData().get(itemIndex - 1);
                Data<X,Y> p2 = series.getData().get(itemIndex + 1);
                double x1 = getXAxis().toNumericValue(p1.getXValue());
                double y1 = getYAxis().toNumericValue(p1.getYValue());
                double x3 = getXAxis().toNumericValue(p2.getXValue());
                double y3 = getYAxis().toNumericValue(p2.getYValue());
                
                double x2 = getXAxis().toNumericValue(item.getXValue());
                double y2 = getYAxis().toNumericValue(item.getYValue());
      
//                //1. y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1)
                double y = ((y3-y1)/(x3-x1)) * x2 + (x3*y1 - y3*x1)/(x3-x1);
                
                //2. we can simply use the midpoint on the line as well..
//                double x = (x3 + x1)/2;
//                double y = (y3 + y1)/2;
//                item.setCurrentX(x);
//                item.setCurrentY(y);
            } else if (itemIndex == 0 && series.getData().size() > 1) {
                animate = true;
               
            } else if (itemIndex == (series.getData().size() - 1) && series.getData().size() > 1) {
                animate = true;
                int last = series.getData().size() - 2;
                
            }  
            if (symbol != null) {
                // fade in new symbol
                symbol.setOpacity(0);
                getPlotChildren().add(symbol);
                FadeTransition ft = new FadeTransition(Duration.millis(500),symbol);
                ft.setToValue(1);
                ft.play();
            }
            if (animate) {
            }
            
        } else if (symbol != null) {
            getPlotChildren().add(symbol);
        }
    }
      @Override protected  void seriesAdded(Series<X,Y> series, int seriesIndex) {
        // create new paths for series
        Path seriesLine = new Path();
        Path fillPath = new Path();
        seriesLine.setStrokeLineJoin(StrokeLineJoin.BEVEL);
        Group areaGroup = new Group(fillPath,seriesLine);
        series.setNode(areaGroup);
        // create series Y multiplier
        DoubleProperty seriesYAnimMultiplier = new SimpleDoubleProperty(this, "seriesYMultiplier");
        seriesYMultiplierMap.put(series, seriesYAnimMultiplier);
        // handle any data already in series
        if (shouldAnimate()) {
            seriesYAnimMultiplier.setValue(0d);
        } else {
            seriesYAnimMultiplier.setValue(1d);
        }
        getPlotChildren().add(areaGroup);
        List<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
        if (shouldAnimate()) {
            // animate in new series
            keyFrames.add(new KeyFrame(Duration.ZERO,
                new KeyValue(areaGroup.opacityProperty(), 0),
                new KeyValue(seriesYAnimMultiplier, 0)
            ));
            keyFrames.add(new KeyFrame(Duration.millis(200),
               new KeyValue(areaGroup.opacityProperty(), 1)
            ));
            keyFrames.add(new KeyFrame(Duration.millis(500),
                new KeyValue(seriesYAnimMultiplier, 1)
            ));
        }
        for (int j=0; j<series.getData().size(); j++) {
            Data<X,Y> item = series.getData().get(j);
            final Node symbol = createSymbol(series, seriesIndex, item, j);
            if (symbol != null) {
                if (shouldAnimate()) {
                    symbol.setOpacity(0);
                    getPlotChildren().add(symbol);
                    // fade in new symbol
                    keyFrames.add(new KeyFrame(Duration.ZERO, new KeyValue(symbol.opacityProperty(), 0)));
                    keyFrames.add(new KeyFrame(Duration.millis(200), new KeyValue(symbol.opacityProperty(), 1)));
                }
                else {
                    getPlotChildren().add(symbol);
                }
            }
        }
       
    }
       
       final int getDataSize() {
        final ObservableList<Series<X,Y>> data = getData();
        return (data!=null) ? data.size() : 0;
    }
       
       /** @inheritDoc */
    @Override protected void layoutPlotChildren() {
        List<LineTo> constructedPath = new ArrayList<>(getDataSize());
        for (int seriesIndex=0; seriesIndex < getDataSize(); seriesIndex++) {
            Series<X, Y> series = getData().get(seriesIndex);
            DoubleProperty seriesYAnimMultiplier = seriesYMultiplierMap.get(series);
            double lastX = 0;
            final ObservableList<Node> children = ((Group) series.getNode()).getChildren();
            ObservableList<PathElement> seriesLine = ((Path) children.get(1)).getElements();
            ObservableList<PathElement> fillPath = ((Path) children.get(0)).getElements();
            seriesLine.clear();
            fillPath.clear();
            constructedPath.clear();
            for (Iterator<Data<X, Y>> it = getDisplayedDataIterator(series); it.hasNext(); ) {
                Data<X, Y> item = it.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(
                        getYAxis().toRealValue(getYAxis().toNumericValue(item.getYValue()) * seriesYAnimMultiplier.getValue()));
                constructedPath.add(new LineTo(x, y));
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                lastX = x;
                Node symbol = item.getNode();
                if (symbol != null) {
                    final double w = symbol.prefWidth(-1);
                    final double h = symbol.prefHeight(-1);
                    symbol.resizeRelocate(x-(w/2), y-(h/2),w,h);
                }
            }

            if (!constructedPath.isEmpty()) {
                Collections.sort(constructedPath, (e1, e2) -> Double.compare(e1.getX(), e2.getX()));
                LineTo first = constructedPath.get(0);

                final double displayYPos = first.getY();
                final double numericYPos = getYAxis().toNumericValue(getYAxis().getValueForDisplay(displayYPos));

                // RT-34626: We can't always use getZeroPosition(), as it may be the case
                // that the zero position of the y-axis is not visible on the chart. In these
                // cases, we need to use the height between the point and the y-axis line.
                final double yAxisZeroPos = getYAxis().getZeroPosition();
                final boolean isYAxisZeroPosVisible = !Double.isNaN(yAxisZeroPos);
                final double yAxisHeight = getYAxis().getHeight();
                final double yFillPos = isYAxisZeroPosVisible ? yAxisZeroPos :
                                        numericYPos < 0 ? numericYPos - yAxisHeight : yAxisHeight;

                seriesLine.add(new MoveTo(first.getX(), displayYPos));
                fillPath.add(new MoveTo(first.getX(), yFillPos));

                seriesLine.addAll(constructedPath);
                fillPath.addAll(constructedPath);
                fillPath.add(new LineTo(lastX, yFillPos));
                fillPath.add(new ClosePath());
            }
        }
    }
    private static class StyleableProperties {
        private static final CssMetaData<AreaChartnoSymbol<?,?>,Boolean> CREATE_SYMBOLS = 
            new CssMetaData<AreaChartnoSymbol<?,?>,Boolean>("-fx-create-symbols",
                BooleanConverter.getInstance(), Boolean.TRUE) {

            @Override
            public boolean isSettable(AreaChartnoSymbol<?,?> node) {
                return node.createSymbols == null || !node.createSymbols.isBound();
}

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(AreaChartnoSymbol<?,?> node) {
                return (StyleableProperty<Boolean>)node.createSymbolsProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<CssMetaData<? extends Styleable, ?>>(XYChart.getClassCssMetaData());
            styleables.add(CREATE_SYMBOLS);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}

