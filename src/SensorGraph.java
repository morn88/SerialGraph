import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

/**
 * Created by m0rnAinu on 23.09.2016.
 * SensorGraph. If dont use Sensor, graph draw volt value from analog pin from Arduino.
 * from youtube video - https://www.youtube.com/watch?v=cw31L_OwX3A&t=2449s
 * Autor - upgrdman
 */

public class SensorGraph {
    static SerialPort chosenPort;

    public static void main(String[] args) {
        //create and configure window
        JFrame window = new JFrame();
        window.setTitle("Sensor Graph GUI");
        window.setSize(600, 400);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create a drop-down box and connect button, then place them at the top of the window
        JComboBox<String> portLists = new JComboBox<String>();
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();
        topPanel.add(portLists);
        topPanel.add(connectButton);
        window.add(topPanel, BorderLayout.NORTH);

        //populate the drop-down box
        SerialPort[] portNames = SerialPort.getCommPorts();
        for (SerialPort portName : portNames) {
            portLists.addItem(portName.getSystemPortName());
        }

        //create the line graph
        XYSeries series = new XYSeries("Arduino Sensor Readings");
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart("Arduino Analog Readings", "Time (seconds)", "Volts", dataset);
        window.add(new ChartPanel(chart), BorderLayout.CENTER);


        // configure the connect button and use another thread to listen for data
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(connectButton.getText().equals("Connect")){
                    //attempt to connect to the serial port
                    chosenPort = SerialPort.getCommPort(portLists.getSelectedItem().toString());
                    chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                    if (chosenPort.openPort()){
                        connectButton.setText("Disconnect");
                        portLists.setEnabled(false);
                    }

                    //create a new thread that listens for incoming text and populates the graph
                    Thread thread = new Thread(){
                        @Override
                        public void run(){
                            Scanner scanner = new Scanner(chosenPort.getInputStream());
                            int x = 0;
                            while (scanner.hasNextLine()) {
                                String line = scanner.nextLine();
                                int number = Integer.parseInt(line);
                                series.add(x++, number);
                            }
                            scanner.close();
                        }
                    };
                    thread.start();
                }
                else{
                    //disconnect to the serial port
                    chosenPort.closePort();
                    portLists.setEnabled(true);
                    connectButton.setText("Connect");

                }
            }
        });

        //show the window
        window.setVisible(true);
    }
}