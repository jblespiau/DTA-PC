package optimization;

import graphics.Plots;
import io.InputOutput;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import dta_solver.adjointMethod.GradientDescentOptimizer;

public class GradientDescent extends GradientDescentMethod {

  /* The stopping criteria is || dJ/dx ||_2 < gradient_condition */
  private double gradient_condition = 10E-5;

  /* Table of the cost for all the iterations of the descent for the a graph */
  private double[] TTT;

  public GradientDescent() {
    super();
    lineSearch = new BackTrackingLineSearch();
  }

  public GradientDescent(int maxIterations) {
    super(maxIterations);
    lineSearch = new BackTrackingLineSearch();
  }

  @Override
  public double[] solve(GradientDescentOptimizer function) {
    double[] control = function.getStartingPoint();
    double[] gradient = new double[control.length];
    TTT = new double[maxIterations + 1];

    System.out.println(
        "\n***************************\n" +
            " Gradient descent launched \n" +
            "***************************\n");
    for (int iteration = 1; iteration <= maxIterations; iteration++) {
      double cost = function.objective(control);
      TTT[iteration - 1] = cost;

      if (verbose) {
        System.out.print("Iteration " + iteration + " | Cost: "
            + cost + "\n");
      }

      /* Line search */
      /* Update x = x * t * delta_x; and J(x) */
      function.gradient(gradient, control);
      control = lineSearch.lineSearch(control, gradient, function);

      /* Stopping condition */
      if (stoppingTest(gradient)) {
        System.out
            .println("Stopping gradient descent because of nearly null gradient");
        for (int i = 0; i < gradient.length; i++)
          System.out.println(gradient[i]);
        System.out.println("If the gradient should not be null, it is" +
            "very likely that the number of time steps is not large" +
            "enough to allow all the vehicles to leave the network. \n" +
            "Try increasing the number of time steps");
        break;
      }
    }

    TTT[maxIterations] = function.objective(control);

    return control;
  }

  /**
   * @brief We stop if the 2 norm of the gradient is smaller than
   *        gradient_condition
   */
  private boolean stoppingTest(double[] gradient) {
    double tmp = 0;
    for (int i = 0; i < gradient.length; i++) {
      tmp += gradient[i] * gradient[i];
    }
    if (tmp < gradient_condition)
      System.out.println("Gradient condition: " + tmp);
    return tmp < gradient_condition;
  }

  public double getGradient_condition() {
    return gradient_condition;
  }

  public void setGradient_condition(double gradient_condition) {
    this.gradient_condition = gradient_condition;
  }

  public JFreeChart getChart() {
    if (TTT == null)
      return null;

    XYSeriesCollection dataset = new XYSeriesCollection();
    XYSeries series;
    series = new XYSeries("Total travel time"); // "Total travel time"
    for (int k = 0; k < TTT.length; k++) {
      series.add(k, TTT[k]);
    }
    dataset.addSeries(series);

    JFreeChart chartTT = ChartFactory.createXYLineChart(null, // title
        "Iterations", // x axis label
        "Total Travel Time", // y axis label
        dataset, // data
        PlotOrientation.VERTICAL, false, // include legend
        true, // tooltips
        false // urls
        );
    XYPlot plotTT = (XYPlot) chartTT.getPlot();
    ValueAxis yAxis = plotTT.getRangeAxis();
    yAxis.setRange(7, 20);

    plotTT.setBackgroundPaint(Color.white);
    plotTT.setDomainGridlinePaint(Color.lightGray);
    plotTT.setRangeGridlinePaint(Color.lightGray);
    InputOutput.writeChartAsPDF("GradientDescent.pdf", chartTT, 500, 280);

    return chartTT;
  }
}