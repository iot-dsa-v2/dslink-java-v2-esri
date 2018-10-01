package org.iot.dsa.dslink.esri;

import java.util.List;

import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalInfo;

/**
 * The main and only node of this link.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode implements Runnable {

	private Portal portal;
	private ArcGISMapImageLayer agmil;
   
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
    
    @Override
    protected void onStable() {
    	String url = "https://services8.arcgis.com/WcAJBZ4RHvSFpfLo/ArcGIS/rest/services/Mobile_Map_Export___UC_2018_WFL1/FeatureServer";
//    	portal = new Portal(url);
//    	portal.addDoneLoadingListener(this);
//    	portal.loadAsync();
    	agmil = new ArcGISMapImageLayer(url);
    	agmil.addDoneLoadingListener(this);
    	agmil.loadAsync();
    }

	@Override
	public void run() {
		LoadStatus ls = agmil.getLoadStatus();
		List<ServiceFeatureTable> tables = agmil.getTables();
		for (final ServiceFeatureTable table: tables) {
			table.addDoneLoadingListener(new Runnable() {

				@Override
				public void run() {
					info(table.getDisplayName());
				}
				
			});
			table.loadAsync();
		}
	}

}
