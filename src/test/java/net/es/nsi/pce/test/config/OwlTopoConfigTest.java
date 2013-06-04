package net.es.nsi.pce.test.config;

import net.es.nsi.pce.config.topo.OwlTopoConfigProvider;
import net.es.nsi.pce.pf.api.topo.*;
import org.testng.annotations.Test;

public class OwlTopoConfigTest {
    @Test (groups = "config")
    public void testTopoLoad() throws Exception {
        System.out.println("testing OWL topology config");


        OwlTopoConfigProvider prov = OwlTopoConfigProvider.getInstance();
        prov.setFilename("src/test/resources/config/topo.owl");
        try {
            prov.loadConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (String networkId: prov.getNetworkIds()) {
            System.out.println("loaded config for Network "+networkId);
        }

        OwlTopologyProvider jtp = OwlTopologyProvider.getInstance();
        Topology topo = jtp.getTopology();
        for (String netId : topo.getNetworkIds()) {
            System.out.println(netId);
            Network net = topo.getNetwork(netId);
            for (String stpId : net.getStpIds()) {
                Stp stp = net.getStp(stpId);

                if (net.getConnectionsFrom(stp).isEmpty()) {
                    System.out.println("  "+stp.getLocalId());
                } else {
                    for (StpConnection conn : net.getConnectionsFrom(stp)) {
                        System.out.println("  "+stp.getLocalId()+" -- "+conn.getZ().getLocalId());

                    }
                }
            }
        }


    }

}
