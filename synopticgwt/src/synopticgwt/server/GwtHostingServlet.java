package synopticgwt.server;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet serving the main page of the Synoptic web app.
 */
public class GwtHostingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static Logger logger = Logger.getLogger("GwtHostingServlet");

    AppConfiguration config;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // Retrieve the configuration.
        ServletContext context = getServletConfig().getServletContext();
        this.config = AppConfiguration.getInstance(context);
      
        // Make certain configuration parameters accessible from within JSP.
        HttpSession session = req.getSession();
        session.setAttribute("analyticsTrackerID", config.analyticsTrackerID);
        session.setAttribute("synopticGWTChangesetID",
                config.synopticGWTChangesetID);
        session.setAttribute("synopticChangesetID", config.synopticChangesetID);
        
        
        String ipAddress = req.getRemoteAddr();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String q = "insert into Visitor(IP, timestamp) values('" + ipAddress + "', '" + now + "')";
        int n = config.derbyDB.insertAndGetAutoValue(q);
        session.setAttribute("vID", n);

        // Forward to the main JSP page for content synthesis.
        try {
            req.getRequestDispatcher("SynopticGWT.jsp").forward(req, resp);
        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}