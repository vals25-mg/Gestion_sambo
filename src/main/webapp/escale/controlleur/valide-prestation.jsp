<%@page import="port.Quai" %>
<%@page import="prevision.Prestation"%>
<%@page import="utilisateur.Utilisateur"%>
<%
    
    try {
        Prestation prestation = new Prestation();
        prestation.setId(request.getParameter("prestation"));
        prestation.validate((Utilisateur) request.getSession().getAttribute("utilisateur"));
        response.sendRedirect("../ajout-prestation.jsp?reference=" + request.getParameter("reference") + "&&quai=" + request.getParameter("quai"));
    } catch (Exception e) {
        response.sendRedirect("../ajout-prestation.jsp?reference=" + request.getParameter("reference") + "&&quai=" + request.getParameter("quai") + "&&error=" + e.getMessage());
    }

%>
