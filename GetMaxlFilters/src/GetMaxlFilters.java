
import com.essbase.api.base.EssException;
import com.essbase.api.base.IEssIterator;
import com.essbase.api.datasource.IEssCube;
import com.essbase.api.datasource.IEssCube.IEssSecurityFilter;
import com.essbase.api.datasource.IEssOlapApplication;
import com.essbase.api.datasource.IEssOlapServer;
import com.essbase.api.domain.IEssDomain;
import com.essbase.api.session.IEssbase;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * GetMaxlFilters <p>
 *
 * Get and format Essbase filter as a Maxl create-replace statement command
 *
 * @author Sebastien Roux @mail roux.sebastien@gmail.com
 *
 * The MIT License Copyright (c) 2012 Sébastien Roux
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

public class GetMaxlFilters {

    private static String essUsr = "admin";
    private static String essPwd = "password";
    private static String essSvr = "localhost";
    static String essProvider = "http://" + essSvr + ":13080/aps/JAPI";
    private static final int FAILURE_CODE = 1;
    private static String essApp = "";
    private static String essDb = "";
    private static final String NOT_ASSIGNED = "NOT ASSIGNED";

    public static String getEssApp() {
        return essApp;
    }

    public static void setEssApp(String essApp) {
        GetMaxlFilters.essApp = essApp;
    }

    public static String getessDb() {
        return essDb;
    }

    public static void setEssDb(String essDb) {
        GetMaxlFilters.essDb = essDb;
    }

    public static String getEssPwd() {
        return essPwd;
    }

    public static void setEssPwd(String essPwd) {
        GetMaxlFilters.essPwd = essPwd;
    }

    public static String getEssSvr() {
        return essSvr;
    }

    public static void setEssSvr(String essSvr) {
        GetMaxlFilters.essSvr = essSvr;
    }

    public static String getEssUsr() {
        return essUsr;
    }

    public static void setEssUsr(String essUsr) {
        GetMaxlFilters.essUsr = essUsr;
    }

    public static String getEssProvider() {
        return essProvider;
    }

    public static void setEssProvider(String essPwd) {
        if (essProvider == null) {
            // Default provider (APS) path
            GetMaxlFilters.essProvider = "http://" + getEssSvr() + ":13080/aps/JAPI";
        } else {
            GetMaxlFilters.essProvider = essProvider;
        }
    }

    public static void GetMaxlFilters() {
        int sts = 0;
        IEssbase essbase = null;
        IEssOlapServer olapSvr = null;

        try {
            essbase = IEssbase.Home.create(IEssbase.JAPI_VERSION);
            IEssDomain dom = essbase.signOn(essUsr, essPwd, false, null, essProvider);
            olapSvr = (IEssOlapServer) dom.getOlapServer(essSvr);

            olapSvr.connect();

            transformFilters(olapSvr);

        } catch (EssException x) {
            System.out.println("Error " + x.getLocalizedMessage());
            sts = FAILURE_CODE;
        } catch (Exception e) {
            System.out.println("Error " + e.getLocalizedMessage());
        } finally {
            try {
                if (essbase != null && essbase.isSignedOn() == true) {
                    essbase.signOff();
                }
            } catch (EssException x) {
                System.err.println("Error: " + x.getLocalizedMessage());
            }
        }
        if (sts == FAILURE_CODE) {
            System.exit(FAILURE_CODE);
        }
    }

    static void transformFilters(IEssOlapServer olapSvr) {

        try {
            // List applications 
            IEssIterator usrApp = olapSvr.getApplications();

            // Loop through applications 
            for (int i = 0; i < usrApp.getCount(); i++) {
                IEssOlapApplication app = (IEssOlapApplication) usrApp.getAt(i);

                // Match specified application
                boolean checkApp = Pattern.matches(".*" + essApp.toLowerCase() + ".*", usrApp.getAt(i).toString().toLowerCase());

                // For specified application
                if (checkApp == true) {

                    // Get databases for each application 
                    IEssIterator usrDb = app.getCubes();

                    // Loop through each database 
                    for (int j = 0; j < usrDb.getCount(); j++) {

                        // Match specified database
                        boolean checkDb = Pattern.matches(".*" + essDb.toLowerCase() + ".*", usrDb.getAt(j).toString().toLowerCase());

                        // For specified database 
                        if (checkDb == true) {

                            IEssCube db = (IEssCube) usrDb.getAt(j);

                            // List filters for each database 
                            IEssIterator dbFilter = db.getSecurityFilters();

                            // Loop through filters 
                            for (int k = 0; k < dbFilter.getCount(); k++) {

                                //Array to join filter rows
                                ArrayList maxlFilter = new ArrayList();

                                // Get filter content 
                                IEssSecurityFilter filter = (IEssSecurityFilter) dbFilter.getAt(k);
                                IEssCube.IEssSecurityFilter.IEssFilterRow filterRow = filter.getFilterRow();

                                // Print maxl filter "prefix"
                                System.out.println(
                                        "create or replace filter "
                                        + "'" + app.getName() + "'.'"
                                        + db.getName() + "'.'"
                                        + filter.getName() + "'");

                                // Get filter rows
                                while (filterRow.getRowString() != null) {

                                    // Add each filter row into array
                                    maxlFilter.add(" " + getAccessLabel(filterRow.getAccess())
                                            + " on '" + filterRow.getRowString() + "'");

                                    filterRow = filter.getFilterRow();
                                }

                                // Build Maxl filter
                                for (int l = 0; l < maxlFilter.size(); l++) {
                                    if (l < maxlFilter.size()) {
                                        System.out.println(maxlFilter.get(l) + ",");
                                    } else {
                                        System.out.println(maxlFilter.get(l));
                                    }
                                }

                                /*
                                 * Add last option.
                                 * Updates the filter definition while retaining
                                 * user associations with the filter. If you
                                 * replace a filter without using
                                 * definition_only, then the filter must be
                                 * re-granted to any users or group to whom it
                                 * was assigned (ref. Essbase technical reference).
                                 */
                                System.out.println(" definition_only;\n");
                            }
                        }
                    }
                }
            }
        } catch (Exception x) {
            System.out.println("Error " + x.getLocalizedMessage());
        }
    }

    // Access level translation 
    static String getAccessLabel(short accessID) {
        String access = "";

        switch (accessID) {
            case 0:
                access = "no_access";
                break;
            case 273:
                access = "read";
                break;
            case 275:
                access = "write";
                break;
            case 280:
                access = "metaread";
                break;
        }
        return access;
    }
}
