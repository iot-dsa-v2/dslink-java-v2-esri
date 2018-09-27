package org.iot.dsa.dslink.esri;

import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * The main and only node of this link.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode {

   
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        // Change the following URL to your README
        declareDefault("Docs",
                       DSString.valueOf("https://github.com/iot-dsa-v2/dslink-java-v2-esri"))
                .setTransient(true)
                .setReadOnly(true);
        declareDefault("Add ESRI Endpoint", makeAddEndpointAction());
    }

    private static DSAction makeAddEndpointAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((MainNode) info.getParent()).addEndpoint(invocation.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSValueType.STRING, null);
        act.addParameter("Address", DSValueType.STRING, null);
        return act;
    }
    
    private void addEndpoint(DSMap parameters) {
        String name = parameters.getString("Name");
        String addr = parameters.getString("Address");
        WebClientProxy clientProxy = new WebClientProxy(addr);
        WebApiNode n = new WebApiNode(addr, clientProxy, true);
        put(name, n);
    }

}
