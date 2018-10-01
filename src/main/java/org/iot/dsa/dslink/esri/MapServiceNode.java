package org.iot.dsa.dslink.esri;

import java.util.List;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import com.esri.arcgisruntime.arcgisservices.ArcGISMapServiceInfo;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISSublayer;
import com.esri.arcgisruntime.layers.SublayerList;
import com.esri.arcgisruntime.loadable.LoadStatus;

public class MapServiceNode extends RemovableNode implements Runnable{
    
    private ArcGISMapImageLayer mapService;
    private String address;
    private DSInfo layersInfo = getInfo("Layers");
    private DSInfo tablesInfo = getInfo("Tables");
    
    public MapServiceNode() {
    }
    
    public MapServiceNode(String address) {
        this.address = address;
    }
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Refresh", makeRefreshAction());
        declareDefault("Layers", new DSNode());
        declareDefault("Tables", new DSNode());
    }
    
    private DSAction makeRefreshAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((MapServiceNode) info.getParent()).init();
                return null;
            }
        };
        return act;
    }
    
    @Override
    protected void onStarted() {
        if (address == null) {
            DSIObject adr = get("Address");
            address = adr instanceof DSString ? ((DSString) adr).toString() : "";
        }
    }
    
    @Override
    protected void onStable() {
        init();
    }
    
    private void init() {
        put("Address", DSString.valueOf(address)).setReadOnly(true).setHidden(true);
        layersInfo.getNode().clear();
        tablesInfo.getNode().clear();
        mapService = new ArcGISMapImageLayer(address);
        mapService.addDoneLoadingListener(this);
        mapService.loadAsync();
    }

    @Override
    public void run() {
        if (mapService.getLoadStatus() == LoadStatus.LOADED) {
            updateServiceInfo(mapService.getMapServiceInfo());
            updateLayers(mapService.getSublayers());
            updateTables(mapService.getTables());
        }
    }
    
    private void updateLayers(SublayerList layers) {
        for (ArcGISSublayer layer : layers) {
            layersInfo.getNode().put(layer.getName(), new LayerNode(layer));
        }
    }
    
    private void updateTables(List<ServiceFeatureTable> tables) {
        
    }
    
    private void updateServiceInfo(ArcGISMapServiceInfo serviceInfo) {
        
    }
    

}
