package org.intermine.task;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;

import org.apache.tools.ant.Task;

/**
 * Task superclass for invoking converters.
 *
 * @author Matthew Wakeling
 */
public class ConverterTask extends Task
{
    protected String model;
    protected String osName;

    /**
     * Set the objectstore name
     * @param osName the model name
     */
    public void setOsName(String osName) {
        this.osName = osName;
    }

    /**
     * Set the model name
     * @param model the model name
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Runs various performance-enhancing SQL statements.
     *
     * @param os the ObjectStore on which to run the SQL
     * @throws SQLException if something goes wrong
     * @throws IOException if an error occurs while reading from the post-processing sql file
     */
    protected void doSQL(ObjectStore os) throws SQLException, IOException {
        if (os instanceof ObjectStoreInterMineImpl) {
            Connection c = null;
            try {
                c = ((ObjectStoreInterMineImpl) os).getConnection();
                Statement s = c.createStatement();
                System.err .println("ALTER TABLE reference ALTER refid SET STATISTICS 1000");
                s.execute("ALTER TABLE reference ALTER refid SET STATISTICS 1000");
                String filename = "resources/" + model + "_src_items.sql";
                InputStream is = ConverterTask.class.getClassLoader().getResourceAsStream(filename);
                if (is == null) {
                    throw new IllegalArgumentException("Model '" + model + "' does not have an"
                            + " associated src items post-processing sql file (" + filename + ")");
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = br.readLine();
                while (line != null) {
                    s.execute(line);
                    System.err .println(line);
                    line = br.readLine();
                }
                System.err .println("ANALYSE");
                s.execute("ANALYSE");
            } finally {
                ((ObjectStoreInterMineImpl) os).releaseConnection(c);
            }
        }
    }
}

