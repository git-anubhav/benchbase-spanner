/******************************************************************************
 *  Copyright 2015 by OLTPBenchmark Project                                   *
 *                                                                            *
 *  Licensed under the Apache License, Version 2.0 (the "License");           *
 *  you may not use this file except in compliance with the License.          *
 *  You may obtain a copy of the License at                                   *
 *                                                                            *
 *    http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                            *
 *  Unless required by applicable law or agreed to in writing, software       *
 *  distributed under the License is distributed on an "AS IS" BASIS,         *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *  See the License for the specific language governing permissions and       *
 *  limitations under the License.                                            *
 ******************************************************************************/

package com.oltpbenchmark.benchmarks.chbenchmark.queries;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.api.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class GenericQuery extends Procedure {

    private static final Logger LOG = LoggerFactory.getLogger(GenericQuery.class);

    private PreparedStatement stmt;
    private Worker owner;

    public void setOwner(Worker w) {
        this.owner = w;
    }

    protected static SQLStmt initSQLStmt(String queryFile) {
        StringBuilder query = new StringBuilder();

        try {
            // todo: this won't work
            FileReader input = new FileReader("src/com/oltpbenchmark/benchmarks/chbenchmark/queries/" + queryFile);
            BufferedReader reader = new BufferedReader(input);
            String line = reader.readLine();
            while (line != null) {
                query.append(line);
                query.append(" ");
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return new SQLStmt(query.toString());
    }

    protected abstract SQLStmt get_query();

    public ResultSet run(Connection conn) throws SQLException {

        //initializing all prepared statements
        stmt = this.getPreparedStatement(conn, get_query());
        if (owner != null) {
            owner.setCurrStatement(stmt);
        }

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            // If the system thinks we're missing a prepared statement, then we
            // should regenerate them.
            if (ex.getErrorCode() == 0 && ex.getSQLState() != null
                    && ex.getSQLState().equals("07003")) {
                rs = stmt.executeQuery();
            } else {
                throw ex;
            }
        }
        while (rs.next()) {
            //do nothing
        }

        if (owner != null) {
            owner.setCurrStatement(null);
        }

        return null;

    }
}
