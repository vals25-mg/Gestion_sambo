<%@page import="facture.Facture" %>
<%@page import="prevision.Escale" %>
<%

    Facture facture = new Facture();
    Escale escale = new Escale();
    try {
        escale = Escale.createEscale(request.getParameter("reference"));
        facture = escale.facturer();
        facture.insert(null);
    } catch (Exception e) {
        response.sendRedirect("../escale/ajout-prestation.jsp?error=" + e.getMessage() + "&&reference=" + request.getParameter("reference"));
    }

%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge"> 
    <link rel="stylesheet" href="../assets/css/bootstrap.css">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Facture</title>
</head>
<body>
    
    <div class="container w-50 shadow p-5 rounded-3 mb-5" style="margin-top: 5rem;">
        <h1>Facture</h1>
        <table class="table">
            <thead>
              <tr>
                <th scope="col">Prestation</th>
                <th scope="col">Quaie</th>
                <th scope="col">Montant</th>
              </tr>
            </thead>
            <tbody>
                <% for (Facture detail : facture.getFactures()) { %>
                        <tr>
                        <td><%=detail.getPrestation().getNom() %></td>
                        <td><%=detail.getPrestation().getEscale().getQuai().getNom() %></td>
                        <td><%=detail.getPrestation().getPrix() %> Ariary</td>
                        </tr>
                <% } %>
            </tbody>
          </table>
          <a href="./formulaire-fin-escale.jsp?facture=<%=facture.getId() %>&&reference=<%=escale.getReference() %>"><button type="button" class="btn btn-primary">Valider</button></a>
    </div>
</body>
</html>