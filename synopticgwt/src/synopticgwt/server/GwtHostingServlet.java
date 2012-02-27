package synopticgwt.server;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Retrieve the configuration.
        ServletContext context = getServletConfig().getServletContext();
        try {
            this.config = AppConfiguration.getInstance(context);
        } catch (Exception e) {
            logger.severe("AppConfiguration generated an exception: "
                    + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error during AppConfiguration construction.");
            return;
        }

        // Make certain configuration parameters accessible from within JSP.
        HttpSession session = req.getSession();
        session.setAttribute("analyticsTrackerID", config.analyticsTrackerID);
        session.setAttribute("userVoiceEnabled", config.userVoiceEnabled);
        session.setAttribute("synopticGWTChangesetID",
                config.synopticGWTChangesetID);
        session.setAttribute("synopticChangesetID", config.synopticChangesetID);

        // Insert visitor's information into DerbyDB and set vID value if vID is
        // null.
        if (session.getAttribute("vID") == null && config.derbyDB != null) {
            String ipAddress = req.getRemoteAddr();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            String q = "insert into Visitor(IP, timestamp) values('"
                    + ipAddress + "', '" + now + "')";
            int n;
            try {
                n = config.derbyDB.insertAndGetAutoValue(q);
            } catch (SQLException e) {
                logger.severe("DerbyDB generated an exception: "
                        + e.getMessage());
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Database insertion error.");
                return;
            }
            session.setAttribute("vID", n);
        }

        // Forward to the main JSP page for content synthesis.
        try {
            req.getRequestDispatcher("SynopticGWT.jsp").forward(req, resp);
        } catch (Exception e) {
            logger.severe("Dispatching to SynopticGWT.jsp failed: "
                    + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error dispatching to SynopticGWT.jsp.");
            return;
        }
    }
}