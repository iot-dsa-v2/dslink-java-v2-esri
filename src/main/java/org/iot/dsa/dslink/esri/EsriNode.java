package org.iot.dsa.dslink.esri;

import java.util.HashSet;
import java.util.Set;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;

public class EsriNode extends RemovableNode {
    
    private WebApiNode apiNode;
    
    protected void putListNode(String key, DSList l) {
        DSIObject obj = get(key);
        EsriNode listNode;
        if (obj instanceof EsriNode) {
            listNode = (EsriNode) obj;
        } else {
            listNode = new EsriNode();
        }
        
        put(key, listNode);

        Set<String> toRemove = listNode.getRemoveSet();
        boolean hasMaps = false;
        boolean hasPrimitives = false;
        for (DSElement elem : l) {
            if (elem.isList()) {
                warn("List within a list, this is unexpected");
            } else if (elem.isMap()) {
                hasMaps = true;
                DSMap m = elem.toMap();
                String name = m.getString("name");
                if (name == null) {
                    name = m.getString("label");
                }
                if (name == null) {
                    warn("Nameless map within a list, this is unexpected");
                } else {
                    listNode.putMapNode(name, m);
                }
                toRemove.remove(name);
            } else {
                hasPrimitives = true;
            }
        }
        
        if (hasPrimitives) {
            if (hasMaps) {
                warn("List contains both maps and primitives, this is unexpected");
            } else {
                put(key, l.copy());
                return;
            }
        }
        for (String name: toRemove) {
            listNode.remove(name);
        }
    }
    
    protected void putMapNode(String key, DSMap m) {
        DSIObject obj = get(key);
        String id = m.getString("id");
        EsriNode mapNode;
        if (id != null) {
            WebClientProxy clientProxy = getApiNode().getClientProxy();
            String address = clientProxy.base;
            if (!address.endsWith("/")) {
                address += "/";
            }
            address += id;
            if (obj instanceof WebApiNode) {
                mapNode = (WebApiNode) obj;
                ((WebApiNode) mapNode).setAddress(address);
            } else {
                mapNode = new WebApiNode(address, clientProxy);
            }
        } else {
            if (obj instanceof EsriNode) {
                mapNode = (EsriNode) obj;
            } else {
                mapNode = new EsriNode();
            }
        }
        
        put(key, mapNode);
        
        Set<String> toRemove = id == null ? mapNode.getRemoveSet() : new HashSet<String>();
        for (Entry entry: m) {
            String name = entry.getKey();
            DSElement value = entry.getValue();
            if (value.isList()) {
                DSList l = value.toList();
                mapNode.putListNode(name, l);
            } else if (value.isMap()) {
                DSMap inner = value.toMap();
                mapNode.putMapNode(name, inner);
            } else {
                mapNode.put(name, value.copy()).setReadOnly(true);
            }
            toRemove.remove(name);
        }
        
        for (String name: toRemove) {
            mapNode.remove(name);
        }
    }
    
    protected Set<String> getRemoveSet() {
        Set<String> toRemove = new HashSet<String>();
        for (DSInfo info: this) {
            if (!info.isAction()) {
                String name = info.getName();
                if (!(name.equals("Address") || name.equals("Username") || name.equals("Password"))) {
                    toRemove.add(name);
                }
            }
        }
        return toRemove;
    }
    
    private WebApiNode getApiNode() {
        if (apiNode == null) {
            if (this instanceof WebApiNode) {
                apiNode = (WebApiNode) this;
            } else {
                apiNode = (WebApiNode) getAncestor(WebApiNode.class);
            }
        }
        return apiNode;
    }

}
