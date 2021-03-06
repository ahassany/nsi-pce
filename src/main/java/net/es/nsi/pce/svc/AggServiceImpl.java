package net.es.nsi.pce.svc;


import net.es.nsi.pce.svc.api.AggService;
import net.es.nsi.pce.svc.api.FindPathResponse;
import net.es.nsi.pce.svc.api.PathObject;

import javax.ws.rs.core.Response;

public class AggServiceImpl implements AggService {

    public Response pathReply(FindPathResponse response) {
        System.out.println("----invoking pathReply, correlation id is: " + response.correlationId);

        for (PathObject po : response.path) {
            System.out.println(po.sourceStp.localId + " -- "+ po.destinationStp.localId);
        }

        Response r;
        if (1 != 0) {
            r = Response.ok().build();
        } else {
            r = Response.notModified().build();
        }

        return r;
    }

}