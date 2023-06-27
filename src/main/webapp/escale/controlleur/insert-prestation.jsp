<%@page import="port.Quai" %>
<%@page import="prevision.Escale"%>
<%@page import="prevision.Prestation"%>
<%@ page import="prevision.Approvisionnement" %>
<%
    
    try {
        Prestation prestation = new Prestation(request.getParameter("prestation"));
        Escale escale = Escale.createEscale(request.getParameter("quai"), request.getParameter("reference"));
        if(prestation.getIdPrestation().equals("PRES003")) {
            prestation = new Approvisionnement();
            prestation.setIdPrestation("PRES003");
            ((Approvisionnement)prestation).setLitre(request.getParameter("quantite"));
            escale.now();
        }
        else{
        escale.setArrive(escale.toDate(request.getParameter("arrive")));
        escale.setDepart(escale.toDate(request.getParameter("depart")));
        }
        escale.ajouterPrestation(prestation);
        response.sendRedirect("../ajout-prestation.jsp?reference=" + request.getParameter("reference") + "&&quai=" + request.getParameter("quai"));
    } catch (Exception e) {
        response.sendRedirect("../ajout-prestation.jsp?reference=" + request.getParameter("reference") + "&&error=" + e.getMessage() + "&&quai=" + request.getParameter("quai"));
    }

%>
