package org.iot.dsa.dslink.esri;

import com.esri.arcgisruntime.layers.ArcGISSublayer;

public class LayerNode extends RemovableNode implements Runnable {
    
    ArcGISSublayer layer;
    
    public LayerNode() {
    }
    
    public LayerNode(ArcGISSublayer layer) {
        this.layer = layer;
    }
    
    @Override
    public void onStarted() {
    }
    
    @Override
    public void onStable() {
        init();
    }
    
    private void init() {
        layer.addDoneLoadingListener(this);
        layer.loadAsync();
    }

    @Override
    public void run() {
    }

}
