package ro.paha.serialtools;


import com.fazecast.jSerialComm.SerialPort;
import ro.paha.serialtools.delimiter.Delimiter;
import ro.paha.serialtools.repository.Repository;
import ro.paha.serialtools.view.PortOpenException;

import java.util.ArrayList;


public class Connector {

    private ArrayList<Port> connections = new ArrayList<>();

    public Port[] getCommPorts() {
        SerialPort[] SystemPorts = SerialPort.getCommPorts();
        Port[] ports = new Port[SystemPorts.length];
        for (int i = 0; i < SystemPorts.length; i++) {
            ports[i] = new Port(
                    SystemPorts[i].getSystemPortName(),
                    SystemPorts[i].getDescriptivePortName(),
                    SystemPorts[i].getPortDescription()
            );
        }

        return ports;
    }

    public void connectToPort(Port port, Repository repository, Delimiter delimiterSequence) throws PortOpenException {
        SerialPort comPort = SerialPort.getCommPort(port.getId());
        comPort.setComPortParameters(
                port.getBaudRate(),
                port.getDataBits(),
                port.getStopBits(),
                port.getParity()
        );
        comPort.setFlowControl(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);
        if (comPort.openPort()) {
            // each port will have it's own listener
            DataReceivedListener listener = new DataReceivedListener(
                    delimiterSequence,
                    repository
            );
            comPort.addDataListener(listener);
            port.setComPort(comPort);
            connections.add(port);
        } else {
            throw new PortOpenException("Error opening " + port.getName() + ".(Port busy)");
        }

    }

    public void closePort(Port port) {
        SerialPort comPort = port.getComPort();
        comPort.removeDataListener();
        comPort.closePort();
        connections.remove(port);
    }

    public void closeAll() {
        connections.forEach((port) -> {
            SerialPort comPort = SerialPort.getCommPort(port.getId());
            comPort.removeDataListener();
            comPort.closePort();
            connections.remove(port);
        });
    }
}